/* Generated By:JJTree: Do not edit this line. ONotInCondition.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.PIdentifiable;
import com.arcadedb.sql.executor.OCommandContext;
import com.arcadedb.sql.executor.OResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotInCondition extends BooleanExpression {

  protected Expression            left;
  protected BinaryCompareOperator operator;
  protected SelectStatement       rightStatement;

  protected Object         right;
  protected InputParameter rightParam;
  protected MathExpression rightMathExpression;

  private static final Object UNSET           = new Object();
  private              Object inputFinalValue = UNSET;

  public NotInCondition(int id) {
    super(id);
  }

  public NotInCondition(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public boolean evaluate(PIdentifiable currentRecord, OCommandContext ctx) {
    Object leftVal = left.execute(currentRecord, ctx);
    Object rightVal = null;
    if (rightStatement != null) {
      rightVal = InCondition.executeQuery(rightStatement, ctx);
    } else if (rightParam != null) {
      rightVal = rightParam.getValue(ctx.getInputParameters());
    } else if (rightMathExpression != null) {
      rightVal = rightMathExpression.execute(currentRecord, ctx);
    }
    if (rightVal == null) {
      return true;
    }
    return !InCondition.evaluateExpression(leftVal, rightVal);
  }

  @Override
  public boolean evaluate(OResult currentRecord, OCommandContext ctx) {
    Object leftVal = left.execute(currentRecord, ctx);
    Object rightVal = null;
    if (rightStatement != null) {
      rightVal = InCondition.executeQuery(rightStatement, ctx);
    } else if (rightParam != null) {
      rightVal = rightParam.getValue(ctx.getInputParameters());
    } else if (rightMathExpression != null) {
      rightVal = rightMathExpression.execute(currentRecord, ctx);
    }
    if (rightVal == null) {
      return true;
    }
    return !InCondition.evaluateExpression(leftVal, rightVal);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {

    left.toString(params, builder);
    builder.append(" NOT IN ");
    if (rightStatement != null) {
      builder.append("(");
      rightStatement.toString(params, builder);
      builder.append(")");
    } else if (right != null) {
      builder.append(convertToString(right));
    } else if (rightParam != null) {
      rightParam.toString(params, builder);
    } else if (rightMathExpression != null) {
      rightMathExpression.toString(params, builder);
    }
  }

  private String convertToString(Object o) {
    if (o instanceof String) {
      return "\"" + ((String) o).replaceAll("\"", "\\\"") + "\"";
    }
    return o.toString();
  }

  @Override
  public boolean supportsBasicCalculation() {

    if (operator != null && !operator.supportsBasicCalculation()) {
      return false;
    }
    if (left != null && !left.supportsBasicCalculation()) {
      return false;
    }
    if (rightMathExpression != null && !rightMathExpression.supportsBasicCalculation()) {
      return false;
    }
    return true;

  }

  @Override
  protected int getNumberOfExternalCalculations() {
    int total = 0;
    if (operator != null && !operator.supportsBasicCalculation()) {
      total++;
    }
    if (left != null && !left.supportsBasicCalculation()) {
      total++;
    }
    if (rightMathExpression != null && !rightMathExpression.supportsBasicCalculation()) {
      total++;
    }
    return total;
  }

  @Override
  protected List<Object> getExternalCalculationConditions() {
    List<Object> result = new ArrayList<Object>();
    if (operator != null && !operator.supportsBasicCalculation()) {
      result.add(this);
    }
    if (rightMathExpression != null && !rightMathExpression.supportsBasicCalculation()) {
      result.add(rightMathExpression);
    }
    return result;
  }

  @Override
  public boolean needsAliases(Set<String> aliases) {
    if (left.needsAliases(aliases)) {
      return true;
    }

    if (rightMathExpression != null && rightMathExpression.needsAliases(aliases)) {
      return true;
    }
    return false;
  }

  @Override
  public NotInCondition copy() {
    NotInCondition result = new NotInCondition(-1);
    result.operator = operator == null ? null : (BinaryCompareOperator) operator.copy();
    result.left = left == null ? null : left.copy();
    result.rightMathExpression = rightMathExpression == null ? null : rightMathExpression.copy();
    result.rightStatement = rightStatement == null ? null : rightStatement.copy();
    result.rightParam = rightParam == null ? null : rightParam.copy();
    result.right = right == null ? null : right;
    return result;
  }

  @Override
  public void extractSubQueries(SubQueryCollector collector) {
    if (left != null) {
      left.extractSubQueries(collector);
    }

    if (rightMathExpression != null) {
      rightMathExpression.extractSubQueries(collector);
    } else if (rightStatement != null) {
      Identifier alias = collector.addStatement(rightStatement);
      rightMathExpression = new BaseExpression(alias);
      rightStatement = null;
    }
  }

  @Override
  public boolean refersToParent() {
    if (left != null && left.refersToParent()) {
      return true;
    }
    if (rightStatement != null && rightStatement.refersToParent()) {
      return true;
    }
    if (rightMathExpression != null && rightMathExpression.refersToParent()) {
      return true;
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    NotInCondition that = (NotInCondition) o;

    if (left != null ? !left.equals(that.left) : that.left != null)
      return false;
    if (operator != null ? !operator.equals(that.operator) : that.operator != null)
      return false;
    if (rightStatement != null ? !rightStatement.equals(that.rightStatement) : that.rightStatement != null)
      return false;
    if (right != null ? !right.equals(that.right) : that.right != null)
      return false;
    if (rightParam != null ? !rightParam.equals(that.rightParam) : that.rightParam != null)
      return false;
    if (rightMathExpression != null ? !rightMathExpression.equals(that.rightMathExpression) : that.rightMathExpression != null)
      return false;
    if (inputFinalValue != null ? !inputFinalValue.equals(that.inputFinalValue) : that.inputFinalValue != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = left != null ? left.hashCode() : 0;
    result = 31 * result + (operator != null ? operator.hashCode() : 0);
    result = 31 * result + (rightStatement != null ? rightStatement.hashCode() : 0);
    result = 31 * result + (right != null ? right.hashCode() : 0);
    result = 31 * result + (rightParam != null ? rightParam.hashCode() : 0);
    result = 31 * result + (rightMathExpression != null ? rightMathExpression.hashCode() : 0);
    result = 31 * result + (inputFinalValue != null ? inputFinalValue.hashCode() : 0);
    return result;
  }

  @Override
  public List<String> getMatchPatternInvolvedAliases() {
    List<String> leftX = left == null ? null : left.getMatchPatternInvolvedAliases();
    List<String> rightX = rightMathExpression == null ? null : rightMathExpression.getMatchPatternInvolvedAliases();

    List<String> result = new ArrayList<String>();
    if (leftX != null) {
      result.addAll(leftX);
    }
    if (rightX != null) {
      result.addAll(rightX);
    }

    return result.size() == 0 ? null : result;
  }

  @Override
  public boolean isCacheable() {
    if (left != null && !left.isCacheable()) {
      return false;
    }

    if (rightStatement != null && !rightStatement.executinPlanCanBeCached()) {
      return false;
    }

    if (rightMathExpression != null && !rightMathExpression.isCacheable()) {
      return false;
    }
    return true;
  }

}
/* JavaCC - OriginalChecksum=8fb82bf72cc7d9cbdf2f9e2323ca8ee1 (do not edit this line) */
