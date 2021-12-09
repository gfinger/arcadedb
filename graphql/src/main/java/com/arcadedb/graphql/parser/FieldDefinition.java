/* Generated by: JJTree: Do not edit this line. FieldDefinition.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.graphql.parser;

public class FieldDefinition extends SimpleNode {

  protected Name                name;
  protected ArgumentsDefinition argumentsDefinition;
  protected Type       type;
  protected Directives directives;

  public FieldDefinition(int id) {
    super(id);
  }

  public FieldDefinition(GraphQLParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(GraphQLParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* ParserGeneratorCC - OriginalChecksum=6128650aa4801ab60df8105dc264845f (do not edit this line) */
