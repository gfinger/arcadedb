/* Generated by: JJTree: Do not edit this line. Url.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import java.util.Map;
import java.util.Objects;

public
class Url extends SimpleNode {

  protected String urlString;

  public Url(int id) {
    super(id);
  }

  public Url(SqlParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return
    visitor.visit(this, data);
  }

  @Override
  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append(urlString);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Url url = (Url) o;
    return Objects.equals(urlString, url.urlString);
  }

  @Override
  public int hashCode() {
    return Objects.hash(urlString);
  }

  @Override
  public SimpleNode copy() {
    Url result = new Url(-1);
    result.urlString = urlString;
    return result;
  }

  public String getUrlString() {
    return urlString;
  }
}
/* ParserGeneratorCC - OriginalChecksum=1c71d71eb1a1a5f32261d88739a61629 (do not edit this line) */