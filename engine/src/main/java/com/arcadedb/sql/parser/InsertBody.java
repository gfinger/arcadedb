/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OInsertBody.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InsertBody extends SimpleNode {

  protected List<Identifier>          identifierList;
  protected List<List<Expression>>    valueExpressions;
  protected List<InsertSetExpression> setExpressions;

  protected Json content;


  public InsertBody(int id) {
    super(id);
  }

  public InsertBody(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {

    if (identifierList != null) {
      builder.append("(");
      boolean first = true;
      for (Identifier item : identifierList) {
        if (!first) {
          builder.append(", ");
        }
        item.toString(params, builder);
        first = false;
      }
      builder.append(") VALUES ");
      if (valueExpressions != null) {
        boolean firstList = true;
        for (List<Expression> itemList : valueExpressions) {
          if (firstList) {
            builder.append("(");
          } else {
            builder.append("),(");
          }
          first = true;
          for (Expression item : itemList) {
            if (!first) {
              builder.append(", ");
            }
            item.toString(params, builder);
            first = false;
          }
          firstList = false;
        }
      }
      builder.append(")");

    }

    if (setExpressions != null) {
      builder.append("SET ");
      boolean first = true;
      for (InsertSetExpression item : setExpressions) {
        if (!first) {
          builder.append(", ");
        }
        item.toString(params, builder);
        first = false;
      }
    }

    if (content != null) {
      builder.append("CONTENT ");
      content.toString(params, builder);
    }

  }

  public InsertBody copy() {
    InsertBody result = new InsertBody(-1);
    result.identifierList = identifierList == null ? null : identifierList.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.valueExpressions = valueExpressions == null ?
        null :
        valueExpressions.stream().map(sub -> sub.stream().map(x -> x.copy()).collect(Collectors.toList()))
            .collect(Collectors.toList());
    result.setExpressions = setExpressions == null ? null : setExpressions.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.content = content == null ? null : content.copy();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    InsertBody that = (InsertBody) o;

    if (identifierList != null ? !identifierList.equals(that.identifierList) : that.identifierList != null)
      return false;
    if (valueExpressions != null ? !valueExpressions.equals(that.valueExpressions) : that.valueExpressions != null)
      return false;
    if (setExpressions != null ? !setExpressions.equals(that.setExpressions) : that.setExpressions != null)
      return false;
    return content != null ? content.equals(that.content) : that.content == null;
  }

  @Override public int hashCode() {
    int result = identifierList != null ? identifierList.hashCode() : 0;
    result = 31 * result + (valueExpressions != null ? valueExpressions.hashCode() : 0);
    result = 31 * result + (setExpressions != null ? setExpressions.hashCode() : 0);
    result = 31 * result + (content != null ? content.hashCode() : 0);
    return result;
  }

  public List<Identifier> getIdentifierList() {
    return identifierList;
  }

  public List<List<Expression>> getValueExpressions() {
    return valueExpressions;
  }

  public List<InsertSetExpression> getSetExpressions() {
    return setExpressions;
  }

  public Json getContent() {
    return content;
  }
}
/* JavaCC - OriginalChecksum=7d2079a41a1fc63a812cb679e729b23a (do not edit this line) */
