package org.cbioportal.application.file.export;

public class ExportException extends RuntimeException {
  public ExportException(String message, Throwable cause) {
    super(message, cause);
  }
}
