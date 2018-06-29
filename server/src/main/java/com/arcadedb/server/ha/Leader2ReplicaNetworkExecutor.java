/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */
package com.arcadedb.server.ha;

import com.arcadedb.Constants;
import com.arcadedb.GlobalConfiguration;
import com.arcadedb.database.Binary;
import com.arcadedb.network.binary.ChannelBinaryServer;
import com.arcadedb.network.binary.ConnectionException;
import com.arcadedb.server.ha.message.HACommand;
import com.arcadedb.server.ha.message.ReplicaConnectHotResyncResponse;
import com.arcadedb.utility.Callable;
import com.arcadedb.utility.FileUtils;
import com.arcadedb.utility.Pair;
import com.conversantmedia.util.concurrent.PushPullBlockingQueue;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * This executor has an intermediate level of buffering managed with a queue. This avoids the Leader to be blocked in case the
 * remote replica does not read messages and the socket remains full causing a block in the sending of messages for all the
 * servers.
 */
public class Leader2ReplicaNetworkExecutor extends Thread {
  public enum STATUS {
    JOINING, OFFLINE, ONLINE
  }

  public static final int PROTOCOL_VERSION = 0;

  private final    HAServer                      server;
  private final    String                        remoteServerName;
  private final    PushPullBlockingQueue<Binary> queue                 = new PushPullBlockingQueue<>(
      GlobalConfiguration.HA_REPLICATION_QUEUE_SIZE.getValueAsInteger());
  private          long                          joinedOn;
  private          long                          leftOn                = 0;
  private          ChannelBinaryServer           channel;
  private          Thread                        queueThread;
  private          STATUS                        status                = STATUS.JOINING;
  private          Object                        lock                  = new Object();
  private volatile boolean                       shutdownCommunication = false;

  // STATS
  private long totalMessages;
  private long totalBytes;
  private long latencyMin;
  private long latencyMax;
  private long latencyTotalTime;

  public Leader2ReplicaNetworkExecutor(final HAServer ha, final Socket socket) throws IOException {
    this.server = ha;
    setName(Constants.PRODUCT + "-ha-leader2replica/?");
    this.channel = new ChannelBinaryServer(socket);

    try {
      if (!ha.isLeader()) {
        this.channel.writeBoolean(false);
        this.channel.writeString("Current server '" + ha.getServerName() + "' is not the leader");
        throw new ConnectionException(socket.getInetAddress().toString(), "Current server '" + ha.getServerName() + "' is not the leader");
      }

      final short remoteProtocolVersion = this.channel.readShort();
      if (remoteProtocolVersion != PROTOCOL_VERSION) {
        this.channel.writeBoolean(false);
        this.channel.writeString("Network protocol version " + remoteProtocolVersion + " is different than local server " + PROTOCOL_VERSION);
        throw new ConnectionException(socket.getInetAddress().toString(),
            "Network protocol version " + remoteProtocolVersion + " is different than local server " + PROTOCOL_VERSION);
      }

      final String remoteClusterName = this.channel.readString();
      if (!remoteClusterName.equals(ha.getClusterName())) {
        this.channel.writeBoolean(false);
        this.channel.writeString("Cluster name '" + remoteClusterName + "' does not match");
        throw new ConnectionException(socket.getInetAddress().toString(), "Cluster name '" + remoteClusterName + "' does not match");
      }

      remoteServerName = this.channel.readString();

      setName(Constants.PRODUCT + "-ha-leader2replica/" + remoteServerName);

      ha.getServer().log(this, Level.INFO, "Remote server '%s' successfully connected", remoteServerName);

      // CONNECTED
      this.channel.writeBoolean(true);

    } finally {
      this.channel.flush();
    }
  }

  public void mergeFrom(final Leader2ReplicaNetworkExecutor previousConnection) {
    lock = previousConnection.lock;
    queue.addAll(previousConnection.queue);
  }

  @Override
  public void run() {
    queueThread = new Thread(new Runnable() {
      @Override
      public void run() {
        Binary lastMessage = null;
        while (!shutdownCommunication || !queue.isEmpty()) {
          try {
            if (lastMessage == null)
              lastMessage = queue.poll(1000, TimeUnit.MILLISECONDS);

            if (lastMessage == null)
              continue;

            if (shutdownCommunication)
              break;

            switch (status) {
            case ONLINE:

              server.getServer().log(this, Level.FINE, "Sending message to replica '%s' (buffered=%d)...", remoteServerName, queue.size());

              sendMessage(lastMessage);
              lastMessage = null;
              break;

            default:
              Thread.sleep(500);
              continue;
            }

          } catch (IOException e) {
            server.getServer().log(this, Level.INFO, "Error on sending replication message to remote server '%s'", e, remoteServerName);
            shutdownCommunication = true;
            return;
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
          }
        }

        server.getServer().log(this, Level.FINE, "Replication thread to remote server '%s' is off (buffered=%d)", remoteServerName, queue.size());

      }
    });
    queueThread.start();
    queueThread.setName(Constants.PRODUCT + "-ha-leader2replica-sender/" + remoteServerName);

    // REUSE THE SAME BUFFER TO AVOID MALLOC
    final Binary buffer = new Binary(1024);

    while (!shutdownCommunication) {
      try {
        final Pair<ReplicationMessage, HACommand> request = server.getMessageFactory().deserializeCommand(buffer, channel.readBytes());

        if (request == null) {
          channel.clearInput();
          continue;
        }

        final ReplicationMessage message = request.getFirst();

        final HACommand response = request.getSecond().execute(server, remoteServerName, message.messageNumber);
        if (response != null) {
          // SEND THE RESPONSE BACK (USING THE SAME BUFFER)
          server.getMessageFactory().serializeCommand(response, buffer, message.messageNumber);

          server.getServer().log(this, Level.FINE, "Request %s -> %s", request, response);

          sendMessage(buffer);

          if (response instanceof ReplicaConnectHotResyncResponse) {
            server.resendMessagesToReplica(((ReplicaConnectHotResyncResponse) response).getPositionInLog(), remoteServerName);
            server.setReplicaStatus(remoteServerName, true);
          }
        }

      } catch (EOFException | SocketException e) {
        server.getServer().log(this, Level.FINE, "Error on reading request from socket", e);
        server.setReplicaStatus(remoteServerName, false);
        close();
      } catch (IOException e) {
        server.getServer().log(this, Level.SEVERE, "Error on reading request", e);
        close();
      }
    }
  }

  public void close() {
    shutdownCommunication = true;

    try {
      final Thread qt = queueThread;
      if (qt != null) {
        try {
          qt.join(5000);
          queueThread = null;
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          // IGNORE IT
        }
      }

      final ChannelBinaryServer c = channel;
      if (c != null) {
        c.close();
        channel = null;
      }

    } catch (Exception e) {
      // IGNORE IT
    }
  }

  public void enqueueMessage(final Binary message) {
    executeInLock(new Callable() {
      @Override
      public Object call(Object iArgument) {

        // WRITE DIRECTLY TO THE MESSAGE QUEUE
        if (queue.size() > 1)
          server.getServer().log(this, Level.FINE, "Buffering request to server '%s' (status=%s buffered=%d)", remoteServerName, status, queue.size());

        if (!queue.offer(message)) {
          // BACK-PRESSURE
          server.getServer()
              .log(this, Level.WARNING, "Applying back-pressure on replicating messages to server '%s' (latency=%s buffered=%d)...", getRemoteServerName(),
                  getLatencyStats(), queue.size());
          try {
            Thread.sleep(200);
          } catch (InterruptedException e) {
            // IGNORE IT
            Thread.currentThread().interrupt();
            throw new ReplicationException("Error on replicating to server '" + remoteServerName + "'");
          }

          if (!queue.offer(message)) {
            server.setReplicaStatus(remoteServerName, false);

            // QUEUE FULL, THE REMOTE SERVER COULD BE STUCK SOMEWHERE. REMOVE THE REPLICA
            throw new ReplicationException("Replica '" + remoteServerName + "' is not reading replication messages");
          }
        }
        return null;
      }
    });

    totalBytes += message.size();
  }

  public void setStatus(final STATUS status) {
    if (this.status == status)
      // NO STATUS CHANGE
      return;

    executeInLock(new Callable() {
      @Override
      public Object call(Object iArgument) {
        Leader2ReplicaNetworkExecutor.this.status = status;
        Leader2ReplicaNetworkExecutor.this.server.getServer().log(this, Level.INFO, "Replica server '%s' is %s", remoteServerName, status);

        Leader2ReplicaNetworkExecutor.this.leftOn = status == STATUS.OFFLINE ? 0 : System.currentTimeMillis();

        if (status == STATUS.ONLINE) {
          Leader2ReplicaNetworkExecutor.this.joinedOn = System.currentTimeMillis();
          Leader2ReplicaNetworkExecutor.this.leftOn = 0;
        } else if (status == STATUS.OFFLINE) {
          Leader2ReplicaNetworkExecutor.this.leftOn = System.currentTimeMillis();
          close();
        }
        return null;
      }
    });

    server.printClusterConfiguration();
  }

  public String getRemoteServerName() {
    return remoteServerName;
  }

  public long getJoinedOn() {
    return joinedOn;
  }

  public long getLeftOn() {
    return leftOn;
  }

  public void updateStats(final long sentOn, final long receivedOn) {
    totalMessages++;

    final long delta = receivedOn - sentOn;
    latencyTotalTime += delta;

    if (latencyMin == -1 || delta < latencyMin)
      latencyMin = delta;
    if (delta > latencyMax)
      latencyMax = delta;
  }

  public STATUS getStatus() {
    return status;
  }

  public String getLatencyStats() {
    if (totalMessages == 0)
      return "";
    return "avg=" + (latencyTotalTime / totalMessages) + " (min=" + latencyMin + " max=" + latencyMax + ")";
  }

  public String getThroughputStats() {
    if (totalBytes == 0)
      return "";
    return FileUtils.getSizeAsString(totalBytes) + " (" + FileUtils
        .getSizeAsString((int) (((double) totalBytes / (System.currentTimeMillis() - joinedOn)) * 1000)) + "/s)";
  }

  public void sendMessage(final Binary msg) throws IOException {
    channel.writeBytes(msg.getContent(), msg.size());
    channel.flush();
  }

  protected void executeInLock(final Callable callback) {
    synchronized (lock) {
      callback.call(null);
    }
  }
}