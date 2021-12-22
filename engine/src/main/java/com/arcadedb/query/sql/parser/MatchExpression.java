/*
 * Copyright © 2021-present Arcade Data Ltd (info@arcadedata.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* Generated By:JJTree: Do not edit this line. OMatchExpression.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import java.util.*;
import java.util.stream.*;

public class MatchExpression extends SimpleNode {
  protected MatchFilter         origin;
  protected List<MatchPathItem> items = new ArrayList<MatchPathItem>();

  public MatchExpression(int id) {
    super(id);
  }

  public MatchExpression(SqlParser p, int id) {
    super(p, id);
  }

  public void toString(Map<String, Object> params, StringBuilder builder) {
    origin.toString(params, builder);
    for (MatchPathItem item : items) {
      item.toString(params, builder);
    }
  }

  @Override
  public MatchExpression copy() {
    MatchExpression result = new MatchExpression(-1);
    result.origin = origin == null ? null : origin.copy();
    result.items = items == null ? null : items.stream().map(x -> x.copy()).collect(Collectors.toList());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    MatchExpression that = (MatchExpression) o;

    if (origin != null ? !origin.equals(that.origin) : that.origin != null)
      return false;
    return items != null ? items.equals(that.items) : that.items == null;
  }

  @Override
  public int hashCode() {
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
