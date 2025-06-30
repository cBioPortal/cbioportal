package org.cbioportal.application.file.export.services;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.cbioportal.application.file.utils.FileWriterFactory;

public class ZipOutputStreamWriterService implements FileWriterFactory, Closeable {

  /** The delimiter used to separate paths in the zip file. */
  public static final String PATH_DELIMITER = "/";

  private final OutputStream outputStream;
  private final ZipOutputStream zipOutputStream;
  private String basePath;

  public ZipOutputStreamWriterService(OutputStream outputStream) {
    this.outputStream = outputStream;
    this.zipOutputStream = new ZipOutputStream(outputStream);
  }

  @Override
  public void setBasePath(String basePath) {
    if (basePath != null) {
      this.basePath = basePath.endsWith(PATH_DELIMITER) ? basePath : basePath + PATH_DELIMITER;
    } else {
      this.basePath = null;
    }
  }

  @Override
  public String getBasePath() {
    return basePath;
  }

  @Override
  public Writer newWriter(String name) throws IOException {
    return new ZipEntryOutputStreamWriter(
        basePath == null ? name : (basePath + name), zipOutputStream);
  }

  @Override
  public void fail(Exception e) {
    // corrupt the whole zip file intentionally
    RuntimeException primaryException = new RuntimeException(e);
    try {
      outputStream.write(("ERROR: " + e.getMessage()).getBytes());
      outputStream.flush();
    } catch (IOException ex) {
      primaryException.addSuppressed(ex);
    }
    // and stop the following writes
    throw primaryException;
  }

  @Override
  public void close() throws IOException {
    zipOutputStream.close();
  }

  static class ZipEntryOutputStreamWriter extends OutputStreamWriter {
    private final ZipOutputStream zipOutputStream;

    public ZipEntryOutputStreamWriter(String name, ZipOutputStream zipOutputStream)
        throws IOException {
      super(zipOutputStream);
      zipOutputStream.putNextEntry(new ZipEntry(name));
      this.zipOutputStream = zipOutputStream;
    }

    @Override
    public void close() throws IOException {
      flush();
      zipOutputStream.closeEntry();
    }
  }
}
