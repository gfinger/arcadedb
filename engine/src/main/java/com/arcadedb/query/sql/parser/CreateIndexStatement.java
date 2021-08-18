/*
 * Copyright 2021 Arcade Data Ltd
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/* Generated By:JJTree: Do not edit this line. OCreateIndexStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Database;
import com.arcadedb.database.Document;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.exception.CommandSQLParsingException;
import com.arcadedb.index.Index;
import com.arcadedb.index.lsm.LSMTreeIndexAbstract;
import com.arcadedb.schema.EmbeddedSchema;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.InternalResultSet;
import com.arcadedb.query.sql.executor.ResultInternal;
import com.arcadedb.query.sql.executor.ResultSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class CreateIndexStatement extends ODDLStatement {

  protected IndexName                          name;
  protected Identifier                         typeName;
  protected List<Property>                     propertyList = new ArrayList<Property>();
  protected Identifier                         type;
  protected Identifier                         engine;
  protected LSMTreeIndexAbstract.NULL_STRATEGY nullStrategy;
  protected List<Identifier>                   keyTypes     = new ArrayList<Identifier>();
  protected Json                               schema;
  protected boolean                            ifNotExists  = false;

  public CreateIndexStatement(int id) {
    super(id);
  }

  public CreateIndexStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override
  public ResultSet executeDDL(final CommandContext ctx) {
    final Long totalIndexed = (Long) execute(ctx);

    final InternalResultSet rs = new InternalResultSet();
    final ResultInternal result = new ResultInternal();
    result.setProperty("operation", "create index");
    result.setProperty("name", name.getValue());
    result.setProperty("totalIndexed", totalIndexed);

    rs.add(result);
    return rs;
  }

  Object execute(final CommandContext ctx) {
    final Database database = ctx.getDatabase();

    if (database.getSchema().existsIndex(name.getValue())) {
      if (ifNotExists) {
        return null;
      } else {
        throw new CommandExecutionException("Index " + name + " already exists");
      }
    }

    final String[] fields = calculateProperties(ctx);

    final EmbeddedSchema.INDEX_TYPE indexType;
    boolean unique = false;

    final String typeAsString = type.getStringValue();
    if (typeAsString.equalsIgnoreCase("FULL_TEXT"))
      indexType = EmbeddedSchema.INDEX_TYPE.FULL_TEXT;
    else if (typeAsString.equalsIgnoreCase("UNIQUE")) {
      indexType = EmbeddedSchema.INDEX_TYPE.LSM_TREE;
      unique = true;
    } else if (typeAsString.equalsIgnoreCase("NOTUNIQUE")) {
      indexType = EmbeddedSchema.INDEX_TYPE.LSM_TREE;
      unique = false;
    } else
      throw new CommandSQLParsingException("Index type '" + typeAsString + "' is not supported");

    final AtomicLong total = new AtomicLong();

    database.getSchema().createTypeIndex(indexType, unique, typeName.getStringValue(), fields, LSMTreeIndexAbstract.DEF_PAGE_SIZE, nullStrategy,
        new Index.BuildIndexCallback() {
          @Override
          public void onDocumentIndexed(final Document document, final long totalIndexed) {
            total.incrementAndGet();

            if (totalIndexed % 100000 == 0) {
              System.out.print(".");
              System.out.flush();
            }
          }
        });

    return total.get();
  }

  /***
   * returns the list of property names to be indexed
   *
   * @param ctx
   * @return
   */
  private String[] calculateProperties(final CommandContext ctx) {
    if (propertyList == null) {
      return null;
    }
    return propertyList.stream().map(x -> x.getCompleteKey()).collect(Collectors.toList()).toArray(new String[] {});
  }

  @Override
  public void toString(final Map<Object, Object> params, final StringBuilder builder) {
    builder.append("CREATE INDEX ");
    name.toString(params, builder);
    if (typeName != null) {
      builder.append(" ON ");
      typeName.toString(params, builder);
      builder.append(" (");
      boolean first = true;
      for (Property prop : propertyList) {
        if (!first) {
          builder.append(", ");
        }
        if (prop.name != null) {
          prop.name.toString(params, builder);
        } else {
          prop.recordAttribute.toString(params, builder);
        }
        if (prop.byKey) {
          builder.append(" BY KEY");
        } else if (prop.byValue) {
          builder.append(" BY VALUE");
        }
        if (prop.collate != null) {
          builder.append(" COLLATE ");
          prop.collate.toString(params, builder);
        }
        first = false;
      }
      builder.append(")");
    }
    builder.append(" ");
    type.toString(params, builder);
    if (engine != null) {
      builder.append(" ENGINE ");
      engine.toString(params, builder);
    }
    if (nullStrategy != null) {
      builder.append(" NULL_STRATEGY ");
      builder.append(nullStrategy.toString());
    }
    if (keyTypes != null && keyTypes.size() > 0) {
      boolean first = true;
      builder.append(" ");
      for (Identifier keyType : keyTypes) {
        if (!first) {
          builder.append(",");
        }
        keyType.toString(params, builder);
        first = false;
      }
    }
    if (schema != null) {
      builder.append(" METADATA ");
      schema.toString(params, builder);
    }
  }

  @Override
  public CreateIndexStatement copy() {
    CreateIndexStatement result = new CreateIndexStatement(-1);
    result.name = name == null ? null : name.copy();
    result.typeName = typeName == null ? null : typeName.copy();
    result.propertyList = propertyList == null ? null : propertyList.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.type = type == null ? null : type.copy();
    result.engine = engine == null ? null : engine.copy();
    result.nullStrategy = nullStrategy == null ? null : nullStrategy;
    result.keyTypes = keyTypes == null ? null : keyTypes.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.schema = schema == null ? null : schema.copy();
    return result;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final CreateIndexStatement that = (CreateIndexStatement) o;

    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;
    if (typeName != null ? !typeName.equals(that.typeName) : that.typeName != null)
      return false;
    if (propertyList != null ? !propertyList.equals(that.propertyList) : that.propertyList != null)
      return false;
    if (type != null ? !type.equals(that.type) : that.type != null)
      return false;
    if (engine != null ? !engine.equals(that.engine) : that.engine != null)
      return false;
    if (nullStrategy != null ? !nullStrategy.equals(that.nullStrategy) : that.nullStrategy != null)
      return false;
    if (keyTypes != null ? !keyTypes.equals(that.keyTypes) : that.keyTypes != null)
      return false;
    return schema != null ? schema.equals(that.schema) : that.schema == null;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (typeName != null ? typeName.hashCode() : 0);
    result = 31 * result + (propertyList != null ? propertyList.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (engine != null ? engine.hashCode() : 0);
    result = 31 * result + (nullStrategy != null ? nullStrategy.hashCode() : 0);
    result = 31 * result + (keyTypes != null ? keyTypes.hashCode() : 0);
    result = 31 * result + (schema != null ? schema.hashCode() : 0);
    return result;
  }

  public static class Property {
    protected Identifier      name;
    protected RecordAttribute recordAttribute;
    protected boolean         byKey   = false;
    protected boolean         byValue = false;
    protected Identifier      collate;

    public Property copy() {
      final Property result = new Property();
      result.name = name == null ? null : name.copy();
      result.recordAttribute = recordAttribute == null ? null : recordAttribute.copy();
      result.byKey = byKey;
      result.byValue = byValue;
      result.collate = collate == null ? null : collate.copy();
      return result;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      final Property property = (Property) o;

      if (byKey != property.byKey)
        return false;
      if (byValue != property.byValue)
        return false;
      if (name != null ? !name.equals(property.name) : property.name != null)
        return false;
      if (recordAttribute != null ? !recordAttribute.equals(property.recordAttribute) : property.recordAttribute != null)
        return false;
      return collate != null ? collate.equals(property.collate) : property.collate == null;
    }

    @Override
    public int hashCode() {
      int result = name != null ? name.hashCode() : 0;
      result = 31 * result + (recordAttribute != null ? recordAttribute.hashCode() : 0);
      result = 31 * result + (byKey ? 1 : 0);
      result = 31 * result + (byValue ? 1 : 0);
      result = 31 * result + (collate != null ? collate.hashCode() : 0);
      return result;
    }

    /**
     * returns the complete key to index, eg. property name or "property by key/value"
     *
     * @return
     */
    public String getCompleteKey() {
      StringBuilder result = new StringBuilder();
      if (name != null)
        result.append(name.getStringValue());
      else if (recordAttribute != null)
        result.append(recordAttribute.getName());

      if (byKey) {
        result.append(" by key");
      }
      if (byValue) {
        result.append(" by value");
      }
      return result.toString();
    }
  }
}
/* JavaCC - OriginalChecksum=bd090e02c4346ad390a6b8c77f1b9dba (do not edit this line) */
