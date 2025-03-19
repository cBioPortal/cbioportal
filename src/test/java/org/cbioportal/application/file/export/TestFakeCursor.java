package org.cbioportal.application.file.export;

import org.apache.ibatis.cursor.Cursor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TestFakeCursor<T> implements Cursor<T> {
    private final Iterator<T> iterator;
    private boolean closed = false;

    public TestFakeCursor(T... ts) {
        this.iterator = Arrays.stream(ts).iterator();
    }

    @Override
    public Iterator<T> iterator() {
        if (closed) {
            throw new IllegalStateException("Cursor is already closed.");
        }
        return iterator;
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public boolean isOpen() {
        return !closed;
    }

    @Override
    public boolean isConsumed() {
        return !iterator.hasNext();
    }

    @Override
    public int getCurrentIndex() {
        return 0;
    }
}