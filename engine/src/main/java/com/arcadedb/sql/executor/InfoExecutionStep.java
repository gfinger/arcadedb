/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.sql.executor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luigidellaquila on 19/12/16.
 */
public class InfoExecutionStep implements ExecutionStep {

  String              name;
  String              type;
  String              javaType;
  String              targetNode;
  String              description;
  long                cost;
  List<ExecutionStep> subSteps = new ArrayList<>();

  @Override public String getName() {
    return name;
  }

  @Override public String getType() {
    return type;
  }

  @Override public String getTargetNode() {
    return targetNode;
  }

  @Override public String getDescription() {
    return description;
  }

  @Override public List<ExecutionStep> getSubSteps() {
    return subSteps;
  }

  @Override public long getCost() {
    return cost;
  }

  @Override public Result toResult() {
    return null;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setTargetNode(String targetNode) {
    this.targetNode = targetNode;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setCost(long cost) {
    this.cost = cost;
  }

  public String getJavaType() {
    return javaType;
  }

  public void setJavaType(String javaType) {
    this.javaType = javaType;
  }
}
