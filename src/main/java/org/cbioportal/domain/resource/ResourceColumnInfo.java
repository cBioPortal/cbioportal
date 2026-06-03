package org.cbioportal.domain.resource;

public record ResourceColumnInfo(
    String id,
    String label,
    String source,
    String dataType,
    boolean filterable,
    boolean sortable,
    boolean visibleByDefault) {}
