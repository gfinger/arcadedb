/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */
package com.arcadedb.server.ha;

import com.arcadedb.Constants;
import com.arcadedb.ContextConfiguration;
import com.arcadedb.GlobalConfiguration;
import com.arcadedb.database.Binary;
import com.arcadedb.database.Database;
import com.arcadedb.engine.ModifiablePage;
import com.arcadedb.engine.PageId;
import com.arcadedb.engine.PageManager;
import com.arcadedb.engine.PaginatedFile;
import com.arcadedb.network.binary.ChannelBinaryClient;
import com.arcadedb.network.binary.ConnectionException;
import com.arcadedb.network.binary.NetworkProtocolException;
import com.arcadedb.network.binary.ServerIsNotTheLeaderException;
import com.arcadedb.schema.SchemaImpl;
import com.arcadedb.server.ServerException;
import com.arcadedb.server.TestCallback;
import com.arcadedb.server.ha.message.*;
import com.arcadedb.utility.FileUtils;
import com.arcadedb.utility.LogManager;
import com.arcadedb.utility.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class Replica2LeaderNetworkExecutor extends Thread {
  private final    HAServer             server;
  private final    String               host;
  private final    int                  port;
  private          String               leaderServerName  = "?";
  private          String               leaderServerHTTPAddress;
  private final    ContextConfiguration configuration;
  private final    boolean              testOn;
  private          ChannelBinaryClient  channel;
  private volatile boolean              shutdown          = false;
  private          Object               channelOutputLock = new Object();
  private          Object               channelInputLock  = new Object();

  public Replica2LeaderNetworkExecutor(final HAServer ha, final String host, final int port, final ContextConfiguration configuration) {
    this.server = ha;
    this.testOn = GlobalConfiguration.TEST.getValueAsBoolean();

    this.host = host;
    this.port = port;
    this.configuration = configuration;

    connect();
  }

  @Override
  public void run() {
    LogManager.instance().setContext(server.getServer().getServerName());

    // REUSE THE SAME BUFFER TO AVOID MALLOC
    final Binary buffer = new Binary(1024);

    while (!shutdown || channel.inputHasData()) {
      try {
        final byte[] requestBytes = receiveResponse();

        if (shutdown)
          return;

        final Pair<ReplicationMessage, HACommand> request = server.getMessageFactory().deserializeCommand(buffer, requestBytes);

        if (request == null) {
          channel.clearInput();
          continue;
        }

        final ReplicationMessage message = request.getFirst();

        server.getServer().log(this, Level.FINE, "Received request %d from the Leader (threadId=%d)", message.messageNumber, Thread.currentThread().getId());

        final long lastMessage = server.getReplicationLogFile().getLastMessageNumber();

        if (message.messageNumber <= lastMessage) {
          server.getServer().log(this, Level.FINE, "Message %d already applied on local server (last=%d). Skip this", message.messageNumber, lastMessage);
          continue;
        }

        if (!server.getReplicationLogFile().checkMessageOrder(message))
          // SKIP
          continue;

        final HACommand response = request.getSecond().execute(server, leaderServerName, message.messageNumber);

        server.getReplicationLogFile().appendMessage(message);

        if (testOn)
          server.getServer().lifecycleEvent(TestCallback.TYPE.REPLICA_MSG_RECEIVED, request);

        if (response != null)
          sendCommandToLeader(buffer, response, message.messageNumber);

      } catch (SocketTimeoutException e) {
        // IGNORE IT
      } catch (Exception e) {
        reconnect(e);
      }
    }
  }

  public String getRemoteServerName() {
    return leaderServerName;
  }

  public String getRemoteAddress() {
    return host + ":" + port;
  }

  private void reconnect(final Exception e) {
    if (!shutdown) {
      if (channel != null)
        channel.close();

      if (server.getLeader() != this) {
        // LEADER ALREADY ELECTED
        server.getServer().log(this, Level.SEVERE, "Removing connection to the previous Leader ('%s'). New Leader is: %s", getRemoteServerName(),
            server.getLeader().getRemoteServerName());
        close();
        return;
      }

      server.getServer()
          .log(this, Level.SEVERE, "Error on communication between current replica and the Leader ('%s'), reconnecting... (error=%s)", getRemoteServerName(),
              e.toString());

      if (!shutdown) {
        try {
          connect();
        } catch (ConnectionException e1) {
          server.getServer().log(this, Level.SEVERE, "Error on re-connecting to the Leader ('%s'), start election (error=%s)", getRemoteServerName(), e1);
          close();
          server.startElection();
        }
      }
    }
  }

  public void sendCommandToLeader(final Binary buffer, final HACommand response, final long messageNumber) throws IOException {
    server.getServer().log(this, Level.FINE, "Sending message (response to %d) to the Leader '%s'...", messageNumber, response);

    server.getMessageFactory().serializeCommand(response, buffer, messageNumber);

    synchronized (channelOutputLock) {
      channel.writeBytes(buffer.getContent(), buffer.size());
      channel.flush();
    }
  }

  public void close() {
    shutdown = true;
    if (channel != null)
      channel.close();
  }

  public void kill() {
    shutdown = true;
    interrupt();
    close();
  }

  public String getRemoteHTTPAddress() {
    return leaderServerHTTPAddress;
  }

  @Override
  public String toString() {
    return leaderServerName;
  }

  private byte[] receiveResponse() throws IOException {
    synchronized (channelInputLock) {
      return channel.readBytes();
    }
  }

  private void connect() {
    server.getServer().log(this, Level.FINE, "Connecting to server %s:%d...", host, port);

    try {
      channel = server.createNetworkConnection(host, port, ReplicationProtocol.COMMAND_CONNECT);
      channel.flush();

      // READ RESPONSE
      synchronized (channelInputLock) {
        final boolean connectionAccepted = channel.readBoolean();
        if (!connectionAccepted) {
          final String reason = channel.readString();

          byte reasonCode = channel.readByte();

          switch (reasonCode) {
          case ReplicationProtocol.ERROR_CONNECT_NOLEADER:
            final String leaderServerName = channel.readString();
            final String leaderAddress = channel.readString();
            server.getServer()
                .log(this, Level.INFO, "Remote server is not a Leader, connecting to the current Leader '%s' (%s)", leaderServerName, leaderAddress);
            channel.close();
            throw new ServerIsNotTheLeaderException(
                "Remote server is not a Leader, connecting to the current Leader '" + leaderServerName + "' (" + leaderAddress + ")", leaderAddress);

          case ReplicationProtocol.ERROR_CONNECT_ELECTION_PENDING:
            server.getServer().log(this, Level.INFO, "An election for the Leader server is pending");
            channel.close();
            throw new ReplicationException("An election for the Leader server is pending");

          case ReplicationProtocol.ERROR_CONNECT_UNSUPPORTEDPROTOCOL:
            server.getServer().log(this, Level.INFO, "Remote server does not support protocol %d", ReplicationProtocol.PROTOCOL_VERSION);
            break;

          case ReplicationProtocol.ERROR_CONNECT_WRONGCLUSTERNAME:
            server.getServer().log(this, Level.INFO, "Remote server joined a different cluster than '%s'", server.getClusterName());
            break;
          }

          channel.close();
          throw new ConnectionException(host + ":" + port, reason);
        }

        leaderServerName = channel.readString();
        leaderServerHTTPAddress = channel.readString();
        final String memberList = channel.readString();
        server.setServerAddresses(memberList);
      }

      server.getServer().log(this, Level.INFO, "Server connected to the Leader server %s:%d, members=[%s]", host, port, server.getServerAddressList());

      setName(Constants.PRODUCT + "-ha-replica2leader/" + server.getServerName());

      server.getServer().log(this, Level.INFO, "Server started as Replica in HA mode (cluster=%s leader=%s:%d)", server.getClusterName(), host, port);

      installDatabases();
    } catch (IOException e) {
      throw new ConnectionException(host + ":" + port, e.toString());
    }
  }

  private void installDatabases() {
    final Binary buffer = new Binary(1024);

    final ReplicationMessage lastMessage = server.getReplicationLogFile().getLastMessage();
    long lastLogNumber = lastMessage != null ? lastMessage.messageNumber + 1 : -1;

    try {
      sendCommandToLeader(buffer, new ReplicaConnectRequest(lastLogNumber), -1);
      final HACommand response = receiveCommandFromLeaderDuringJoining(buffer);

      if (response instanceof ReplicaConnectFullResyncResponse) {
        server.getServer().log(this, Level.INFO, "Asking for a full resync...");

        if (testOn)
          server.getServer().lifecycleEvent(TestCallback.TYPE.REPLICA_FULL_RESYNC, null);

        final ReplicaConnectFullResyncResponse fullSync = (ReplicaConnectFullResyncResponse) response;

        final Set<String> databases = fullSync.getDatabases();

        for (String db : databases) {
          sendCommandToLeader(buffer, new DatabaseStructureRequest(db), -1);
          final DatabaseStructureResponse dbStructure = (DatabaseStructureResponse) receiveCommandFromLeaderDuringJoining(buffer);

          final Database database = server.getServer().getOrCreateDatabase(db);

          installDatabase(buffer, db, dbStructure, database);
        }

      } else {
        server.getServer().log(this, Level.INFO, "Receiving hot resync...");

        if (testOn)
          server.getServer().lifecycleEvent(TestCallback.TYPE.REPLICA_HOT_RESYNC, null);
      }

      sendCommandToLeader(buffer, new ReplicaReadyRequest(), -1);

    } catch (Exception e) {
      server.getServer().log(this, Level.SEVERE, "Error on starting HA service (error=%s)", e);
      throw new ServerException("Cannot start HA service", e);
    }
  }

  private void installDatabase(final Binary buffer, final String db, final DatabaseStructureResponse dbStructure, final Database database) throws IOException {

    // WRITE THE SCHEMA
    final FileWriter schemaFile = new FileWriter(database.getDatabasePath() + "/" + SchemaImpl.SCHEMA_FILE_NAME);
    try {
      schemaFile.write(dbStructure.getSchemaJson());
    } finally {
      schemaFile.close();
    }

    // WRITE ALL THE FILES
    for (Map.Entry<Integer, String> f : dbStructure.getFileNames().entrySet()) {
      installFile(buffer, db, database, f.getKey(), f.getValue());
    }

    // RELOAD THE SCHEMA
    ((SchemaImpl) database.getSchema()).close();
    ((SchemaImpl) database.getSchema()).load(PaginatedFile.MODE.READ_ONLY);
  }

  private void installFile(final Binary buffer, final String db, final Database database, final int fileId, final String fileName) throws IOException {
    final PageManager pageManager = database.getPageManager();

    final PaginatedFile file = database.getFileManager().getOrCreateFile(fileId, database.getDatabasePath() + "/" + fileName);

    final int pageSize = file.getPageSize();

    int from = 0;

    server.getServer().log(this, Level.FINE, "Installing file '%s'...", fileName);

    int pages = 0;
    long fileSize = 0;

    while (true) {
      sendCommandToLeader(buffer, new FileContentRequest(db, fileId, from), -1);
      final FileContentResponse fileChunk = (FileContentResponse) receiveCommandFromLeaderDuringJoining(buffer);

      if (fileChunk.getPages() == 0)
        break;

      if (fileChunk.getPagesContent().size() != fileChunk.getPages() * pageSize) {
        server.getServer().log(this, Level.SEVERE, "Error on received chunk for file '%s': size=%s, expected=%s (pages=%d)", fileName,
            FileUtils.getSizeAsString(fileChunk.getPagesContent().size()), FileUtils.getSizeAsString(fileChunk.getPages() * pageSize), pages);
        throw new ReplicationException("Invalid file chunk");
      }

      for (int i = 0; i < fileChunk.getPages(); ++i) {
        final ModifiablePage page = new ModifiablePage(pageManager, new PageId(file.getFileId(), from + i), pageSize);
        System.arraycopy(fileChunk.getPagesContent().getContent(), i * pageSize, page.getTrackable().getContent(), 0, pageSize);
        page.loadMetadata();
        pageManager.overridePage(page);

        ++pages;
        fileSize += pageSize;
      }

      if (fileChunk.isLast())
        break;

      from += fileChunk.getPages();
    }

    server.getServer().log(this, Level.FINE, "File '%s' installed (pages=%d size=%s)", fileName, pages, FileUtils.getSizeAsString(fileSize));
  }

  private HACommand receiveCommandFromLeaderDuringJoining(final Binary buffer) throws IOException {
    final byte[] response = receiveResponse();

    final Pair<ReplicationMessage, HACommand> command = server.getMessageFactory().deserializeCommand(buffer, response);
    if (command == null)
      throw new NetworkProtocolException("Error on reading response, message " + response[0] + " not valid");

    return command.getSecond();
  }
}
