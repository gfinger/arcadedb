/*
 * Copyright (c) - Arcade Analytics LTD (https://arcadeanalytics.com)
 */
package com.arcadedb.utility;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Iterator that allow to iterate against multiple collection of elements.
 */
public class MultiIterator<T> implements Iterator<T>, Iterable<T> {
  private List<Object> sources;
  private Iterator<?>  sourcesIterator;
  private Iterator<T>  partialIterator;

  private int     browsed  = 0;
  private int     skip     = -1;
  private int     limit    = -1;
  private boolean embedded = false;

  private int skipped = 0;

  public MultiIterator() {
    sources = new ArrayList<>();
  }

  public MultiIterator(final Iterator<? extends Collection<?>> iterator) {
    sourcesIterator = iterator;
    getNextPartial();
  }

  @Override
  public boolean hasNext() {
    while (skipped < skip) {
      if (!hasNextInternal()) {
        return false;
      }
      partialIterator.next();
      skipped++;
    }
    return hasNextInternal();
  }

  private boolean hasNextInternal() {
    if (sourcesIterator == null) {
      if (sources == null || sources.isEmpty())
        return false;

      // THE FIRST TIME CREATE THE ITERATOR
      sourcesIterator = sources.iterator();
      getNextPartial();
    }

    if (partialIterator == null)
      return false;

    if (limit > -1 && browsed >= limit)
      return false;

    if (partialIterator.hasNext())
      return true;
    else if (sourcesIterator.hasNext())
      return getNextPartial();

    return false;
  }

  @Override
  public T next() {
    if (!hasNext())
      throw new NoSuchElementException();

    browsed++;
    return partialIterator.next();
  }

  @Override
  public Iterator<T> iterator() {
    reset();
    return this;
  }

  public void reset() {
    sourcesIterator = null;
    partialIterator = null;
    browsed = 0;
    skipped = 0;
  }

  public MultiIterator<T> add(final Object iValue) {
    if (iValue != null) {
      if (sourcesIterator != null)
        throw new IllegalStateException("MultiCollection iterator is in use and new collections cannot be added");
      sources.add(iValue);
    }
    return this;
  }

  public int size() {
    // SUM ALL THE COLLECTION SIZES
    int size = 0;
    final int totSources = sources.size();
    for (int i = 0; i < totSources; ++i) {
      final Object o = sources.get(i);

      if (o != null)
        if (o instanceof Collection<?>)
          size += ((Collection<?>) o).size();
        else if (o instanceof Map<?, ?>)
          size += ((Map<?, ?>) o).size();
        else if (o.getClass().isArray())
          size += Array.getLength(o);
        else
          size++;
    }
    return size;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("PMultiIterator.remove()");
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(final int limit) {
    this.limit = limit;
  }

  public int getSkip() {
    return skip;
  }

  public void setSkip(final int skip) {
    this.skip = skip;
  }

  public boolean contains(final Object value) {
    final int totSources = sources.size();
    for (int i = 0; i < totSources; ++i) {
      Object o = sources.get(i);

      if (o != null) {
        if (o instanceof Collection<?>) {
          if (((Collection<?>) o).contains(value))
            return true;
        }
      }
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  protected boolean getNextPartial() {
    if (sourcesIterator != null)
      while (sourcesIterator.hasNext()) {
        Object next = sourcesIterator.next();
        if (next != null) {

          if (next instanceof Iterable<?>)
            next = ((Iterable) next).iterator();

          if (next instanceof Iterator<?>) {
            if (((Iterator<T>) next).hasNext()) {
              partialIterator = (Iterator<T>) next;
              return true;
            }
          } else if (next instanceof Collection<?>) {
            if (!((Collection<T>) next).isEmpty()) {
              partialIterator = ((Collection<T>) next).iterator();
              return true;
            }
          } else if (next.getClass().isArray()) {
            final int arraySize = Array.getLength(next);
            if (arraySize > 0) {
              if (arraySize == 1)
                partialIterator = new IterableObject<T>((T) Array.get(next, 0));
              else
                partialIterator = new IterableObjectArray<T>(next).iterator();
              return true;
            }
          } else {
            partialIterator = new IterableObject<T>((T) next);
            return true;
          }
        }
      }

    return false;
  }

  public boolean isEmbedded() {
    return embedded;
  }

  public MultiIterator<T> setEmbedded(final boolean embedded) {
    this.embedded = embedded;
    return this;
  }

  @Override
  public String toString() {
    return "[" + size() + "]";
  }
}
