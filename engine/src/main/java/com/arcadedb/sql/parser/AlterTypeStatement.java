/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OAlterClassStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.ResultSet;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AlterTypeStatement extends ODDLStatement {

  /**
   * the name of the class
   */
  protected Identifier name;
  /**
   * the class property to be altered
   */
  public    Object     property;

  protected Identifier       identifierValue;
  protected List<Identifier> identifierListValue;
  protected Boolean          add;
  protected Boolean          remove;
  protected PNumber          numberValue;
  protected Boolean          booleanValue;
  public    Identifier       customKey;
  public    Expression       customValue;

  protected PInteger   defaultBucketId;
  protected Identifier defaultBucketName;

  // only to manage 'round-robin' as a bucket selection strategy (not a valid identifier)
  protected String customString;

  protected boolean unsafe;

  public AlterTypeStatement(int id) {
    super(id);
  }

  public AlterTypeStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override
  public void toString(Map<Object, Object> params, StringBuilder builder) {
    throw new UnsupportedOperationException();
//    builder.append("ALTER USERTYPE ");
//    name.toString(params, builder);
//    if (property != null) {
//      builder.append(" " + property.name() + " ");
//      switch (property) {
//      case NAME:
//      case SHORTNAME:
//      case ADDCLUSTER:
//      case REMOVECLUSTER:
//      case DESCRIPTION:
//      case ENCRYPTION:
//        if (numberValue != null) {
//          numberValue.toString(params, builder);//clusters only
//        } else if (identifierValue != null) {
//          identifierValue.toString(params, builder);
//        } else {
//          builder.append("null");
//        }
//        break;
//      case CLUSTERSELECTION:
//        if (identifierValue != null) {
//          identifierValue.toString(params, builder);
//        } else if (customString != null) {
//          builder.append('\'').append(customString).append('\'');
//        } else {
//          builder.append("null");
//        }
//        break;
//      case SUPERUSERTYPE:
//        if (Boolean.TRUE.equals(add)) {
//          builder.append("+");
//        } else if (Boolean.TRUE.equals(remove)) {
//          builder.append("-");
//        }
//        if (identifierValue == null) {
//          builder.append("null");
//        } else {
//          identifierValue.toString(params, builder);
//        }
//        break;
//      case SUPERUSERTYPEES:
//        if (identifierListValue == null) {
//          builder.append("null");
//        } else {
//          boolean first = true;
//          for (OIdentifier ident : identifierListValue) {
//            if (!first) {
//              builder.append(", ");
//            }
//            ident.toString(params, builder);
//            first = false;
//          }
//        }
//        break;
//      case OVERSIZE:
//        numberValue.toString(params, builder);
//        break;
//      case STRICTMODE:
//      case ABSTRACT:
//        builder.append(booleanValue.booleanValue());
//        break;
//      case CUSTOM:
//        customKey.toString(params, builder);
//        builder.append("=");
//        if (customValue == null) {
//          builder.append("null");
//        } else {
//          customValue.toString(params, builder);
//        }
//        break;
//      }
//    } else if (defaultClusterId != null) {
//      builder.append(" DEFAULTCLUSTER ");
//      defaultClusterId.toString(params, builder);
//    } else if (defaultClusterName != null) {
//      builder.append(" DEFAULTCLUSTER ");
//      defaultClusterName.toString(params, builder);
//    }
//    if (unsafe) {
//      builder.append(" UNSAFE");
//    }
  }

  public Statement copy() {
    AlterTypeStatement result = new AlterTypeStatement(-1);
    result.name = name == null ? null : name.copy();
    result.property = property;
    result.identifierValue = identifierValue == null ? null : identifierValue.copy();
    result.identifierListValue =
        identifierListValue == null ? null : identifierListValue.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.add = add;
    result.remove = remove;
    result.numberValue = numberValue == null ? null : numberValue.copy();
    result.booleanValue = booleanValue;
    result.customKey = customKey == null ? null : customKey.copy();
    result.customValue = customValue == null ? null : customValue.copy();
    result.customString = customString;
    result.defaultBucketId = defaultBucketId == null ? null : defaultBucketId.copy();
    result.defaultBucketName = defaultBucketName == null ? null : defaultBucketName.copy();
    result.unsafe = unsafe;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final AlterTypeStatement that = (AlterTypeStatement) o;

    if (unsafe != that.unsafe)
      return false;
    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;
    if (property != that.property)
      return false;
    if (identifierValue != null ? !identifierValue.equals(that.identifierValue) : that.identifierValue != null)
      return false;
    if (identifierListValue != null ? !identifierListValue.equals(that.identifierListValue) : that.identifierListValue != null)
      return false;
    if (add != null ? !add.equals(that.add) : that.add != null)
      return false;
    if (remove != null ? !remove.equals(that.remove) : that.remove != null)
      return false;
    if (numberValue != null ? !numberValue.equals(that.numberValue) : that.numberValue != null)
      return false;
    if (booleanValue != null ? !booleanValue.equals(that.booleanValue) : that.booleanValue != null)
      return false;
    if (customKey != null ? !customKey.equals(that.customKey) : that.customKey != null)
      return false;
    if (customValue != null ? !customValue.equals(that.customValue) : that.customValue != null)
      return false;
    if (defaultBucketId != null ? !defaultBucketId.equals(that.defaultBucketId) : that.defaultBucketId != null)
      return false;
    if (defaultBucketName != null ? !defaultBucketName.equals(that.defaultBucketName) : that.defaultBucketName != null)
      return false;
    return customString != null ? customString.equals(that.customString) : that.customString == null;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (property != null ? property.hashCode() : 0);
    result = 31 * result + (identifierValue != null ? identifierValue.hashCode() : 0);
    result = 31 * result + (identifierListValue != null ? identifierListValue.hashCode() : 0);
    result = 31 * result + (add != null ? add.hashCode() : 0);
    result = 31 * result + (remove != null ? remove.hashCode() : 0);
    result = 31 * result + (numberValue != null ? numberValue.hashCode() : 0);
    result = 31 * result + (booleanValue != null ? booleanValue.hashCode() : 0);
    result = 31 * result + (customKey != null ? customKey.hashCode() : 0);
    result = 31 * result + (customValue != null ? customValue.hashCode() : 0);
    result = 31 * result + (defaultBucketId != null ? defaultBucketId.hashCode() : 0);
    result = 31 * result + (defaultBucketName != null ? defaultBucketName.hashCode() : 0);
    result = 31 * result + (customString != null ? customString.hashCode() : 0);
    result = 31 * result + (unsafe ? 1 : 0);
    return result;
  }

  @Override
  public ResultSet executeDDL(CommandContext ctx) {
    DocumentType oClass = ctx.getDatabase().getSchema().getType(name.getStringValue());
    if (oClass == null) {
      throw new CommandExecutionException("Type not found: " + name);
    }
//    if (property != null) {
//      switch (property) {
//      case NAME:
//        if (!unsafe) {
//          checkNotEdge(oClass);
//          checkNotIndexed(oClass);
//        }
//        try {
//          oClass.setName(identifierValue.getStringValue());
//        } catch (Exception e) {
//          OException x = OException.wrapException(new PCommandExecutionException("Invalid class name: " + toString()), e);
//          throw x;
//        }
//        break;
//      case SHORTNAME:
//        if (identifierValue != null) {
//          try {
//            oClass.setShortName(identifierValue.getStringValue());
//          } catch (Exception e) {
//            OException x = OException.wrapException(new PCommandExecutionException("Invalid class name: " + toString()), e);
//            throw x;
//          }
//        } else {
//          throw new PCommandExecutionException("Invalid class name: " + toString());
//        }
//        break;
//      case ADDCLUSTER:
//        if (identifierValue != null) {
//          oClass.addCluster(identifierValue.getStringValue());
//        } else if (numberValue != null) {
//          oClass.addClusterId(numberValue.getValue().intValue());
//        } else {
//          throw new PCommandExecutionException("Invalid bucket value: " + toString());
//        }
//        break;
//      case REMOVECLUSTER:
//        int bucketId = -1;
//        if (identifierValue != null) {
//          bucketId = ctx.getDatabase().getClusterIdByName(identifierValue.getStringValue());
//          if (bucketId < 0) {
//            throw new PCommandExecutionException("Cluster not found: " + toString());
//          }
//        } else if (numberValue != null) {
//          bucketId = numberValue.getValue().intValue();
//        } else {
//          throw new PCommandExecutionException("Invalid bucket value: " + toString());
//        }
//        oClass.removeClusterId(bucketId);
//        break;
//      case DESCRIPTION:
//        if (identifierValue != null) {
//          oClass.setDescription(identifierValue.getStringValue());
//        } else {
//          throw new PCommandExecutionException("Invalid class name: " + toString());
//        }
//        break;
//      case ENCRYPTION:
//        //TODO
//
//        break;
//      case CLUSTERSELECTION:
//        if (identifierValue != null) {
//          oClass.setClusterSelection(identifierValue.getStringValue());
//        } else if (customString != null) {
//          oClass.setClusterSelection(customString);
//        } else {
//          oClass.setClusterSelection("null");
//        }
//        break;
//      case SUPERUSERTYPE:
//        doSetSuperclass(ctx, oClass, identifierValue);
//        break;
//      case SUPERUSERTYPEES:
//        if (identifierListValue == null) {
//          oClass.setSuperUserTypes(Collections.EMPTY_LIST);
//        } else {
//          doSetSuperclasses(ctx, oClass, identifierListValue);
//        }
//        break;
//      case OVERSIZE:
//        oClass.setOverSize(numberValue.getValue().floatValue());
//        break;
//      case STRICTMODE:
//        oClass.setStrictMode(booleanValue.booleanValue());
//        break;
//      case ABSTRACT:
//        oClass.setAbstract(booleanValue.booleanValue());
//        break;
//      case CUSTOM:
//        Object value = null;
//        if (customValue != null) {
//          value = customValue.execute((PIdentifiable) null, ctx);
//        }
//        if (value != null) {
//          value = "" + value;
//        }
//        oClass.setCustom(customKey.getStringValue(), (String) value);
//        break;
//      }
//    } else if (defaultClusterId != null) {
//      oClass.setDefaultClusterId(defaultClusterId.getValue().intValue());
//    } else if (defaultClusterName != null) {
//      int bucketId = ctx.getDatabase().getClusterIdByName(defaultClusterName.getStringValue());
//      oClass.setDefaultClusterId(bucketId);
//    }
    throw new UnsupportedOperationException();

//    OInternalResultSet resultSet = new OInternalResultSet();
//    OResultInternal result = new OResultInternal();
//    result.setProperty("operation", "ALTER USERTYPE");
//    result.setProperty("className", name.getStringValue());
//    result.setProperty("result", "OK");
//    return resultSet;
  }

  private void checkNotIndexed(DocumentType oClass) {
//    Set<PIndex> indexes = oClass.getAllIndexes();
//    if (indexes != null && indexes.size() > 0) {
//      throw new PCommandExecutionException("Cannot rename class '" + oClass.getName()
//          + "' because it has indexes defined on it. Drop indexes before or use UNSAFE (at your won risk)");
//    }
  }

  private void checkNotEdge(DocumentType oClass) {
//    if (oClass.isSubClassOf("E")) {
//      throw new PCommandExecutionException("Cannot alter class '" + oClass
//          + "' because is an Edge class and could break vertices. Use UNSAFE if you want to force it");
//    }
  }

  private void doSetSuperclass(CommandContext ctx, DocumentType oClass, Identifier superclassName) {
//    if (superclassName == null) {
//      throw new PCommandExecutionException("Invalid superclass name: " + toString());
//    }
//    OClass superclass = ctx.getDatabase().getMetadata().getSchema().getClass(superclassName.getStringValue());
//    if (superclass == null) {
//      throw new PCommandExecutionException("superclass not found: " + toString());
//    }
//    if (Boolean.TRUE.equals(add)) {
//      oClass.addSuperClass(superclass);
//    } else if (Boolean.TRUE.equals(remove)) {
//      oClass.removeSuperClass(superclass);
//    } else {
//      oClass.setSuperUserTypes(Collections.singletonList(superclass));
//    }
  }

  private void doSetSuperclasses(CommandContext ctx, DocumentType oClass, List<Identifier> superclassNames) {
//    if (superclassNames == null) {
//      throw new PCommandExecutionException("Invalid superclass name: " + toString());
//    }
//    List<OClass> superclasses = new ArrayList<>();
//    for (OIdentifier superclassName : superclassNames) {
//      OClass superclass = ctx.getDatabase().getMetadata().getSchema().getClass(superclassName.getStringValue());
//      if (superclass == null) {
//        throw new PCommandExecutionException("superclass not found: " + toString());
//      }
//      superclasses.add(superclass);
//    }
//    if (Boolean.TRUE.equals(add)) {
//      for (OClass superclass : superclasses) {
//        oClass.addSuperClass(superclass);
//      }
//    } else if (Boolean.TRUE.equals(remove)) {
//      for (OClass superclass : superclasses) {
//        oClass.removeSuperClass(superclass);
//      }
//    } else {
//      oClass.setSuperUserTypes(superclasses);
//    }
  }
}
/* JavaCC - OriginalChecksum=4668bb1cd336844052df941f39bdb634 (do not edit this line) */
