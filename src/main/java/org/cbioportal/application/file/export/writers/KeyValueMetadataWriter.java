package org.cbioportal.application.file.export.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.SequencedMap;
import org.cbioportal.application.file.export.ExportException;

/** Serializes key-value metadata to cBioPortal metadata format e.g. meta_study.txt */
public class KeyValueMetadataWriter {

  private final Writer writer;

  /**
   * @param writer - the writer to write the key-value metadata to e.g. StringWriter, FileWriter
   */
  public KeyValueMetadataWriter(Writer writer) {
    this.writer = writer;
  }

  private static String composeKeyValueLine(String key, String value) {
    return key + ": " + (value == null ? "" : value.replace("\n", "\\n")) + "\n";
  }

  /** Write a stream of key-value pairs to the writer */
  public void write(SequencedMap<String, String> metadata) {
    for (Map.Entry<String, String> entry : metadata.entrySet()) {
      if (entry.getValue() == null) {
        continue;
      }
      try {
        writer.write(composeKeyValueLine(entry.getKey(), entry.getValue()));
      } catch (IOException e) {
        throw new ExportException(
            "Error while writing key-value metadata for key "
                + entry.getKey()
                + " and value "
                + entry.getValue(),
            e);
      }
    }
  }
}
