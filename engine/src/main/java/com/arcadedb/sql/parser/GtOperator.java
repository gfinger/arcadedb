/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OGtOperator.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.DatabaseInternal;
import com.arcadedb.database.Identifiable;
import com.arcadedb.schema.Type;

public class GtOperator extends SimpleNode implements BinaryCompareOperator {
  public GtOperator(int id) {
    super(id);
  }

  public GtOperator(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public boolean execute(final DatabaseInternal database, Object iLeft, Object iRight) {
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
    if (iLeft instanceof Identifiable && !(iRight instanceof Identifiable)) {
      return false;
    }
    if (!(iLeft instanceof Comparable)) {
      return false;
    }
    return ((Comparable<Object>) iLeft).compareTo(iRight) > 0;
  }

  @Override
  public String toString() {
    return ">";
  }

  @Override
  public boolean supportsBasicCalculation() {
    return true;
  }

  @Override
  public GtOperator copy() {
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
/* JavaCC - OriginalChecksum=4b96739fc6e9ae496916d542db361376 (do not edit this line) */
