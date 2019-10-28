/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb;

import com.arcadedb.database.Database;
import com.arcadedb.database.Identifiable;
import com.arcadedb.database.MutableDocument;
import com.arcadedb.database.async.ErrorCallback;
import com.arcadedb.engine.DatabaseChecker;
import com.arcadedb.exception.ConcurrentModificationException;
import com.arcadedb.graph.MutableVertex;
import com.arcadedb.index.IndexCursor;
import com.arcadedb.log.LogManager;
import com.arcadedb.schema.EdgeType;
import com.arcadedb.schema.SchemaImpl;
import com.arcadedb.schema.VertexType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class MVCCTest extends BaseTest {
  private static final int CYCLES      = 3;
  private static final int TOT_ACCOUNT = 100;
  private static final int TOT_TX      = 100;
  private static final int PARALLEL    = Runtime.getRuntime().availableProcessors();

  @Test
  public void testMVCC() {
    for (int i = 0; i < CYCLES; ++i) {
      createSchema();

      populateDatabase();

      LogManager.instance().log(this, Level.INFO, "Executing " + TOT_TX + " transactions between " + TOT_ACCOUNT + " accounts");

      database.async().setParallelLevel(PARALLEL);

      final AtomicLong otherErrors = new AtomicLong();
      final AtomicLong mvccErrors = new AtomicLong();
      database.async().onError(new ErrorCallback() {
        @Override
        public void call(Exception exception) {

          if (exception instanceof ConcurrentModificationException) {
            mvccErrors.incrementAndGet();
          } else {
            otherErrors.incrementAndGet();
            LogManager.instance().log(this, Level.SEVERE, "UNEXPECTED ERROR: " + exception, exception);
          }
        }
      });

      long begin = System.currentTimeMillis();

      try {
        final Random rnd = new Random();

        for (long txId = 0; txId < TOT_TX; ++txId) {
          database.async().transaction(new Database.TransactionScope() {
            @Override
            public void execute(Database database) {
              Assertions.assertTrue(database.getTransaction().getModifiedPages() == 0);
              Assertions.assertNull(database.getTransaction().getPageCounter(1));

              final MutableDocument tx = database.newVertex("Transaction");
              tx.set("uuid", UUID.randomUUID().toString());
              tx.set("date", new Date());
              tx.set("amount", rnd.nextInt(TOT_ACCOUNT));
              tx.save();

              final IndexCursor accounts = database.lookupByKey("Account", new String[] { "id" }, new Object[] { 0 });

              Assertions.assertTrue(accounts.hasNext());

              Identifiable account = accounts.next();

              ((MutableVertex) tx).newEdge("PurchasedBy", account, true, "date", new Date());
            }
          }, 0);
        }

        database.async().waitCompletion();

      } finally {
        new DatabaseChecker().check(database);

        Assertions.assertTrue(mvccErrors.get() > 0);
        Assertions.assertEquals(0, otherErrors.get());

        System.out.println("Insertion finished in " + (System.currentTimeMillis() - begin) + "ms, managed mvcc exceptions " + mvccErrors.get());

        database.drop();
        database = factory.create();
      }

      LogManager.instance().flush();
      System.out.flush();
      System.out.println("----------------");
    }
  }

  private void populateDatabase() {

    long begin = System.currentTimeMillis();

    try {
      database.transaction(new Database.TransactionScope() {
        @Override
        public void execute(Database database) {
          for (long row = 0; row < TOT_ACCOUNT; ++row) {
            final MutableDocument record = database.newVertex("Account");
            record.set("id", row);
            record.set("name", "Luca" + row);
            record.set("surname", "Skywalker" + row);
            record.set("registered", new Date());
            record.save();
          }
        }
      });

    } finally {
      LogManager.instance().log(this, Level.INFO, "Database populate finished in " + (System.currentTimeMillis() - begin) + "ms");
    }
  }

  private void createSchema() {
    if (!database.getSchema().existsType("Account")) {
      database.begin();

      final VertexType accountType = database.getSchema().createVertexType("Account", PARALLEL);
      accountType.createProperty("id", Long.class);
      accountType.createProperty("name", String.class);
      accountType.createProperty("surname", String.class);
      accountType.createProperty("registered", Date.class);

      database.getSchema().createIndexes(SchemaImpl.INDEX_TYPE.LSM_TREE, true, "Account", new String[] { "id" }, 5000000);

      final VertexType txType = database.getSchema().createVertexType("Transaction", PARALLEL);
      txType.createProperty("uuid", String.class);
      txType.createProperty("date", Date.class);
      txType.createProperty("amount", BigDecimal.class);

      database.getSchema().createIndexes(SchemaImpl.INDEX_TYPE.LSM_TREE, true, "Transaction", new String[] { "uuid" }, 5000000);

      final EdgeType edgeType = database.getSchema().createEdgeType("PurchasedBy", PARALLEL);
      edgeType.createProperty("date", Date.class);

      database.commit();
    }
  }
}