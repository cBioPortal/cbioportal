package org.cbioportal.infrastructure.repository.clickhouse.resource;

/** Group and row totals, read on their own only when a page falls past the last group. */
public record ResourceTableGroupTotals(long totalGroups, long totalRows) {}
