package org.cbioportal.application.file.model;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

/**
 * Generic memory efficient representation of a long table with columns and rows.
 * The long here means the table has much more rows than columns. The number of columns is fixed.
 * The class implements an iterator (single pass) over rows, where each row is a function from column to optional value.
 * It also provides an iterable (multiple passes) over columns.
 * @param <C> - column type
 * @param <R> - row type
 */
public class LongTable<C, R> implements Iterator<Function<C, Optional<R>>>, Closeable {

    private final Iterable<C> columns;
    private final Iterator<Function<C, Optional<R>>> rows;

    public LongTable(Iterable<C> columns, Iterator<Function<C, Optional<R>>> rows) {
       this.rows = rows;
       this.columns = columns;
    }

    public Iterable<C> getColumns() {
        return columns;
    }

    @Override
    public boolean hasNext() {
        return rows.hasNext();
    }

    @Override
    public Function<C, Optional<R>> next() {
        return rows.next();
    }

    @Override
    public void close() throws IOException {
        if (rows instanceof Closeable) {
            ((Closeable) rows).close();
        }
    }
}
