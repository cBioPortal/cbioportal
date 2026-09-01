package org.cbioportal.domain.resource;

/** The three counts a resource-table response reports, gathered in a single pass over the rows. */
public record ResourceTableCounts(long rowCount, long patientCount, long sampleCount) {

  public static ResourceTableCounts empty() {
    return new ResourceTableCounts(0L, 0L, 0L);
  }
}
