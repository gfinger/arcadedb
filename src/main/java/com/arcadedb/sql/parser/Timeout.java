/* Generated By:JJTree: Do not edit this line. OTimeout.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.OResult;
import com.arcadedb.sql.executor.OResultInternal;

import java.util.Map;

public class Timeout extends SimpleNode {
  public static final String RETURN    = "RETURN";
  public static final String EXCEPTION = "EXCEPTION";

  protected Number val;
  protected String failureStrategy;

  public Timeout(int id) {
    super(id);
  }

  public Timeout(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append(" TIMEOUT " + val);
    if (failureStrategy != null) {
      builder.append(" ");
      builder.append(failureStrategy);
    }
  }

  public Timeout copy() {
    Timeout result = new Timeout(-1);
    result.val = val;
    result.failureStrategy = failureStrategy;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Timeout timeout = (Timeout) o;

    if (val != null ? !val.equals(timeout.val) : timeout.val != null)
      return false;
    if (failureStrategy != null ? !failureStrategy.equals(timeout.failureStrategy) : timeout.failureStrategy != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = val != null ? val.hashCode() : 0;
    result = 31 * result + (failureStrategy != null ? failureStrategy.hashCode() : 0);
    return result;
  }

  public Number getVal() {
    return val;
  }

  public String getFailureStrategy() {
    return failureStrategy;
  }

  public OResult serialize() {
    OResultInternal result = new OResultInternal();
    result.setProperty("val", val);
    result.setProperty("failureStrategy", failureStrategy);
    return result;
  }

  public void deserialize(OResult fromResult) {
    val = fromResult.getProperty("val");
    failureStrategy = fromResult.getProperty("failureStrategy");
  }
}
/* JavaCC - OriginalChecksum=fef7f5d488f7fca1b6ad0b70c6841931 (do not edit this line) */
