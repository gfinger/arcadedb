/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.sql.executor;

import com.arcadedb.database.Document;
import com.arcadedb.database.Identifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by luigidellaquila on 12/10/16.
 */
public class ReturnMatchElementsStep extends AbstractUnrollStep {

  public ReturnMatchElementsStep(CommandContext context, boolean profilingEnabled) {
    super(context, profilingEnabled);
  }

  @Override
  protected Collection<Result> unroll(Result doc, CommandContext iContext) {
    List<Result> result = new ArrayList<>();
    for (String s : doc.getPropertyNames()) {
      if (!s.startsWith(OMatchExecutionPlanner.DEFAULT_ALIAS_PREFIX)) {
        Object elem = doc.getProperty(s);
        if (elem instanceof Identifiable) {
          ResultInternal newelem = new ResultInternal();
          newelem.setElement((Document) ((Identifiable) elem).getRecord());
          elem = newelem;
        }
        if (elem instanceof Result) {
          result.add((Result) elem);
        }
        //else...? TODO
      }
    }
    return result;
  }

  @Override
  public String prettyPrint(int depth, int indent) {
    String spaces = ExecutionStepInternal.getIndent(depth, indent);
    return spaces + "+ UNROLL $elements";
  }
}
