/* Generated by: JJTree: Do not edit this line. DirectiveDefinition.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.graphql.parser;

public class DirectiveDefinition extends SimpleNode {

  protected Name                name;
  protected ArgumentsDefinition argumentsDefinition;
  protected DirectiveLocations  directiveLocations;

  public DirectiveDefinition(int id) {
    super(id);
  }

  public DirectiveDefinition(GraphQLParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(GraphQLParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* ParserGeneratorCC - OriginalChecksum=4b1cd90db8c14c7ef8c7de1a56fd24a0 (do not edit this line) */
