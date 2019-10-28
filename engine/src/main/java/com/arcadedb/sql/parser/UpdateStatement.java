/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OUpdateStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Database;
import com.arcadedb.database.DatabaseInternal;
import com.arcadedb.sql.executor.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateStatement extends Statement {
  public FromClause target;

  protected List<UpdateOperations> operations = new ArrayList<UpdateOperations>();

  protected boolean upsert = false;

  protected boolean    returnBefore = false;
  protected boolean    returnAfter  = false;
  protected boolean    returnCount  = false;
  protected Projection returnProjection;

  public WhereClause whereClause;

  public Object lockRecord = null;

  public Limit   limit;
  public Timeout timeout;

  public UpdateStatement(int id) {
    super(id);
  }

  public UpdateStatement(SqlParser p, int id) {
    super(p, id);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append(getStatementType());
    if (target != null) {
      target.toString(params, builder);
    }

    for (UpdateOperations ops : this.operations) {
      builder.append(" ");
      ops.toString(params, builder);
    }

    if (upsert) {
      builder.append(" UPSERT");
    }

    if (returnBefore || returnAfter || returnCount) {
      builder.append(" RETURN");
      if (returnBefore) {
        builder.append(" BEFORE");
      } else if (returnAfter) {
        builder.append(" AFTER");
      } else {
        builder.append(" COUNT");
      }
      if (returnProjection != null) {
        builder.append(" ");
        returnProjection.toString(params, builder);
      }
    }
    if (whereClause != null) {
      builder.append(" WHERE ");
      whereClause.toString(params, builder);
    }

//    if (lockRecord != null) {
//      builder.append(" LOCK ");
//      switch (lockRecord) {
//      case DEFAULT:
//        builder.append("DEFAULT");
//        break;
//      case EXCLUSIVE_LOCK:
//        builder.append("RECORD");
//        break;
//      case SHARED_LOCK:
//        builder.append("SHARED");
//        break;
//      case NONE:
//        builder.append("NONE");
//        break;
//      }
//    }
    if (limit != null) {
      limit.toString(params, builder);
    }
    if (timeout != null) {
      timeout.toString(params, builder);
    }
  }

  protected String getStatementType() {
    return "UPDATE ";
  }

  @Override
  public UpdateStatement copy() {
    UpdateStatement result = null;
    try {
      result = getClass().getConstructor(Integer.TYPE).newInstance(-1);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    result.target = target == null ? null : target.copy();
    result.operations = operations == null ? null : operations.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.upsert = upsert;
    result.returnBefore = returnBefore;
    result.returnAfter = returnAfter;
    result.returnProjection = returnProjection == null ? null : returnProjection.copy();
    result.whereClause = whereClause == null ? null : whereClause.copy();
    result.lockRecord = lockRecord;
    result.limit = limit == null ? null : limit.copy();
    result.timeout = timeout == null ? null : timeout.copy();
    return result;
  }

  @Override
  public ResultSet execute(Database db, Object[] args, CommandContext parentCtx) {
    BasicCommandContext ctx = new BasicCommandContext();
    if (parentCtx != null) {
      ctx.setParentWithoutOverridingChild(parentCtx);
    }
    ctx.setDatabase(db);
    Map<Object, Object> params = new HashMap<>();
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        params.put(i, args[i]);
      }
    }
    ctx.setInputParameters(params);
    UpdateExecutionPlan executionPlan = createExecutionPlan(ctx, false);
    executionPlan.executeInternal();
    return new LocalResultSet(executionPlan);
  }

  @Override
  public ResultSet execute(Database db, Map params, CommandContext parentCtx) {
    BasicCommandContext ctx = new BasicCommandContext();
    if (parentCtx != null) {
      ctx.setParentWithoutOverridingChild(parentCtx);
    }
    ctx.setDatabase(db);
    ctx.setInputParameters(params);
    UpdateExecutionPlan executionPlan = createExecutionPlan(ctx, false);
    executionPlan.executeInternal();
    return new LocalResultSet(executionPlan);
  }

  public UpdateExecutionPlan createExecutionPlan(CommandContext ctx, boolean enableProfiling) {
    OUpdateExecutionPlanner planner = new OUpdateExecutionPlanner(this);
    return planner.createExecutionPlan(ctx, enableProfiling);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    UpdateStatement that = (UpdateStatement) o;

    if (upsert != that.upsert)
      return false;
    if (returnBefore != that.returnBefore)
      return false;
    if (returnAfter != that.returnAfter)
      return false;
    if (target != null ? !target.equals(that.target) : that.target != null)
      return false;
    if (operations != null ? !operations.equals(that.operations) : that.operations != null)
      return false;
    if (returnProjection != null ? !returnProjection.equals(that.returnProjection) : that.returnProjection != null)
      return false;
    if (whereClause != null ? !whereClause.equals(that.whereClause) : that.whereClause != null)
      return false;
    if (lockRecord != that.lockRecord)
      return false;
    if (limit != null ? !limit.equals(that.limit) : that.limit != null)
      return false;
    return timeout != null ? timeout.equals(that.timeout) : that.timeout == null;
  }

  @Override
  public int hashCode() {
    int result = target != null ? target.hashCode() : 0;
    result = 31 * result + (operations != null ? operations.hashCode() : 0);
    result = 31 * result + (upsert ? 1 : 0);
    result = 31 * result + (returnBefore ? 1 : 0);
    result = 31 * result + (returnAfter ? 1 : 0);
    result = 31 * result + (returnProjection != null ? returnProjection.hashCode() : 0);
    result = 31 * result + (whereClause != null ? whereClause.hashCode() : 0);
    result = 31 * result + (lockRecord != null ? lockRecord.hashCode() : 0);
    result = 31 * result + (limit != null ? limit.hashCode() : 0);
    result = 31 * result + (timeout != null ? timeout.hashCode() : 0);
    return result;
  }

  public FromClause getTarget() {
    return target;
  }

  public List<UpdateOperations> getOperations() {
    return operations;
  }

  public boolean isUpsert() {
    return upsert;
  }

  public boolean isReturnBefore() {
    return returnBefore;
  }

  public boolean isReturnAfter() {
    return returnAfter;
  }

  public boolean isReturnCount() {
    return returnCount;
  }

  public Projection getReturnProjection() {
    return returnProjection;
  }

  public WhereClause getWhereClause() {
    return whereClause;
  }

  public Object getLockRecord() {
    return lockRecord;
  }

  public Limit getLimit() {
    return limit;
  }

  public Timeout getTimeout() {
    return timeout;
  }
}
/* JavaCC - OriginalChecksum=093091d7273f1073ad49f2a2bf709a53 (do not edit this line) */
