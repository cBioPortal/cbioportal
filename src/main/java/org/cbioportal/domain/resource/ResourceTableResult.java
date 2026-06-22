package org.cbioportal.domain.resource;

import java.util.List;
import java.util.Map;

public record ResourceTableResult(
    List<ResourceTableTab> tabs,
    List<ResourceColumnInfo> columns,
    List<ResourceTableRow> rows,
    long totalRowCount,
    long filteredPatientCount,
    long filteredSampleCount,
    Map<String, List<ResourceFacetOption>> facets) {}
