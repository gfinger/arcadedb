/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */
package com.arcadedb.stresstest.workload;

import com.arcadedb.database.Record;
import com.arcadedb.exception.NeedRetryException;
import com.arcadedb.log.LogManager;
import com.arcadedb.remote.RemoteDatabase;
import com.arcadedb.stresstest.DatabaseIdentifier;
import com.arcadedb.stresstest.StressTesterSettings;
import com.arcadedb.utility.Callable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * CRUD implementation of the workload.
 *
 * @author Luca Garulli (l.garulli--(at)--orientdb.com)
 */
public abstract class OBaseWorkload implements OWorkload {
  protected RemoteDatabase.CONNECTION_STRATEGY connectionStrategy = RemoteDatabase.CONNECTION_STRATEGY.STICKY;

  public abstract class OBaseWorkLoadContext {
    public int threadId;
    public int currentIdx;
    public int totalPerThread;

    public abstract void init(DatabaseIdentifier dbIdentifier, int operationsPerTransaction);

    public abstract void close();
  }

  public class OWorkLoadResult {
    public AtomicInteger current = new AtomicInteger();
    public int           total   = 1;
    public long          totalTime;
    public long          totalTimeOperationsNs;
    public long          throughputAvgNs;

    public long latencyAvgNs;
    public long latencyMinNs;
    public long latencyMaxNs;
    public int  latencyPercentileAvg;
    public long latencyPercentile99Ns;
    public long latencyPercentile99_9Ns;

    public AtomicInteger conflicts = new AtomicInteger();

    public String toOutput(final int leftSpaces) {
      final StringBuilder indent = new StringBuilder();
      for (int i = 0; i < leftSpaces; ++i)
        indent.append(' ');

      return String.format(
          "\n%s- Throughput: %.3f/sec (Avg %.3fms/op)\n%s- Latency Avg: %.3fms/op (%dth percentile) - Min: %.3fms - 99th Perc: %.3fms - 99.9th Perc: %.3fms - Max: %.3fms - Conflicts: %d",
          indent, total * 1000 / (float) totalTime, throughputAvgNs / 1000000f, indent, latencyAvgNs / 1000000f, latencyPercentileAvg, latencyMinNs / 1000000f,
          latencyPercentile99Ns / 1000000f, latencyPercentile99_9Ns / 1000000f, latencyMaxNs / 1000000f, conflicts.get());
    }

    public JSONObject toJSON() {
      final JSONObject json = new JSONObject();
      json.put("total", total);
      json.put("time", totalTime / 1000f);
      json.put("timeOperations", totalTimeOperationsNs / 1000f);

      json.put("throughput", totalTime > 0 ? total * 1000 / (float) totalTime : 0);
      json.put("throughputAvg", throughputAvgNs / 1000000f);

      json.put("latencyAvg", latencyAvgNs / 1000000f);
      json.put("latencyMin", latencyMinNs / 1000000f);
      json.put("latencyPercAvg", latencyPercentileAvg);
      json.put("latencyPerc99", latencyPercentile99Ns / 1000000f);
      json.put("latencyPerc99_9", latencyPercentile99_9Ns / 1000000f);
      json.put("latencyMax", latencyMaxNs / 1000000f);
      json.put("conflicts", conflicts.get());
      return json;
    }
  }

  protected static final long         MAX_ERRORS = 100;
  protected              List<String> errors     = new ArrayList<String>();

  protected List<OBaseWorkLoadContext> executeOperation(final DatabaseIdentifier dbIdentifier, final OWorkLoadResult result,
      final StressTesterSettings settings, final Callable<Void, OBaseWorkLoadContext> callback) {

    if (result.total == 0)
      return null;

    final int concurrencyLevel = settings.concurrencyLevel;
    final int operationsPerTransaction = settings.operationsPerTransaction;

    final int totalPerThread = result.total / concurrencyLevel;
    final int totalPerLastThread = totalPerThread + result.total % concurrencyLevel;

    final Long[] operationTiming = new Long[result.total];

    final List<OBaseWorkLoadContext> contexts = new ArrayList<OBaseWorkLoadContext>(concurrencyLevel);

    final Thread[] thread = new Thread[concurrencyLevel];
    for (int t = 0; t < concurrencyLevel; ++t) {
      final int currentThread = t;

      final OBaseWorkLoadContext context = getContext();
      contexts.add(context);

      thread[t] = new Thread(new Runnable() {
        @Override
        public void run() {
          context.threadId = currentThread;
          context.totalPerThread = context.threadId < concurrencyLevel - 1 ? totalPerThread : totalPerLastThread;

          context.init(dbIdentifier, operationsPerTransaction);
          try {
            final int startIdx = totalPerThread * context.threadId;

            final AtomicInteger operationsExecutedInTx = new AtomicInteger();

            for (final AtomicInteger i = new AtomicInteger(); i.get() < context.totalPerThread; i.incrementAndGet()) {
              executeWithRetries(new Callable<Object, Integer>() {
                @Override
                public Object call(final Integer retry) {
                  if (retry > 0) {
                    i.addAndGet(operationsExecutedInTx.get() * -1);
                    if (i.get() < 0)
                      i.set(0);
                    operationsExecutedInTx.set(0);
                  }

                  context.currentIdx = startIdx + i.get();

                  final long startOp = System.nanoTime();
                  try {

                    try {
                      return callback.call(context);
                    } finally {
                      operationsExecutedInTx.incrementAndGet();

                      if (operationsPerTransaction > 0 && (i.get() + 1) % operationsPerTransaction == 0 || i.get() == context.totalPerThread - 1) {
                        commitTransaction(context);
                        operationsExecutedInTx.set(0);
                        beginTransaction(context);
                      }
                    }

                  } catch (NeedRetryException e) {
                    result.conflicts.incrementAndGet();

                    manageNeedRetryException(context, e);

                    if (operationsPerTransaction > 0)
                      beginTransaction(context);

                    throw e;

                  } catch (Exception e) {
                    errors.add(e.toString());
                    if (errors.size() > MAX_ERRORS) {
                      LogManager.instance().log(this, Level.SEVERE, "Error during execution of database operation", e);
                      return null;
                    }
                  } finally {
                    operationTiming[context.currentIdx] = System.nanoTime() - startOp;
                  }

                  return null;
                }
              }, 10);

              if (settings.delay > 0)
                try {
                  Thread.sleep(settings.delay);
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
            }

            if (operationsPerTransaction > 0)
              commitTransaction(context);

          } finally {
            context.close();
          }
        }
      });
    }

    final long startTime = System.currentTimeMillis();

    // START ALL THE THREADS
    for (int t = 0; t < concurrencyLevel; ++t)
      thread[t].start();

    // WAIT FOR ALL THE THREADS
    for (int t = 0; t < concurrencyLevel; ++t) {
      try {
        thread[t].join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    // STOP THE COUNTER
    result.totalTime = System.currentTimeMillis() - startTime;

    Arrays.sort(operationTiming);

    result.throughputAvgNs = (int) (result.totalTime * 1000000 / operationTiming.length);

    // COMPUTE THE TOTAL COST OF OPERATIONS ONLY
    result.totalTimeOperationsNs = 0;
    for (long l : operationTiming)
      result.totalTimeOperationsNs += l;

    result.latencyMinNs = operationTiming[0];
    result.latencyMaxNs = operationTiming[operationTiming.length - 1];

    result.latencyAvgNs = (int) (result.totalTimeOperationsNs / operationTiming.length);
    result.latencyPercentileAvg = getPercentile(operationTiming, result.latencyAvgNs);
    result.latencyPercentile99Ns = operationTiming[(int) (operationTiming.length * 99f / 100f)];
    result.latencyPercentile99_9Ns = operationTiming[(int) (operationTiming.length * 99.9f / 100f)];

    return contexts;
  }

  protected void manageNeedRetryException(final OBaseWorkLoadContext context, final NeedRetryException e) {
  }

  protected abstract void beginTransaction(OBaseWorkLoadContext context);

  protected abstract void commitTransaction(OBaseWorkLoadContext context);

  protected abstract OBaseWorkLoadContext getContext();

  protected String getErrors() {
    final StringBuilder buffer = new StringBuilder();
    if (!errors.isEmpty()) {
      buffer.append("\nERRORS:");
      for (int i = 0; i < errors.size(); ++i) {
        buffer.append("\n");
        buffer.append(i);
        buffer.append(": ");
        buffer.append(errors.get(i));
      }
    }
    return buffer.toString();
  }

  protected int getPercentile(final Long[] sortedResults, final long time) {
    int j = 0;
    for (; j < sortedResults.length; j++) {
      final Long valueNs = sortedResults[j];
      if (valueNs > time) {
        break;
      }
    }
    return (int) (100 * (j / (float) sortedResults.length));
  }

  public static Object executeWithRetries(final com.arcadedb.utility.Callable<Object, Integer> callback, final int maxRetry) {
    return executeWithRetries(callback, maxRetry, 0, null);
  }

  public static Object executeWithRetries(final com.arcadedb.utility.Callable<Object, Integer> callback, final int maxRetry, final int waitBetweenRetry) {
    return executeWithRetries(callback, maxRetry, waitBetweenRetry, null);
  }

  public static Object executeWithRetries(final com.arcadedb.utility.Callable<Object, Integer> callback, final int maxRetry, final int waitBetweenRetry,
      final Record[] recordToReloadOnRetry) {
    NeedRetryException lastException = null;
    for (int retry = 0; retry < maxRetry; ++retry) {
      try {
        return callback.call(retry);
      } catch (NeedRetryException e) {
        // SAVE LAST EXCEPTION AND RETRY
        lastException = e;

        if (recordToReloadOnRetry != null) {
          // RELOAD THE RECORDS
          for (Record r : recordToReloadOnRetry)
            r.reload();
        }

        if (waitBetweenRetry > 0)
          try {
            Thread.sleep(waitBetweenRetry);
          } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
            break;
          }
      }
    }
    throw lastException;
  }
}
