package org.cbioportal.application.file.model;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.SequencedMap;
import java.util.SequencedSet;

import static java.util.Collections.emptyList;

/**
 * A class representing a table of data.
 * <p>
 * This class implements the {@link CloseableIterator} interface to allow iteration over the rows of the table.
 * It also implements the {@link HeaderInfo} interface to provide information about the table's header and comments.
 * </p>
 */
public class Table implements CloseableIterator<SequencedMap<String, String>>, HeaderInfo {

    private final PeekingIterator<? extends TableRow> rows;
    private final Closeable closeable;
    private SequencedSet<String> header;

    public Table(CloseableIterator<? extends TableRow> rows) {
        this.closeable = rows;
        this.rows = Iterators.peekingIterator(rows);
    }

    public Table(CloseableIterator<? extends TableRow> rows, SequencedSet<String> header) {
        this(rows);
        this.header = header;
    }

    @Override
    public boolean hasNext() {
        return rows.hasNext();
    }

    @Override
    public SequencedMap<String, String> next() {
        return rows.next().toRow();
    }

    @Override
    public Iterable<Iterable<String>> getComments() {
        if (rows instanceof HeaderInfo headerInfo) {
            return headerInfo.getComments();
        }
        return emptyList();
    }

    @Override
    public SequencedSet<String> getHeader() {
        if (header != null) {
            return header;
        }
        if (rows instanceof HeaderInfo headerInfo) {
            return headerInfo.getHeader();
        }
        if (rows.hasNext()) {
            return rows.peek().toRow().sequencedKeySet();
        }
        return new LinkedHashSet<>();
    }

    @Override
    public void close() throws IOException {
        closeable.close();
    }

}
