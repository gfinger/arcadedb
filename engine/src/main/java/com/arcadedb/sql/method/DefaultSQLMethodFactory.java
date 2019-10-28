/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */
package com.arcadedb.sql.method;

import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.sql.executor.SQLMethod;
import com.arcadedb.sql.function.conversion.SQLMethodAsDate;
import com.arcadedb.sql.function.conversion.SQLMethodAsDateTime;
import com.arcadedb.sql.function.conversion.SQLMethodAsDecimal;
import com.arcadedb.sql.function.conversion.SQLMethodConvert;
import com.arcadedb.sql.function.text.*;
import com.arcadedb.sql.method.misc.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Default method factory.
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultSQLMethodFactory implements SQLMethodFactory {

  private final Map<String, Object> methods = new HashMap<String, Object>();

  public DefaultSQLMethodFactory() {
    register(SQLMethodAppend.NAME, new SQLMethodAppend());
    register(SQLMethodAsBoolean.NAME, new SQLMethodAsBoolean());
    register(SQLMethodAsDate.NAME, new SQLMethodAsDate());
    register(SQLMethodAsDateTime.NAME, new SQLMethodAsDateTime());
    register(SQLMethodAsDecimal.NAME, new SQLMethodAsDecimal());
    register(SQLMethodAsFloat.NAME, new SQLMethodAsFloat());
    register(SQLMethodAsInteger.NAME, new SQLMethodAsInteger());
    register(SQLMethodAsList.NAME, new SQLMethodAsList());
    register(SQLMethodAsLong.NAME, new SQLMethodAsLong());
    register(SQLMethodAsMap.NAME, new SQLMethodAsMap());
    register(SQLMethodAsSet.NAME, new SQLMethodAsSet());
    register(SQLMethodAsString.NAME, new SQLMethodAsString());
    register(SQLMethodCharAt.NAME, new SQLMethodCharAt());
    register(SQLMethodConvert.NAME, new SQLMethodConvert());
    register(SQLMethodField.NAME, new SQLMethodField());
    register(SQLMethodFormat.NAME, new SQLMethodFormat());
    register(SQLMethodHash.NAME, new SQLMethodHash());
    register(SQLMethodIndexOf.NAME, new SQLMethodIndexOf());
    register(SQLMethodJavaType.NAME, new SQLMethodJavaType());
    register(SQLMethodKeys.NAME, new SQLMethodKeys());
    register(SQLMethodLastIndexOf.NAME, new SQLMethodLastIndexOf());
    register(SQLMethodLeft.NAME, new SQLMethodLeft());
    register(SQLMethodLength.NAME, new SQLMethodLength());
    register(SQLMethodNormalize.NAME, new SQLMethodNormalize());
    register(SQLMethodPrefix.NAME, new SQLMethodPrefix());
    register(SQLMethodRemove.NAME, new SQLMethodRemove());
    register(SQLMethodRemoveAll.NAME, new SQLMethodRemoveAll());
    register(SQLMethodReplace.NAME, new SQLMethodReplace());
    register(SQLMethodRight.NAME, new SQLMethodRight());
    register(SQLMethodSize.NAME, new SQLMethodSize());
    register(SQLMethodSplit.NAME, new SQLMethodSplit());
    register(SQLMethodToLowerCase.NAME, new SQLMethodToLowerCase());
    register(SQLMethodToUpperCase.NAME, new SQLMethodToUpperCase());
    register(SQLMethodTrim.NAME, new SQLMethodTrim());
    register(SQLMethodType.NAME, new SQLMethodType());
    register(SQLMethodSubString.NAME, new SQLMethodSubString());
    register(SQLMethodToJSON.NAME, new SQLMethodToJSON());
  }

  public void register(final String iName, final Object iImplementation) {
    methods.put(iName.toLowerCase(Locale.ENGLISH), iImplementation);
  }

  @Override
  public boolean hasMethod(final String iName) {
    return methods.containsKey(iName.toLowerCase(Locale.ENGLISH));
  }

  @Override
  public Set<String> getMethodNames() {
    return methods.keySet();
  }

  @Override
  public SQLMethod createMethod(final String name) throws CommandExecutionException {
    final Object m = methods.get(name);
    final SQLMethod method;

    if (m instanceof Class<?>)
      try {
        method = (SQLMethod) ((Class<?>) m).newInstance();
      } catch (Exception e) {
        throw new CommandExecutionException("Cannot create SQL method: " + m, e);
      }
    else
      method = (SQLMethod) m;

    if (method == null)
      throw new CommandExecutionException("Unknown method name: " + name);

    return method;
  }

}
