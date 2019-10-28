/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OGroupBy.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultInternal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Unwind extends SimpleNode {

  protected List<Identifier> items = new ArrayList<Identifier>();

  public Unwind(int id) {
    super(id);
  }

  public Unwind(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("UNWIND ");
    for (int i = 0; i < items.size(); i++) {
      if (i > 0) {
        builder.append(", ");
      }
      items.get(i).toString(params, builder);
    }
  }

  public Unwind copy() {
    Unwind result = new Unwind(-1);
    result.items = items.stream().map(x -> x.copy()).collect(Collectors.toList());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Unwind oUnwind = (Unwind) o;

    return items != null ? items.equals(oUnwind.items) : oUnwind.items == null;
  }

  @Override
  public int hashCode() {
    return items != null ? items.hashCode() : 0;
  }

  public List<Identifier> getItems() {
    return items;
  }

  public Result serialize() {
    ResultInternal result = new ResultInternal();
    if (items != null) {
      result.setProperty("items", items.stream().map(x -> x.serialize()).collect(Collectors.toList()));
    }
    return result;
  }

  public void deserialize(Result fromResult) {
    if (fromResult.getProperty("items") != null) {
      List<Result> ser = fromResult.getProperty("items");
      items = new ArrayList<>();
      for (Result r : ser) {
        Identifier exp = new Identifier(-1);
        Identifier.deserialize(r);
        items.add(exp);
      }
    }
  }
}
/* JavaCC - OriginalChecksum=4739190aa6c1a3533a89b76a15bd6fdf (do not edit this line) */
