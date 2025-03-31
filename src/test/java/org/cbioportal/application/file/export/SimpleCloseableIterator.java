package org.cbioportal.application.file.export;

import org.cbioportal.application.file.utils.CloseableIterator;

import java.io.IOException;
import java.util.Iterator;

public class SimpleCloseableIterator<T> implements CloseableIterator<T> {

    private final Iterator<T> iterator;

    public SimpleCloseableIterator(Iterable<T> ts) {
        this.iterator = ts.iterator();
    }

    @Override
    public void close() throws IOException {
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
