/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OMatchPathItem.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Document;
import com.arcadedb.database.Identifiable;
import com.arcadedb.database.Record;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.sql.executor.CommandContext;

import java.util.*;

public class MatchPathItem extends SimpleNode {
  protected MethodCall  method;
  protected MatchFilter filter;

  public MatchPathItem(int id) {
    super(id);
  }

  public MatchPathItem(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public boolean isBidirectional() {
    if (filter.getWhileCondition() != null) {
      return false;
    }
    if (filter.getMaxDepth() != null) {
      return false;
    }
    if (filter.isOptional()) {
      return false;
    }
    return method.isBidirectional();
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    method.toString(params, builder);
    if (filter != null) {
      filter.toString(params, builder);
    }
  }

  public Iterable<Identifiable> executeTraversal(MatchStatement.MatchContext matchContext, CommandContext iCommandContext,
      Identifiable startingPoint, int depth) {

    WhereClause filter = null;
    WhereClause whileCondition = null;
    Integer maxDepth = null;
    DocumentType oClass = null;
    if (this.filter != null) {
      filter = this.filter.getFilter();
      whileCondition = this.filter.getWhileCondition();
      maxDepth = this.filter.getMaxDepth();
      String className = this.filter.getTypeName(iCommandContext);
      oClass = iCommandContext.getDatabase().getSchema().getType(className);
    }

    Set<Identifiable> result = new HashSet<Identifiable>();

    if (whileCondition == null && maxDepth == null) {// in this case starting point is not returned and only one level depth is
      // evaluated
      Iterable<Identifiable> queryResult = traversePatternEdge(matchContext, startingPoint, iCommandContext);

      if (this.filter == null || this.filter.getFilter() == null) {
        return queryResult;
      }

      for (Identifiable origin : queryResult) {
        Object previousMatch = iCommandContext.getVariable("$currentMatch");
        iCommandContext.setVariable("$currentMatch", origin);
        if ((oClass == null || matchesClass(origin, oClass)) && (filter == null || filter
            .matchesFilters(origin, iCommandContext))) {
          result.add(origin);
        }
        iCommandContext.setVariable("$currentMatch", previousMatch);
      }
    } else {// in this case also zero level (starting point) is considered and traversal depth is given by the while condition
      iCommandContext.setVariable("$depth", depth);
      Object previousMatch = iCommandContext.getVariable("$currentMatch");
      iCommandContext.setVariable("$currentMatch", startingPoint);
      if ((oClass == null || matchesClass(startingPoint, oClass)) && (filter == null || filter
          .matchesFilters(startingPoint, iCommandContext))) {
        result.add(startingPoint);
      }

      if ((maxDepth == null || depth < maxDepth) && (whileCondition == null || whileCondition
          .matchesFilters(startingPoint, iCommandContext))) {

        Iterable<Identifiable> queryResult = traversePatternEdge(matchContext, startingPoint, iCommandContext);

        for (Identifiable origin : queryResult) {
          // TODO consider break strategies (eg. re-traverse nodes)
          Iterable<Identifiable> subResult = executeTraversal(matchContext, iCommandContext, origin, depth + 1);
          if (subResult instanceof java.util.Collection) {
            result.addAll((java.util.Collection<? extends Identifiable>) subResult);
          } else {
            for (Identifiable i : subResult) {
              result.add(i);
            }
          }
        }
      }
      iCommandContext.setVariable("$currentMatch", previousMatch);
    }
    return result;
  }

  private boolean matchesClass(Identifiable identifiable, DocumentType oClass) {
    if (identifiable == null) {
      return false;
    }
    Record record = identifiable.getRecord();
    if (record == null) {
      return false;
    }

    return ((Document) record).getType().equals(oClass);
  }

  protected Iterable<Identifiable> traversePatternEdge(MatchStatement.MatchContext matchContext, Identifiable startingPoint,
      CommandContext iCommandContext) {

    Iterable possibleResults = null;
    if (filter != null) {
      Identifiable matchedNode = matchContext.matched.get(filter.getAlias());
      if (matchedNode != null) {
        possibleResults = Collections.singleton(matchedNode);
      } else if (matchContext.matched.containsKey(filter.getAlias())) {
        possibleResults = Collections.emptySet();//optional node, the matched element is a null value
      } else {
        possibleResults = matchContext.candidates == null ? null : matchContext.candidates.get(filter.getAlias());
      }
    }

    Object qR = this.method.execute(startingPoint, possibleResults, iCommandContext);
    return (qR instanceof Iterable && !(qR instanceof Record)) ? (Iterable) qR : Collections.singleton((Identifiable) qR);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    MatchPathItem that = (MatchPathItem) o;

    if (method != null ? !method.equals(that.method) : that.method != null)
      return false;
    return filter != null ? filter.equals(that.filter) : that.filter == null;
  }

  @Override
  public int hashCode() {
    int result = method != null ? method.hashCode() : 0;
    result = 31 * result + (filter != null ? filter.hashCode() : 0);
    return result;
  }

  @Override
  public MatchPathItem copy() {
    MatchPathItem result = null;
    try {
      result = getClass().getConstructor(Integer.TYPE).newInstance(-1);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    result.method = method == null ? null : method.copy();
    result.filter = filter == null ? null : filter.copy();
    return result;
  }

  public MethodCall getMethod() {
    return method;
  }

  public void setMethod(MethodCall method) {
    this.method = method;
  }

  public MatchFilter getFilter() {
    return filter;
  }

  public void setFilter(MatchFilter filter) {
    this.filter = filter;
  }
}
/* JavaCC - OriginalChecksum=ffe8e0ffde583d7b21c9084eff6a8944 (do not edit this line) */
