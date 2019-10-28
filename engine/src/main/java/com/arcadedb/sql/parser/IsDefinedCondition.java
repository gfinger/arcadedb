/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OIsDefinedCondition.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Identifiable;
import com.arcadedb.database.Record;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.Result;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IsDefinedCondition extends BooleanExpression implements SimpleBooleanExpression {

  protected Expression expression;

  public IsDefinedCondition(int id) {
    super(id);
  }

  public IsDefinedCondition(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public boolean evaluate(Identifiable currentRecord, CommandContext ctx) {
    Object elem = currentRecord.getRecord();
    if (elem instanceof Record) {
      return expression.isDefinedFor((Record) elem);
    }
    return false;
  }

  @Override
  public boolean evaluate(Result currentRecord, CommandContext ctx) {
    return expression.isDefinedFor(currentRecord);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    expression.toString(params, builder);
    builder.append(" is defined");
  }

  @Override
  public boolean supportsBasicCalculation() {
    return true;
  }

  @Override
  protected int getNumberOfExternalCalculations() {
    return 0;
  }

  @Override
  protected List<Object> getExternalCalculationConditions() {
    return Collections.EMPTY_LIST;
  }

  @Override
  public boolean needsAliases(Set<String> aliases) {
    return expression.needsAliases(aliases);
  }

  @Override
  public IsDefinedCondition copy() {
    IsDefinedCondition result = new IsDefinedCondition(-1);
    result.expression = expression.copy();
    return result;
  }

  @Override
  public void extractSubQueries(SubQueryCollector collector) {
    this.expression.extractSubQueries(collector);
  }

  @Override
  public boolean refersToParent() {
    return expression != null && expression.refersToParent();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    IsDefinedCondition that = (IsDefinedCondition) o;

    return expression != null ? expression.equals(that.expression) : that.expression == null;
  }

  @Override
  public int hashCode() {
    return expression != null ? expression.hashCode() : 0;
  }

  @Override
  public List<String> getMatchPatternInvolvedAliases() {
    return expression.getMatchPatternInvolvedAliases();
  }

  @Override
  public boolean isCacheable() {
    return expression.isCacheable();
  }
}
/* JavaCC - OriginalChecksum=075954b212c8cb44c8538bf5dea047d3 (do not edit this line) */
