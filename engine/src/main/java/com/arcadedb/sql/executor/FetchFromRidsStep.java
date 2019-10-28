/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.sql.executor;

import com.arcadedb.database.Document;
import com.arcadedb.database.Identifiable;
import com.arcadedb.database.RID;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.exception.TimeoutException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by luigidellaquila on 22/07/16.
 */
public class FetchFromRidsStep extends AbstractExecutionStep {
  private Collection<RID> rids;

  private Iterator<RID> iterator;

  private Result nextResult = null;

  public FetchFromRidsStep(Collection<RID> rids, CommandContext ctx, boolean profilingEnabled) {
    super(ctx, profilingEnabled);
    this.rids = rids;
    reset();
  }

  public void reset() {
    iterator = rids.iterator();
    nextResult = null;
  }

  @Override
  public ResultSet syncPull(CommandContext ctx, int nRecords) throws TimeoutException {
    getPrev().ifPresent(x -> x.syncPull(ctx, nRecords));
    return new ResultSet() {
      int internalNext = 0;

      private void fetchNext() {
        if (nextResult != null) {
          return;
        }
        while (iterator.hasNext()) {
          RID nextRid = iterator.next();
          if (nextRid == null) {
            continue;
          }
          Identifiable nextDoc = ctx.getDatabase().lookupByRID(nextRid, true);
          if (nextDoc == null) {
            continue;
          }
          nextResult = new ResultInternal();
          ((ResultInternal) nextResult).setElement((Document) nextDoc);
          return;
        }
        return;
      }

      @Override
      public boolean hasNext() {
        if (internalNext >= nRecords) {
          return false;
        }
        if (nextResult == null) {
          fetchNext();
        }
        return nextResult != null;
      }

      @Override
      public Result next() {
        if (!hasNext()) {
          throw new IllegalStateException();
        }

        internalNext++;
        Result result = nextResult;
        nextResult = null;
        return result;
      }

      @Override
      public void close() {

      }

      @Override
      public Optional<ExecutionPlan> getExecutionPlan() {
        return null;
      }

      @Override
      public Map<String, Long> getQueryStats() {
        return null;
      }
    };
  }

  @Override
  public String prettyPrint(int depth, int indent) {
    return ExecutionStepInternal.getIndent(depth, indent) + "+ FETCH FROM RIDs\n" + ExecutionStepInternal.getIndent(depth, indent)
        + "  " + rids;
  }

  @Override
  public Result serialize() {
    ResultInternal result = ExecutionStepInternal.basicSerialize(this);
    if (rids != null) {
      result.setProperty("rids", rids.stream().map(x -> x.toString()).collect(Collectors.toList()));
    }
    return result;
  }

  @Override
  public void deserialize(Result fromResult) {
    try {
      ExecutionStepInternal.basicDeserialize(fromResult, this);
      if (fromResult.getProperty("rids") != null) {
        List<String> ser = fromResult.getProperty("rids");
        throw new UnsupportedOperationException();
//        rids = ser.stream().map(x -> new PRID(x)).collect(Collectors.toList());
      }
      reset();
    } catch (Exception e) {
      throw new CommandExecutionException(e);
    }
  }
}
