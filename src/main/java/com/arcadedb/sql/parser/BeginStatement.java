/* Generated By:JJTree: Do not edit this line. OBeginStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.OCommandContext;
import com.arcadedb.sql.executor.OInternalResultSet;
import com.arcadedb.sql.executor.OResultInternal;
import com.arcadedb.sql.executor.OResultSet;

import java.util.Map;

public class BeginStatement extends SimpleExecStatement {
  protected Identifier isolation;

  public BeginStatement(int id) {
    super(id);
  }

  public BeginStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override public OResultSet executeSimple(OCommandContext ctx) {
    ctx.getDatabase().begin();
    OInternalResultSet result = new OInternalResultSet();
    OResultInternal item = new OResultInternal();
    item.setProperty("operation", "begin");
//    if (isolation != null) {
//      ctx.getDatabase().getTransaction().setIsolationLevel(OTransaction.ISOLATION_LEVEL.valueOf(isolation.getStringValue()));
//      item.setProperty("isolation", isolation.getStringValue());
//    }
    result.add(item);
    return result;
  }

  @Override public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("BEGIN");
    if (isolation != null) {
      builder.append(" ISOLATION ");
      isolation.toString(params, builder);
    }
  }

  @Override public BeginStatement copy() {
    BeginStatement result = new BeginStatement(-1);
    result.isolation = isolation == null ? null : isolation.copy();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    BeginStatement that = (BeginStatement) o;

    if (isolation != null ? !isolation.equals(that.isolation) : that.isolation != null)
      return false;

    return true;
  }

  @Override public int hashCode() {
    return isolation != null ? isolation.hashCode() : 0;
  }
}
/* JavaCC - OriginalChecksum=aaa994acbe63cc4169fe33144d412fed (do not edit this line) */
