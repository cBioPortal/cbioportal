package org.cbioportal.application.file.export;

import java.io.IOException;
import java.util.Iterator;
import org.cbioportal.application.file.utils.CloseableIterator;

public class SimpleCloseableIterator<T> implements CloseableIterator<T> {

  private final Iterator<T> iterator;

  public SimpleCloseableIterator(Iterable<T> ts) {
    this.iterator = ts.iterator();
  }

  @Override
  public void close() throws IOException {
    // No resources to close in this simple implementation
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public T next() {
    return iterator.next();
  }
}
