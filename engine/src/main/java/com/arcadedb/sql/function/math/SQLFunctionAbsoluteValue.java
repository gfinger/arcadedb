/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */
package com.arcadedb.sql.function.math;

import com.arcadedb.database.Identifiable;
import com.arcadedb.sql.executor.CommandContext;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Evaluates the absolute value for numeric types.  The argument must be a
 * BigDecimal, BigInteger, Integer, Long, Double or a Float, or null.  If
 * null is passed in the result will be null.  Otherwise the result will
 * be the mathematical absolute value of the argument passed in and will be
 * of the same type that was passed in.
 *
 * @author Michael MacFadden
 */
public class SQLFunctionAbsoluteValue extends SQLFunctionMathAbstract {
  public static final String NAME = "abs";
  private             Object result;

  public SQLFunctionAbsoluteValue() {
    super(NAME, 1, 1);
  }

  public Object execute( Object iThis, final Identifiable iRecord, final Object iCurrentResult,
      final Object[] iParams, CommandContext iContext) {
    Object inputValue = iParams[0];

    if (inputValue == null) {
      result = null;
    } else if (inputValue instanceof BigDecimal) {
      result = ((BigDecimal) inputValue).abs();
    } else if (inputValue instanceof BigInteger) {
      result = ((BigInteger) inputValue).abs();
    } else if (inputValue instanceof Integer) {
      result = Math.abs((Integer) inputValue);
    } else if (inputValue instanceof Long) {
      result = Math.abs((Long) inputValue);
    } else if (inputValue instanceof Short) {
      result = (short) Math.abs((Short) inputValue);
    } else if (inputValue instanceof Double) {
      result = Math.abs((Double) inputValue);
    } else if (inputValue instanceof Float) {
      result = Math.abs((Float) inputValue);
    } else {
      throw new IllegalArgumentException("Argument to absolute value must be a number.");
    }

    return getResult();
  }

  public boolean aggregateResults() {
    return false;
  }

  public String getSyntax() {
    return "abs(<number>)";
  }

  @Override
  public Object getResult() {
    return result;
  }
}
