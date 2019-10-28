/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OLetStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.InternalResultSet;
import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultSet;

import java.util.Map;

public class LetStatement extends SimpleExecStatement {
  protected Identifier name;

  protected Statement  statement;
  protected Expression expression;

  public LetStatement(int id) {
    super(id);
  }

  public LetStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override
  public ResultSet executeSimple(CommandContext ctx) {
    Object result;
    if (expression != null) {
      result = expression.execute((Result) null, ctx);
    } else {
      Map<Object, Object> params = ctx.getInputParameters();
      result = statement.execute(ctx.getDatabase(), params, ctx);
    }
    if (result instanceof ResultSet) {
      InternalResultSet rs = new InternalResultSet();
      ((ResultSet) result).stream().forEach(x -> rs.add(x));
      rs.setPlan(((ResultSet) result).getExecutionPlan().orElse(null));
      ((ResultSet) result).close();
      result = rs;
    }

    if (ctx != null && ctx.getParent() != null) {
      ctx.getParent().setVariable(name.getStringValue(), result);
    }
    return new InternalResultSet();
  }

  @Override
  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("LET ");
    name.toString(params, builder);
    builder.append(" = ");
    if (statement != null) {
      statement.toString(params, builder);
    } else {
      expression.toString(params, builder);
    }
  }

  @Override
  public LetStatement copy() {
    LetStatement result = new LetStatement(-1);
    result.name = name == null ? null : name.copy();
    result.statement = statement == null ? null : statement.copy();
    result.expression = expression == null ? null : expression.copy();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    LetStatement that = (LetStatement) o;

    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;
    if (statement != null ? !statement.equals(that.statement) : that.statement != null)
      return false;
    return expression != null ? expression.equals(that.expression) : that.expression == null;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (statement != null ? statement.hashCode() : 0);
    result = 31 * result + (expression != null ? expression.hashCode() : 0);
    return result;
  }

  public Identifier getName() {
    return name;
  }
}
/* JavaCC - OriginalChecksum=cc646e5449351ad9ced844f61b687928 (do not edit this line) */
