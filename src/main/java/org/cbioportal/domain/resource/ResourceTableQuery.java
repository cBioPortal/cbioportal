package org.cbioportal.domain.resource;

import java.util.List;

public record ResourceTableQuery(
    List<String> studyIds,
    String resourceId,
    List<String> patientIds,
    List<String> sampleIds,
    String search,
    int pageNumber,
    int pageSize,
    String sortBy,
    String direction,
    List<ResourceColumnFilter> filters) {

  public int offset() {
    return Math.max(pageNumber, 0) * limit();
  }

  public int limit() {
    return Math.max(pageSize, 0);
  }
}
