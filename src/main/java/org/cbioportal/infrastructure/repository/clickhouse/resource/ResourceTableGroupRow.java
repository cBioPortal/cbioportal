package org.cbioportal.infrastructure.repository.clickhouse.resource;

/**
 * One group of a page as the page query returns it, with the totals over every group and the
 * located group's position repeated on each row, so all of them come back in one round trip.
 */
public record ResourceTableGroupRow(
    String studyId,
    String patientId,
    String sampleId,
    long rowCount,
    long totalGroups,
    long totalRows,
    Long locatedIndex) {}
