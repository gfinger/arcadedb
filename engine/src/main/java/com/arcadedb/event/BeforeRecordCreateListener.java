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

package com.arcadedb.event;

import com.arcadedb.database.Record;

/**
 * Listener to receive events before a new record (documents, vertices and edges) is created.
 * <p>
 * NOTE: the callback is invoked synchronously. For this reason the execution should be as fast as possible. Even with a fast implementation, using this
 * callback may cause a sensible slowdown of creation operations.
 *
 * @author Luca Garulli (l.garulli@arcadedata.com)
 **/
public interface BeforeRecordCreateListener {
  /**
   * Callback invoked right before a new record (documents, vertices and edges) has been saved.
   *
   * @return true if the record must be saved, otherwise false to prevent the record to be created.
   */
  boolean onBeforeCreate(Record record);
}