/* Generated by: JJTree: Do not edit this line. Directives.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.graphql.parser;

import java.util.ArrayList;
import java.util.List;

public
class Directives extends SimpleNode {

  List<Directive> directives = new ArrayList<>();

  public Directives(int id) {
    super(id);
  }

  public Directives(GraphQLParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(GraphQLParserVisitor visitor, Object data) {
    return
    visitor.visit(this, data);
  }
}
/* ParserGeneratorCC - OriginalChecksum=f93ba7b3c8a0e407e3d07fabf22ecdf2 (do not edit this line) */
