/* Generated By:JJTree: Do not edit this line. OSuffixIdentifier.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.PIdentifiable;
import com.arcadedb.database.PModifiableDocument;
import com.arcadedb.database.PRecord;
import com.arcadedb.exception.PCommandExecutionException;
import com.arcadedb.sql.executor.*;

import java.util.*;

public class SuffixIdentifier extends SimpleNode {

  protected Identifier      identifier;
  protected RecordAttribute recordAttribute;
  protected boolean star = false;

  public SuffixIdentifier(int id) {
    super(id);
  }

  public SuffixIdentifier(SqlParser p, int id) {
    super(p, id);
  }

  public SuffixIdentifier(Identifier identifier) {
    this.identifier = identifier;
  }

  public SuffixIdentifier(RecordAttribute attr) {
    this.recordAttribute = attr;
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    if (identifier != null) {
      identifier.toString(params, builder);
    } else if (recordAttribute != null) {
      recordAttribute.toString(params, builder);
    } else if (star) {
      builder.append("*");
    }
  }

  public Object execute(PIdentifiable iCurrentRecord, OCommandContext ctx) {
    if (star) {
      return iCurrentRecord;
    }
    if (identifier != null) {
      String varName = identifier.getStringValue();
      if (ctx != null && ctx.getVariable(varName) != null) {
        return ctx.getVariable(varName);
      }

      if (iCurrentRecord != null) {
        return ((PRecord) iCurrentRecord.getRecord()).get(varName);
      }
      return null;
    }
    if (recordAttribute != null) {
      return ((PRecord) iCurrentRecord.getRecord()).get(recordAttribute.name);
    }
    return null;
  }

  public Object execute(OResult iCurrentRecord, OCommandContext ctx) {
    if (star) {
      return iCurrentRecord;
    }
    if (identifier != null) {
      String varName = identifier.getStringValue();
      if (ctx != null && varName.equalsIgnoreCase("$parent")) {
        return ctx.getParent();
      }
      if (ctx != null && ctx.getVariable(varName) != null) {
        return ctx.getVariable(varName);
      }
      if (iCurrentRecord != null) {
        if (iCurrentRecord.hasProperty(varName)) {
          return iCurrentRecord.getProperty(varName);
        }
        if (iCurrentRecord.getMetadataKeys().contains(varName)) {
          return iCurrentRecord.getMetadata(varName);
        }
      }
      return null;
    }

    if (iCurrentRecord != null && recordAttribute != null) {
      return recordAttribute.evaluate(iCurrentRecord, ctx);
    }

    return null;
  }

  public Object execute(Map iCurrentRecord, OCommandContext ctx) {
    if (star) {
      OResultInternal result = new OResultInternal();
      if (iCurrentRecord != null) {
        for (Map.Entry<Object, Object> x : ((Map<Object, Object>) iCurrentRecord).entrySet()) {
          result.setProperty("" + x.getKey(), x.getValue());
        }
        return result;
      }
      return iCurrentRecord;
    }
    if (identifier != null) {
      String varName = identifier.getStringValue();
      if (ctx != null && varName.equalsIgnoreCase("$parent")) {
        return ctx.getParent();
      }
      if (ctx != null && ctx.getVariable(varName) != null) {
        return ctx.getVariable(varName);
      }
      if (iCurrentRecord != null) {
        return iCurrentRecord.get(varName);
      }
      return null;
    }
    if (recordAttribute != null) {
      return iCurrentRecord.get(recordAttribute.name);
    }
    return null;
  }

  public Object execute(Iterable iterable, OCommandContext ctx) {
    if (star) {
      return null;
    }
    List<Object> result = new ArrayList<>();
    for (Object o : iterable) {
      result.add(execute(o, ctx));
    }
    return result;
  }

  public Object execute(Iterator iterator, OCommandContext ctx) {
    if (star) {
      return null;
    }
    List<Object> result = new ArrayList<>();
    while (iterator.hasNext()) {
      result.add(execute(iterator.next(), ctx));
    }
    if (iterator instanceof OResultSet) {
      try {
        ((OResultSet) iterator).reset();
      } catch (Exception ignore) {
      }
    }
    return result;
  }

  public Object execute(OCommandContext iCurrentRecord) {
    if (star) {
      return null;
    }
    if (identifier != null) {
      String varName = identifier.getStringValue();
      if (iCurrentRecord != null) {
        return iCurrentRecord.getVariable(varName);
      }
      return null;
    }
    if (recordAttribute != null && iCurrentRecord != null) {
      return iCurrentRecord.getVariable(recordAttribute.name);
    }
    return null;
  }

  public Object execute(Object currentValue, OCommandContext ctx) {
    if (currentValue instanceof OResult) {
      return execute((OResult) currentValue, ctx);
    }
    if (currentValue instanceof PIdentifiable) {
      return execute((PIdentifiable) currentValue, ctx);
    }
    if (currentValue instanceof Map) {
      return execute((Map) currentValue, ctx);
    }
    if (currentValue instanceof OCommandContext) {
      return execute((OCommandContext) currentValue);
    }
    if (currentValue instanceof Iterable) {
      return execute((Iterable) currentValue, ctx);
    }
    if (currentValue instanceof Iterator) {
      return execute((Iterator) currentValue, ctx);
    }
    if (currentValue == null) {
      return execute((OResult) null, ctx);
    }

    return null;
    // TODO other cases?
  }

  public boolean isBaseIdentifier() {
    return identifier != null;
  }

  public boolean needsAliases(Set<String> aliases) {
    if (identifier != null) {
      return aliases.contains(identifier.getStringValue());
    }
    if (recordAttribute != null) {
      for (String s : aliases) {
        if (s.equalsIgnoreCase(recordAttribute.name)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isAggregate() {
    return false;
  }

  public boolean isCount() {
    return false;
  }

  public SuffixIdentifier splitForAggregation(AggregateProjectionSplit aggregateProj) {
    return this;
  }

  public boolean isEarlyCalculated() {
    if (identifier != null && identifier.internalAlias) {
      return true;
    }
    return false;
  }

  public void aggregate(Object value, OCommandContext ctx) {
    throw new UnsupportedOperationException("this operation does not support plain aggregation: " + toString());
  }

  public AggregationContext getAggregationContext(OCommandContext ctx) {
    throw new UnsupportedOperationException("this operation does not support plain aggregation: " + toString());
  }

  public SuffixIdentifier copy() {
    SuffixIdentifier result = new SuffixIdentifier(-1);
    result.identifier = identifier == null ? null : identifier.copy();
    result.recordAttribute = recordAttribute == null ? null : recordAttribute.copy();
    result.star = star;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    SuffixIdentifier that = (SuffixIdentifier) o;

    if (star != that.star)
      return false;
    if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null)
      return false;
    if (recordAttribute != null ? !recordAttribute.equals(that.recordAttribute) : that.recordAttribute != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = identifier != null ? identifier.hashCode() : 0;
    result = 31 * result + (recordAttribute != null ? recordAttribute.hashCode() : 0);
    result = 31 * result + (star ? 1 : 0);
    return result;
  }

  public void extractSubQueries(SubQueryCollector collector) {

  }

  public boolean refersToParent() {
    if (identifier != null && identifier.getStringValue().equalsIgnoreCase("$parent")) {
      return true;
    }
    return false;
  }

  public void setValue(Object target, Object value, OCommandContext ctx) {
    if (target instanceof OResult) {
      setValue((OResult) target, value, ctx);
    } else if (target instanceof PIdentifiable) {
      setValue((PIdentifiable) target, value, ctx);
    } else if (target instanceof Map) {
      setValue((Map) target, value, ctx);
    }
  }

  public void setValue(PIdentifiable target, Object value, OCommandContext ctx) {
    if (target == null) {
      return;
    }
    PModifiableDocument doc = null;
    if (target instanceof PModifiableDocument) {
      doc = (PModifiableDocument) target;
    } else {
      PRecord rec = target.getRecord();
      if (rec instanceof PRecord) {
        doc = (PModifiableDocument) rec;
      }
    }
    if (doc != null) {
      doc.set(identifier.getStringValue(), value);
    } else {
      throw new PCommandExecutionException("Cannot set record attribute " + recordAttribute + " on existing document");
    }
  }

  public void setValue(Map target, Object value, OCommandContext ctx) {
    if (target == null) {
      return;
    }
    if (identifier != null) {
      target.put(identifier.getStringValue(), value);
    } else if (recordAttribute != null) {
      target.put(recordAttribute.getName(), value);
    }
  }

  public void setValue(OResult target, Object value, OCommandContext ctx) {
    if (target == null) {
      return;
    }
    if (target instanceof OResultInternal) {
      OResultInternal intTarget = (OResultInternal) target;
      if (identifier != null) {
        intTarget.setProperty(identifier.getStringValue(), value);
      } else if (recordAttribute != null) {
        intTarget.setProperty(recordAttribute.getName(), value);
      }
    } else {
      throw new PCommandExecutionException("Cannot set property on unmodifiable target: " + target);
    }
  }

  public void applyRemove(Object currentValue, OCommandContext ctx) {
    if (currentValue == null) {
      return;
    }
    if (identifier != null) {
      if (currentValue instanceof OResultInternal) {
        ((OResultInternal) currentValue).removeProperty(identifier.getStringValue());
      } else if (currentValue instanceof PModifiableDocument) {
        ((PModifiableDocument) currentValue).set(identifier.getStringValue(), null);
      } else if (currentValue instanceof Map) {
        ((Map) currentValue).remove(identifier.getStringValue());
      }
    }
  }

  public OResult serialize() {
    OResultInternal result = new OResultInternal();
    if (identifier != null) {
      result.setProperty("identifier", identifier.serialize());
    }
    if (recordAttribute != null) {
      result.setProperty("recordAttribute", recordAttribute.serialize());
    }
    result.setProperty("star", star);
    return result;
  }

  public void deserialize(OResult fromResult) {
    if (fromResult.getProperty("identifier") != null) {
      identifier = new Identifier(-1);
      identifier.deserialize(fromResult.getProperty("identifier"));
    }
    if (fromResult.getProperty("recordAttribute") != null) {
      recordAttribute = new RecordAttribute(-1);
      recordAttribute.deserialize(fromResult.getProperty("recordAttribute"));
    }
    star = fromResult.getProperty("star");
  }

  public boolean isDefinedFor(OResult currentRecord) {
    if (identifier != null) {
      return currentRecord.hasProperty(identifier.getStringValue());
    }
    return true;
  }

  public boolean isDefinedFor(PRecord currentRecord) {
    if (identifier != null) {
      return ((PRecord) currentRecord.getRecord()).getPropertyNames().contains(identifier.getStringValue());
    }
    return true;
  }

  public OCollate getCollate(OResult currentRecord, OCommandContext ctx) {
//    if (identifier != null) {
//      return currentRecord.getRecord().map(x -> (PRecord) x).ap(elem -> elem.getType())
//          .map(clazz -> clazz.getProperty(identifier.getStringValue())).map(prop -> prop.getCollate()).orElse(null);
//    }
    return null;
  }

  public boolean isCacheable() {
    return true;
  }
}
/* JavaCC - OriginalChecksum=5d9be0188c7d6e2b67d691fb88a518f8 (do not edit this line) */
