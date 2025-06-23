package org.cbioportal.application.file.export.repositories.mybatis.utils;

import java.io.IOException;
import java.util.Iterator;
import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.utils.CloseableIterator;

public class CursorAdapter<T> implements CloseableIterator<T> {
  private final Cursor<T> cursor;
  private Iterator<T> iterator;

  public CursorAdapter(Cursor<T> cursor) {
    this.cursor = cursor;
  }

  @Override
  public void close() throws IOException {
    cursor.close();
  }

  @Override
  public boolean hasNext() {
    return getIterator().hasNext();
  }

  @Override
  public T next() {
    return getIterator().next();
  }

  private Iterator<T> getIterator() {
    if (iterator == null) {
      iterator = cursor.iterator();
    }
    return iterator;
  }
}
