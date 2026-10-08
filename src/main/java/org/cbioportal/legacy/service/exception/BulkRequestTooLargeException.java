package org.cbioportal.legacy.service.exception;

/** A molecular data request would return more data than the configured bulk-request limits. */
public class BulkRequestTooLargeException extends RuntimeException {

  public BulkRequestTooLargeException(String message) {
    super(message);
  }
}
