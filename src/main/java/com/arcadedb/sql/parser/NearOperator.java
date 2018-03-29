/* Generated By:JJTree: Do not edit this line. ONearOperator.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

public class NearOperator extends SimpleNode implements BinaryCompareOperator {
  public NearOperator(int id) {
    super(id);
  }

  public NearOperator(SqlParser p, int id) {
    super(p, id);
  }

  /** Accept the visitor. **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public boolean execute(Object left, Object right) {
    throw new UnsupportedOperationException(toString() + " operator cannot be evaluated in this context");
  }

  @Override
  public String toString() {
    return "NEAR";
  }

  @Override public boolean supportsBasicCalculation() {
    return false;
  }

  @Override public NearOperator copy() {
    return this;
  }

  @Override public boolean equals(Object obj) {
    return obj != null && obj.getClass().equals(this.getClass());
  }

  @Override public int hashCode() {
    return getClass().hashCode();
  }
}
/* JavaCC - OriginalChecksum=a79af9beed70f813658f38a0162320e0 (do not edit this line) */
