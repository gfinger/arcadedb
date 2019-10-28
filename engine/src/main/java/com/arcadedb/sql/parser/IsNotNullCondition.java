/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OIsNotNullCondition.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Identifiable;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.Result;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IsNotNullCondition extends BooleanExpression {

  protected Expression expression;

  public IsNotNullCondition(int id) {
    super(id);
  }

  public IsNotNullCondition(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }


  @Override public boolean evaluate(Identifiable currentRecord, CommandContext ctx) {
    return expression.execute(currentRecord, ctx) != null;
  }

  @Override public boolean evaluate(Result currentRecord, CommandContext ctx) {
    return expression.execute(currentRecord, ctx) != null;
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    expression.toString(params, builder);
    builder.append(" IS NOT NULL");
  }

  @Override public boolean supportsBasicCalculation() {
    return expression.supportsBasicCalculation();
  }

  @Override protected int getNumberOfExternalCalculations() {
    if (!expression.supportsBasicCalculation()) {
      return 1;
    }
    return 0;
  }

  @Override protected List<Object> getExternalCalculationConditions() {
    if (!expression.supportsBasicCalculation()) {
      return (List) Collections.singletonList(expression);
    }
    return Collections.EMPTY_LIST;
  }

  @Override public boolean needsAliases(Set<String> aliases) {
    return expression.needsAliases(aliases);
  }

  @Override public BooleanExpression copy() {
    IsNotNullCondition result = new IsNotNullCondition(-1);
    result.expression = expression.copy();
    return result;
  }

  @Override public void extractSubQueries(SubQueryCollector collector) {
    this.expression.extractSubQueries(collector);
  }

  @Override public boolean refersToParent() {
    return expression != null && expression.refersToParent();
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    IsNotNullCondition that = (IsNotNullCondition) o;

    return expression != null ? expression.equals(that.expression) : that.expression == null;
  }

  @Override public int hashCode() {
    return expression != null ? expression.hashCode() : 0;
  }

  @Override public List<String> getMatchPatternInvolvedAliases() {
    return expression.getMatchPatternInvolvedAliases();
  }

  @Override
  public boolean isCacheable() {
    return expression.isCacheable();
  }

}
/* JavaCC - OriginalChecksum=a292fa8a629abb7f6fe72a627fc91361 (do not edit this line) */
