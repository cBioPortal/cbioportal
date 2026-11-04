package org.cbioportal.infrastructure.repository.clickhouse.resource;

/**
 * The value counts of one group on the page.
 *
 * @param counts a JSON object keyed by requested column id, each holding value-to-count pairs
 */
public record ResourceGroupCountsRow(String studyId, String groupKey, String counts) {}
