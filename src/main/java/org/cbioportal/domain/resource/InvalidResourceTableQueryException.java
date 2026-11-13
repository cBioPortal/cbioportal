package org.cbioportal.domain.resource;

/** A resource-table request that cannot be answered as asked; reported as a bad request. */
public class InvalidResourceTableQueryException extends RuntimeException {

  public InvalidResourceTableQueryException(String message) {
    super(message);
  }
}
