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

package com.arcadedb.schema;

import com.arcadedb.database.DatabaseInternal;
import com.arcadedb.engine.Bucket;
import com.arcadedb.graph.Vertex;

public class VertexType extends DocumentType {

  public VertexType(final EmbeddedSchema schema, final String name) {
    super(schema, name);
  }

  public byte getType() {
    return Vertex.RECORD_TYPE;
  }

  protected void addBucketInternal(final Bucket bucket) {
    super.addBucketInternal(bucket);
    ((DatabaseInternal) schema.getDatabase()).getGraphEngine().createVertexType((DatabaseInternal) schema.getDatabase(), this);
  }
}
