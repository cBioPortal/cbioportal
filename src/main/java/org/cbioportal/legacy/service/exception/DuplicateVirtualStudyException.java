package org.cbioportal.legacy.service.exception;

public class DuplicateVirtualStudyException extends RuntimeException {
  public DuplicateVirtualStudyException(String message) {
    super(message);
  }
}
