/* Generated By:JJTree: Do not edit this line. ODeleteEdgeWhereStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

public
class DeleteEdgeWhereStatement extends DeleteEdgeStatement {
  public DeleteEdgeWhereStatement(int id) {
    super(id);
  }

  public DeleteEdgeWhereStatement(SqlParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override public DeleteEdgeStatement copy() {
    return super.copy();
  }
}
/* JavaCC - OriginalChecksum=1298a0baf9921378983d0722f8ebe68b (do not edit this line) */
