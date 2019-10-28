/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OUpdateAddItem.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import java.util.Map;

public class UpdateAddItem extends SimpleNode {

  protected Identifier left;
  protected Expression right;

  public UpdateAddItem(int id) {
    super(id);
  }

  public UpdateAddItem(SqlParser p, int id) {
    super(p, id);
  }

  /** Accept the visitor. **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }


  public void toString(Map<Object, Object> params, StringBuilder builder) {
    left.toString(params, builder);
    builder.append(" = ");
    right.toString(params, builder);
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    UpdateAddItem that = (UpdateAddItem) o;

    if (left != null ? !left.equals(that.left) : that.left != null)
      return false;
    return right != null ? right.equals(that.right) : that.right == null;
  }

  @Override public int hashCode() {
    int result = left != null ? left.hashCode() : 0;
    result = 31 * result + (right != null ? right.hashCode() : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=769679aa2d2d8df58a13210152b50a9d (do not edit this line) */
