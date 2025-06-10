package org.cbioportal.application.file.utils;

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public interface CloseableIterator<T> extends Closeable, Iterator<T> {
  static <T> CloseableIterator<T> empty() {
    return new CloseableIterator<T>() {
      @Override
      public void close() {}

      @Override
      public boolean hasNext() {
        return false;
      }

      @Override
      public T next() {
        throw new NoSuchElementException("No elements in iterator");
      }
    };
  }
}
