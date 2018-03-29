/* Generated By:JJTree: Do not edit this line. OMatchExpression.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MatchExpression extends SimpleNode {
  protected MatchFilter origin;
  protected List<MatchPathItem> items = new ArrayList<MatchPathItem>();

  public MatchExpression(int id) {
    super(id);
  }

  public MatchExpression(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    origin.toString(params, builder);
    for (MatchPathItem item : items) {
      item.toString(params, builder);
    }
  }

  @Override public MatchExpression copy() {
    MatchExpression result = new MatchExpression(-1);
    result.origin = origin == null ? null : origin.copy();
    result.items = items == null ? null : items.stream().map(x -> x.copy()).collect(Collectors.toList());
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    MatchExpression that = (MatchExpression) o;

    if (origin != null ? !origin.equals(that.origin) : that.origin != null)
      return false;
    if (items != null ? !items.equals(that.items) : that.items != null)
      return false;

    return true;
  }

  @Override public int hashCode() {
    int result = origin != null ? origin.hashCode() : 0;
    result = 31 * result + (items != null ? items.hashCode() : 0);
    return result;
  }

  public MatchFilter getOrigin() {
    return origin;
  }

  public void setOrigin(MatchFilter origin) {
    this.origin = origin;
  }

  public List<MatchPathItem> getItems() {
    return items;
  }

  public void setItems(List<MatchPathItem> items) {
    this.items = items;
  }
}
/* JavaCC - OriginalChecksum=73491fb653c32baf66997290db29f370 (do not edit this line) */
