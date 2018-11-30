/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.index.lsm;

import com.arcadedb.database.Binary;
import com.arcadedb.database.DatabaseInternal;
import com.arcadedb.database.RID;
import com.arcadedb.engine.BasePage;
import com.arcadedb.engine.MutablePage;
import com.arcadedb.engine.PaginatedComponent;
import com.arcadedb.engine.PaginatedFile;
import com.arcadedb.index.IndexCursorEntry;
import com.arcadedb.index.IndexException;
import com.arcadedb.schema.SchemaImpl;
import com.arcadedb.schema.Type;
import com.arcadedb.serializer.BinaryComparator;
import com.arcadedb.serializer.BinarySerializer;
import com.arcadedb.serializer.BinaryTypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static com.arcadedb.database.Binary.BYTE_SERIALIZED_SIZE;
import static com.arcadedb.database.Binary.INT_SERIALIZED_SIZE;

/**
 * Abstract class for LSM-based indexes. The first page contains 2 bytes to store key and value types. The pages are populated from the head of the page
 * with the pointers to the pair key/value that starts from the tail. A page is full when there is no space anymore between the head
 * (key pointers) and the tail (key/value pairs).
 * <p>
 * When a page is full, another page is created, waiting for a compaction.
 * <p>
 * HEADER ROOT PAGE (1st) = [offsetFreeKeyValueContent(int:4),numberOfEntries(int:4),mutable(boolean:1),compactedPageNumberOfSeries(int:4),subIndexFileId(int:4),numberOfKeys(byte:1),keyType(byte:1)*]
 * <p>
 * HEADER Nst PAGE        = [offsetFreeKeyValueContent(int:4),numberOfEntries(int:4),mutable(boolean:1),compactedPageNumberOfSeries(int:4)]
 */
public abstract class LSMTreeIndexAbstract extends PaginatedComponent {
  public static final    int    DEF_PAGE_SIZE     = 2 * 1024 * 1024;
  public static final    RID    REMOVED_ENTRY_RID = new RID(null, -1, -1l);
  protected static final String TEMP_EXT          = "temp_";

  protected static final LSMTreeIndexCompacted.LookupResult LOWER  = new LSMTreeIndexCompacted.LookupResult(false, true, 0, null);
  protected static final LSMTreeIndexCompacted.LookupResult HIGHER = new LSMTreeIndexCompacted.LookupResult(false, true, 0, null);

  protected final LSMTreeIndex     mainIndex;
  protected final BinaryComparator comparator;
  protected final BinarySerializer serializer;
  protected final byte             valueType = BinaryTypes.TYPE_COMPRESSED_RID;
  protected final boolean          unique;
  protected       byte[]           keyTypes;

  public enum COMPACTING_STATUS {NO, SCHEDULED, IN_PROGRESS}

  protected static class LookupResult {
    public final boolean found;
    public final boolean outside;
    public final int     keyIndex;
    public final int[]   valueBeginPositions;

    public LookupResult(final boolean found, final boolean outside, final int keyIndex, final int[] valueBeginPositions) {
      this.found = found;
      this.outside = outside;
      this.keyIndex = keyIndex;
      this.valueBeginPositions = valueBeginPositions;
    }
  }

  /**
   * Called at creation time.
   */
  protected LSMTreeIndexAbstract(final LSMTreeIndex mainIndex, final DatabaseInternal database, final String name, final boolean unique,
      String filePath, final String ext, final PaginatedFile.MODE mode, final byte[] keyTypes, final int pageSize) throws IOException {
    super(database, name, filePath, ext, mode, pageSize);
    this.mainIndex = mainIndex;
    this.serializer = database.getSerializer();
    this.comparator = serializer.getComparator();
    this.unique = unique;
    this.keyTypes = keyTypes;
  }

  /**
   * Called at cloning time.
   */
  protected LSMTreeIndexAbstract(final LSMTreeIndex mainIndex, final DatabaseInternal database, final String name, final boolean unique,
      String filePath, final String ext, final byte[] keyTypes, final int pageSize) throws IOException {
    super(database, name, filePath, TEMP_EXT + ext, PaginatedFile.MODE.READ_WRITE, pageSize);
    this.mainIndex = mainIndex;
    this.serializer = database.getSerializer();
    this.comparator = serializer.getComparator();
    this.unique = unique;
    this.keyTypes = keyTypes;
  }

  /**
   * Called at load time (1st page only).
   */
  protected LSMTreeIndexAbstract(final LSMTreeIndex mainIndex, final DatabaseInternal database, final String name, final boolean unique,
      String filePath, final int id, final PaginatedFile.MODE mode, final int pageSize) throws IOException {
    super(database, name, filePath, id, mode, pageSize);
    this.mainIndex = mainIndex;
    this.serializer = database.getSerializer();
    this.comparator = serializer.getComparator();
    this.unique = unique;
  }

  /**
   * @param purpose 0 = exists, 1 = retrieve, 2 = ascending iterator, 3 = descending iterator
   */
  protected abstract LookupResult compareKey(final Binary currentPageBuffer, final int startIndexArray, final Object[] convertedKeys,
      int mid, final int count, final int purpose);

  public PaginatedComponent getPaginatedComponent() {
    return this;
  }

  public boolean isUnique() {
    return unique;
  }

  public int getFileId() {
    return file.getFileId();
  }

  @Override
  public String toString() {
    return name + "(" + getFileId() + ")";
  }

  public byte[] getKeyTypes() {
    return keyTypes;
  }

  public boolean isDeletedEntry(final Object rid) {
    return ((RID) rid).getBucketId() < 0;
  }

  public void removeTempSuffix() {
    final String fileName = file.getFileName();

    final int extPos = fileName.lastIndexOf('.');
    if (fileName.substring(extPos + 1).startsWith(TEMP_EXT)) {
      try {
        file.rename(fileName.substring(0, extPos) + "." + fileName.substring(extPos + TEMP_EXT.length() + 1));
      } catch (FileNotFoundException e) {
        throw new IndexException("Cannot rename temp file", e);
      }
    }
  }

  public void drop() throws IOException {
    if (database.isOpen()) {
      database.getPageManager().deleteFile(file.getFileId());
      database.getFileManager().dropFile(file.getFileId());
      ((SchemaImpl) database.getSchema()).removeFile(file.getFileId());
      ((SchemaImpl) database.getSchema()).saveConfiguration();
    } else {
      new File(file.getFilePath()).delete();
    }
  }

  public Map<String, Long> getStats() {
    final Map<String, Long> stats = new HashMap<>();
    stats.put("pages", (long) getTotalPages());
    return stats;
  }

  @Override
  public Object getMainComponent() {
    return mainIndex;
  }

  /**
   * Lookups for an entry in the index by using dichotomy search.
   *
   * @param purpose 0 = exists, 1 = retrieve, 2 = ascending iterator, 3 = descending iterator
   *
   * @return always an LookupResult object, never null
   */
  protected LookupResult lookupInPage(final int pageNum, final int count, final Binary currentPageBuffer, final Object[] convertedKeys,
      final int purpose) {
    if (keyTypes.length == 0)
      throw new IllegalArgumentException("No key types found");

    if (convertedKeys.length > keyTypes.length)
      throw new IllegalArgumentException(
          "key is composed of " + convertedKeys.length + " items, while the index defined " + keyTypes.length + " items");

    if ((purpose == 0 || purpose == 1) && convertedKeys.length != keyTypes.length)
      throw new IllegalArgumentException(
          "key is composed of " + convertedKeys.length + " items, while the index defined " + keyTypes.length + " items");

    if (count == 0)
      // EMPTY, NOT FOUND
      return new LookupResult(false, true, 0, null);

    int low = 0;
    int high = count - 1;

    final int startIndexArray = getHeaderSize(pageNum);

    LookupResult result;

    // CHECK THE BOUNDARIES FIRST (LOWER THAN THE FIRST)
    result = compareKey(currentPageBuffer, startIndexArray, convertedKeys, low, count, purpose);
    if (result == LOWER) {
      if (purpose == 2)
        // BROWSE ASCENDING
        return new LookupResult(false, false, low, new int[] { currentPageBuffer.position() });

      return new LookupResult(false, true, low, null);
    } else if (result != HIGHER)
      return result;

    // CHECK THE BOUNDARIES FIRST (HIGHER THAN THE LAST)
    result = compareKey(currentPageBuffer, startIndexArray, convertedKeys, high, count, purpose);
    if (result == HIGHER) {
      if (purpose == 3)
        // BROWSE ASCENDING
        return new LookupResult(false, false, high, new int[] { currentPageBuffer.position() });

      return new LookupResult(false, true, count, null);
    } else if (result != LOWER)
      return result;

    int mid = low;
    while (low <= high) {
      mid = (low + high) / 2;

      result = compareKey(currentPageBuffer, startIndexArray, convertedKeys, mid, count, purpose);

      if (result == HIGHER)
        low = mid + 1;
      else if (result == LOWER)
        high = mid - 1;
      else
        return result;
    }

    if (purpose == 3) {
      return new LookupResult(false, false, high, null);
    }

    return new LookupResult(false, false, low, null);
  }

  protected void writeEntry(final Binary buffer, final Object[] keys, final Object rid) {
    buffer.clear();

    // WRITE KEYS
    for (int i = 0; i < keyTypes.length; ++i)
      serializer.serializeValue(buffer, keyTypes[i], keys[i]);

    writeEntryValue(buffer, rid);
  }

  protected void writeEntry(final Binary buffer, final Object[] keys, final Object[] rids) {
    buffer.clear();

    // WRITE KEYS
    for (int i = 0; i < keyTypes.length; ++i)
      serializer.serializeValue(buffer, keyTypes[i], keys[i]);

    writeEntryValues(buffer, rids);
  }

  /**
   * Reads the keys and returns the serialized size.
   */
  protected int getSerializedKeySize(final Binary buffer, final int keyLength) {
    final int startsAt = buffer.position();
    for (int keyIndex = 0; keyIndex < keyLength; ++keyIndex)
      serializer.deserializeValue(database, buffer, keyTypes[keyIndex]);

    return buffer.position() - startsAt;
  }

  protected Object[] convertKeys(final Object[] keys, final byte[] keyTypes) {
    if (keys != null) {
      final Object[] convertedKeys = new Object[keys.length];
      for (int i = 0; i < keys.length; ++i) {
        convertedKeys[i] = Type.convert(database, keys[i], BinaryTypes.getClassFromType(keyTypes[i]));

        if (convertedKeys[i] instanceof String)
          // OPTIMIZATION: ALWAYS CONVERT STRINGS TO BYTE[]
          convertedKeys[i] = ((String) convertedKeys[i]).getBytes();
      }
      return convertedKeys;
    }
    return keys;
  }

  protected Object[] getPageBound(final BasePage currentPage, final Binary currentPageBuffer) {
    final Object[] min = getKeyInPagePosition(currentPage.getPageId().getPageNumber(), currentPageBuffer, 0);

    final int count = getCount(currentPage);

    if (count > 1)
      return new Object[] { min, getKeyInPagePosition(currentPage.getPageId().getPageNumber(), currentPageBuffer, count - 1) };
    else
      return new Object[] { min, min };
  }

  protected Object[] getKeyInPagePosition(final int pageNum, final Binary currentPageBuffer, final int position) {
    final int startIndexArray = getHeaderSize(pageNum);
    final int contentPos = currentPageBuffer.getInt(startIndexArray + (position * INT_SERIALIZED_SIZE));
    currentPageBuffer.position(contentPos);

    final Object[] key = new Object[keyTypes.length];

    for (int keyIndex = 0; keyIndex < keyTypes.length; ++keyIndex)
      key[keyIndex] = serializer.deserializeValue(database, currentPageBuffer, keyTypes[keyIndex]);

    return key;
  }

  protected int compareKey(final Binary currentPageBuffer, final int startIndexArray, final Object keys[], final int mid, final int count) {
    final int contentPos = currentPageBuffer.getInt(startIndexArray + (mid * INT_SERIALIZED_SIZE));
    if (contentPos < startIndexArray + (count * INT_SERIALIZED_SIZE))
      throw new IndexException(
          "Internal error: invalid content position " + contentPos + " is < of " + (startIndexArray + (count * INT_SERIALIZED_SIZE)));

    currentPageBuffer.position(contentPos);

    int result = -1;
    for (int keyIndex = 0; keyIndex < keys.length; ++keyIndex) {
      // GET THE KEY
      final Object key = keys[keyIndex];

      if (keyTypes[keyIndex] == BinaryTypes.TYPE_STRING) {
        // OPTIMIZATION: SPECIAL CASE, LAZY EVALUATE BYTE PER BYTE THE STRING
        result = comparator.compareStrings((byte[]) key, currentPageBuffer);
      } else {
        final Object keyValue = serializer.deserializeValue(database, currentPageBuffer, keyTypes[keyIndex]);
        result = comparator.compare(key, keyTypes[keyIndex], keyValue, keyTypes[keyIndex]);
      }

      if (result != 0)
        break;
    }

    return result;
  }

  protected int getHeaderSize(final int pageNum) {
    int size = INT_SERIALIZED_SIZE + INT_SERIALIZED_SIZE + BYTE_SERIALIZED_SIZE + INT_SERIALIZED_SIZE;
    if (pageNum == 0)
      size += INT_SERIALIZED_SIZE + BYTE_SERIALIZED_SIZE + keyTypes.length;

    return size;
  }

  protected static int compareKeys(final BinaryComparator comparator, final byte[] keyTypes, final Object[] keys1, final Object[] keys2) {
    final int minKeySize = Math.min(keys1.length, keys2.length);

    for (int k = 0; k < minKeySize; ++k) {
      final int result = comparator.compare(keys1[k], keyTypes[k], keys2[k], keyTypes[k]);
      if (result < 0)
        return -1;
      else if (result > 0)
        return 1;
    }
    return 0;
  }

  private void writeEntryValues(final Binary buffer, final Object[] values) {
    // WRITE NUMBER OF VALUES
    serializer.serializeValue(buffer, BinaryTypes.TYPE_INT, values.length);

    // WRITE VALUES
    for (int i = 0; i < values.length; ++i)
      serializer.serializeValue(buffer, valueType, values[i]);
  }

  private void writeEntryValue(final Binary buffer, final Object value) {
    // WRITE NUMBER OF VALUES
    serializer.serializeValue(buffer, BinaryTypes.TYPE_INT, 1);

    // WRITE VALUES
    serializer.serializeValue(buffer, valueType, value);
  }

  protected RID[] readEntryValues(final Binary buffer) {
    final int items = (int) serializer.deserializeValue(database, buffer, BinaryTypes.TYPE_INT);

    final RID[] rids = new RID[items];

    for (int i = 0; i < rids.length; ++i)
      rids[i] = (RID) serializer.deserializeValue(database, buffer, valueType);

    return rids;
  }

  private void readEntryValues(final Binary buffer, final List<RID> list) {
    final int items = (int) serializer.deserializeValue(database, buffer, BinaryTypes.TYPE_INT);

    final Object[] rids = new Object[items];

    for (int i = 0; i < rids.length; ++i)
      list.add((RID) serializer.deserializeValue(database, buffer, valueType));
  }

  private List<RID> readAllValuesFromResult(final Binary currentPageBuffer, final LookupResult result) {
    final List<RID> allValues = new ArrayList<>();
    for (int i = 0; i < result.valueBeginPositions.length; ++i) {
      currentPageBuffer.position(result.valueBeginPositions[i]);
      readEntryValues(currentPageBuffer, allValues);
    }
    return allValues;
  }

  protected int getCount(final BasePage currentPage) {
    return currentPage.readInt(INT_SERIALIZED_SIZE);
  }

  protected void setCount(final MutablePage currentPage, final int newCount) {
    currentPage.writeInt(INT_SERIALIZED_SIZE, newCount);
  }

  protected boolean isMutable(final BasePage currentPage) {
    return currentPage.readByte(INT_SERIALIZED_SIZE + INT_SERIALIZED_SIZE) == 1;
  }

  protected void setMutable(final MutablePage currentPage, final boolean mutable) {
    currentPage.writeByte(INT_SERIALIZED_SIZE + INT_SERIALIZED_SIZE, (byte) (mutable ? 1 : 0));
  }

  protected Object[] checkForNulls(final Object keys[]) {
    if (keys != null)
      for (int i = 0; i < keys.length; ++i)
        if (keys[i] == null)
          throw new IllegalArgumentException(
              "Indexed key " + mainIndex.getTypeName() + Arrays.toString(mainIndex.propertyNames) + " cannot be NULL");
    return keys;
  }

  protected boolean lookupInPageAndAddInResultset(final BasePage currentPage, final Binary currentPageBuffer, final int count,
      final Object[] originalKeys, final Object[] convertedKeys, final int limit, final Set<IndexCursorEntry> set,
      final Set<RID> removedRIDs) {
    final LookupResult result = lookupInPage(currentPage.getPageId().getPageNumber(), count, currentPageBuffer, convertedKeys, 1);
    if (result.found) {
      // REAL ALL THE ENTRIES
      final List<RID> allValues = readAllValuesFromResult(currentPageBuffer, result);

      // START FROM THE LAST ENTRY
      for (int i = allValues.size() - 1; i > -1; --i) {
        final RID rid = allValues.get(i);

        if (REMOVED_ENTRY_RID.equals(rid)) {
          // DELETED ITEM
          set.clear();
          return false;
        }

        if (rid.getBucketId() < 0) {
          // RID DELETED, SKIP THE RID
          final RID originalRID = getOriginalRID(rid);
          if (!set.contains(originalRID))
            removedRIDs.add(originalRID);
          continue;
        }

        if (removedRIDs.contains(rid))
          // ALREADY FOUND AS DELETED
          continue;

        set.add(new IndexCursorEntry(originalKeys, rid, 1));

        if (limit > -1 && set.size() >= limit) {
          return false;
        }
      }
    }
    return true;
  }

  protected int getValuesFreePosition(final BasePage currentPage) {
    return currentPage.readInt(0);
  }

  protected void setValuesFreePosition(final MutablePage currentPage, final int newValuesFreePosition) {
    currentPage.writeInt(0, newValuesFreePosition);
  }

  protected RID getOriginalRID(final RID rid) {
    return new RID(database, (rid.getBucketId() * -1) - 2, rid.getPosition());
  }
}