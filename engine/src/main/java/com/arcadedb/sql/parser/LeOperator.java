/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OLeOperator.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.DatabaseInternal;
import com.arcadedb.schema.Type;

public class LeOperator extends SimpleNode implements BinaryCompareOperator {
  public LeOperator(int id) {
    super(id);
  }

  public LeOperator(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public boolean execute(DatabaseInternal database, Object iLeft, Object iRight) {
    if (iLeft == iRight) {
      return true;
    }
    if (iLeft == null || iRight == null) {
      return false;
    }

    if (iLeft.getClass() != iRight.getClass() && iLeft instanceof Number && iRight instanceof Number) {
      Number[] couple = Type.castComparableNumber((Number) iLeft, (Number) iRight);
      iLeft = couple[0];
      iRight = couple[1];
    } else {
      iRight = Type.convert(database, iRight, iLeft.getClass());
    }

    if (iRight == null)
      return false;
    return ((Comparable<Object>) iLeft).compareTo(iRight) <= 0;
  }

  @Override
  public String toString() {
    return "<=";
  }

  @Override
  public boolean supportsBasicCalculation() {
    return true;
  }

  @Override
  public LeOperator copy() {
    return this;
  }

  @Override
  public boolean isRangeOperator() {
    return true;
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj.getClass().equals(this.getClass());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
/* JavaCC - OriginalChecksum=8b3232c970fd654af947274a5f384a93 (do not edit this line) */
