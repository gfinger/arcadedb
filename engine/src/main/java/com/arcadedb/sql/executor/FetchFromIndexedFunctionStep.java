/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.sql.executor;

import com.arcadedb.database.Document;
import com.arcadedb.database.Record;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.exception.TimeoutException;
import com.arcadedb.sql.parser.BinaryCondition;
import com.arcadedb.sql.parser.FromClause;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Created by luigidellaquila on 06/08/16.
 */
public class FetchFromIndexedFunctionStep extends AbstractExecutionStep {
  private BinaryCondition functionCondition;
  private FromClause      queryTarget;

  private long cost = 0;
  //runtime0
  Iterator<Record> fullResult = null;

  public FetchFromIndexedFunctionStep(BinaryCondition functionCondition, FromClause queryTarget, CommandContext ctx,
      boolean profilingEnabled) {
    super(ctx, profilingEnabled);
    this.functionCondition = functionCondition;
    this.queryTarget = queryTarget;
  }

  @Override
  public ResultSet syncPull(CommandContext ctx, int nRecords) throws TimeoutException {
    getPrev().ifPresent(x -> x.syncPull(ctx, nRecords));
    init(ctx);

    return new ResultSet() {
      int localCount = 0;

      @Override
      public boolean hasNext() {
        if (localCount >= nRecords) {
          return false;
        }
        return fullResult.hasNext();
      }

      @Override
      public Result next() {
        long begin = profilingEnabled ? System.nanoTime() : 0;
        try {
          if (localCount >= nRecords) {
            throw new IllegalStateException();
          }
          if (!fullResult.hasNext()) {
            throw new IllegalStateException();
          }
          ResultInternal result = new ResultInternal();
          result.setElement((Document) fullResult.next().getRecord());
          localCount++;
          return result;
        } finally {
          if (profilingEnabled) {
            cost += (System.nanoTime() - begin);
          }
        }
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

  private void init(CommandContext ctx) {
    if (fullResult == null) {
      long begin = profilingEnabled ? System.nanoTime() : 0;
      try {
        fullResult = functionCondition.executeIndexedFunction(queryTarget, ctx).iterator();
      } finally {
        if (profilingEnabled) {
          cost += (System.nanoTime() - begin);
        }
      }
    }
  }

  @Override
  public String prettyPrint(int depth, int indent) {
    String result =
        ExecutionStepInternal.getIndent(depth, indent) + "+ FETCH FROM INDEXED FUNCTION " + functionCondition.toString();
    if (profilingEnabled) {
      result += " (" + getCostFormatted() + ")";
    }
    return result;
  }

  @Override
  public void reset() {
    this.fullResult = null;
  }

  @Override
  public long getCost() {
    return cost;
  }

  @Override
  public Result serialize() {
    ResultInternal result = ExecutionStepInternal.basicSerialize(this);
    result.setProperty("functionCondition", this.functionCondition.serialize());
    result.setProperty("queryTarget", this.queryTarget.serialize());

    return result;
  }

  @Override
  public void deserialize(Result fromResult) {
    try {
      ExecutionStepInternal.basicDeserialize(fromResult, this);
      functionCondition = new BinaryCondition(-1);
      functionCondition.deserialize(fromResult.getProperty("functionCondition "));

      queryTarget = new FromClause(-1);
      queryTarget.deserialize(fromResult.getProperty("functionCondition "));

    } catch (Exception e) {
      throw new CommandExecutionException(e);
    }
  }
}
