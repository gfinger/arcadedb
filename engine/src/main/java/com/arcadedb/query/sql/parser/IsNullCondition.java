/*
 * Copyright © 2021-present Arcade Data Ltd (info@arcadedata.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* Generated By:JJTree: Do not edit this line. OIsNullCondition.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Identifiable;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.Result;

import java.util.*;

public class IsNullCondition extends BooleanExpression {

  protected Expression expression;

  public IsNullCondition(int id) {
    super(id);
  }

  public IsNullCondition(SqlParser p, int id) {
    super(p, id);
  }

  @Override
  public boolean evaluate(Identifiable currentRecord, CommandContext ctx) {
    return expression.execute(currentRecord, ctx) == null;
  }

  @Override
  public boolean evaluate(Result currentRecord, CommandContext ctx) {
    return expression.execute(currentRecord, ctx) == null;
  }

  public Expression getExpression() {
    return expression;
  }

  public void setExpression(Expression expression) {
    this.expression = expression;
  }

  public void toString(Map<String, Object> params, StringBuilder builder) {
    expression.toString(params, builder);
    builder.append(" is null");
  }

  @Override
  public boolean supportsBasicCalculation() {
    return expression.supportsBasicCalculation();
  }

  @Override
  protected int getNumberOfExternalCalculations() {
    if (expression.supportsBasicCalculation()) {
      return 0;
    }
    return 1;
  }

  @Override
  protected List<Object> getExternalCalculationConditions() {
    if (expression.supportsBasicCalculation()) {
      return Collections.EMPTY_LIST;
    }
    return Collections.singletonList(expression);
  }

  @Override
  public boolean needsAliases(Set<String> aliases) {
    return expression.needsAliases(aliases);
  }

  @Override
  public IsNullCondition copy() {
    IsNullCondition result = new IsNullCondition(-1);
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

    IsNullCondition that = (IsNullCondition) o;

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
/* JavaCC - OriginalChecksum=29ebbc506a98f90953af91a66a03aa1e (do not edit this line) */
