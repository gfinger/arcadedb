/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

package com.arcadedb.index;

import com.arcadedb.BaseTest;
import com.arcadedb.GlobalConfiguration;
import com.arcadedb.database.*;
import com.arcadedb.exception.DuplicatedKeyException;
import com.arcadedb.exception.NeedRetryException;
import com.arcadedb.exception.TransactionException;
import com.arcadedb.index.lsm.LSMTreeIndexAbstract;
import com.arcadedb.log.LogManager;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.SchemaImpl;
import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class NullValuesndexTest extends BaseTest {
  private static final int    TOT       = 10;
  private static final String TYPE_NAME = "V";
  private static final int    PAGE_SIZE = 20000;

  @Test
  public void testNullStrategyError() {
    try {
      database.transaction(new Database.TransactionScope() {
        @Override
        public void execute(Database database) {
          Assertions.assertFalse(database.getSchema().existsType(TYPE_NAME));

          final DocumentType type = database.getSchema().createDocumentType(TYPE_NAME, 3);
          type.createProperty("id", Integer.class);
          type.createProperty("name", String.class);
          final Index[] indexes = database.getSchema().createIndexes(SchemaImpl.INDEX_TYPE.LSM_TREE, true, TYPE_NAME, new String[] { "id" }, PAGE_SIZE);
          final Index[] indexes2 = database.getSchema()
              .createIndexes(SchemaImpl.INDEX_TYPE.LSM_TREE, true, TYPE_NAME, new String[] { "name" }, PAGE_SIZE, LSMTreeIndexAbstract.NULL_STRATEGY.ERROR,
                  null);

          for (int i = 0; i < TOT; ++i) {
            final MutableDocument v = database.newDocument(TYPE_NAME);
            v.set("id", i);
            v.set("name", "Jay");
            v.set("surname", "Miner");
            v.save();
          }

          final MutableDocument v = database.newDocument(TYPE_NAME);
          v.set("id", TOT);
          v.save();

          database.commit();
          database.begin();

          for (Index index : indexes) {
            Assertions.assertTrue(index.getStats().get("pages") > 1);
          }
        }
      });
      Assertions.fail();
    } catch (TransactionException e) {
      Assertions.assertTrue(e.getCause() instanceof IllegalArgumentException);
      Assertions.assertTrue(e.getCause().getMessage().startsWith("Indexed key V[name] cannot be NULL"));
    }
  }

  @Test
  public void testNullStrategySkip() {
    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {
        Assertions.assertFalse(database.getSchema().existsType(TYPE_NAME));

        final DocumentType type = database.getSchema().createDocumentType(TYPE_NAME, 3);
        type.createProperty("id", Integer.class);
        type.createProperty("name", String.class);
        final Index[] indexes = database.getSchema().createIndexes(SchemaImpl.INDEX_TYPE.LSM_TREE, true, TYPE_NAME, new String[] { "id" }, PAGE_SIZE);
        final Index[] indexes2 = database.getSchema()
            .createIndexes(SchemaImpl.INDEX_TYPE.LSM_TREE, true, TYPE_NAME, new String[] { "name" }, PAGE_SIZE, LSMTreeIndexAbstract.NULL_STRATEGY.SKIP, null);

        for (int i = 0; i < TOT; ++i) {
          final MutableDocument v = database.newDocument(TYPE_NAME);
          v.set("id", i);
          v.set("name", "Jay");
          v.set("surname", "Miner");
          v.save();
        }

        final MutableDocument v = database.newDocument(TYPE_NAME);
        v.set("id", TOT);
        v.save();

        database.commit();
        database.begin();
      }
    });

    database.transaction((db) -> {
      Assertions.assertEquals(db.countType(TYPE_NAME, true), TOT + 1);
    });
  }

}