package org.cbioportal.application.file.export.writers;

import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.cbioportal.application.file.export.ExportException;
import org.cbioportal.application.file.model.HeaderInfo;

/** Writes TSV records to a writer */
public class TsvDataWriter {

  public static final String COMMENT_STARTER = "#";
  private static final String TAB = "\t";
  private final Writer writer;

  public TsvDataWriter(Writer writer) {
    this.writer = writer;
  }

  private static String composeRow(Iterable<String> row) {
    return StreamSupport.stream(row.spliterator(), false)
            .map(s -> s == null ? "" : s.replace(TAB, "\\t"))
            .collect(Collectors.joining(TAB))
        + "\n";
  }

  public void write(Iterator<SequencedMap<String, String>> table) {
    SequencedSet<String> header = null;
    if (table instanceof HeaderInfo headerInfo) {
      header = writeHeaderInfoRows(headerInfo);
    }
    writeRows(header, table);
  }

  private SequencedSet<String> writeHeaderInfoRows(HeaderInfo headerInfo) {
    SequencedSet<String> header;
    Integer size = null;
    for (Iterable<String> comments : headerInfo.getComments()) {
      if (size == null) size = Iterables.size(comments);
      else if (size != Iterables.size(comments)) {
        throw new IllegalArgumentException(
            "All comments must have the same number of columns: "
                + size
                + " != "
                + Iterables.size(comments));
      }
      writeCommentsRow(comments);
    }
    header = headerInfo.getHeader();
    if (header != null) {
      if (size != null && size != header.size()) {
        throw new IllegalArgumentException(
            "All comments must have the same number of columns as the header: "
                + size
                + " != "
                + header.size());
      }
      writeRow(header);
    }
    return header;
  }

  private void writeRows(
      SequencedSet<String> header, Iterator<SequencedMap<String, String>> table) {
    while (table.hasNext()) {
      SequencedMap<String, String> row = table.next();
      if (header == null) {
        header = row.sequencedKeySet();
      } else if (!header.equals(row.sequencedKeySet())) {
        throw new IllegalArgumentException(
            "All rows must have identical headers in the same order: "
                + header
                + " != "
                + row.sequencedKeySet());
      }
      writeRow(row.sequencedValues());
    }
  }

  private void writeRow(Iterable<String> row) {
    writeContent(composeRow(row));
  }

  private void writeCommentsRow(Iterable<String> row) {
    writeContent(COMMENT_STARTER + composeRow(row));
  }

  private void writeContent(String content) {
    try {
      writer.write(content);
    } catch (IOException e) {
      throw new ExportException("Error while writing TSV data: " + content, e);
    }
  }
}
