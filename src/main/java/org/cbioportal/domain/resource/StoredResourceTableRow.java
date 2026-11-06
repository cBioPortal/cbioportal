package org.cbioportal.domain.resource;

import java.util.Map;

/**
 * A resource table row as stored, together with its own resource definition's {@code url_template}.
 * The use case turns it into the {@link ResourceTableRow} that is served, filling in the URL from
 * the template when the row has none of its own.
 */
public record StoredResourceTableRow(
    String studyId,
    String resourceId,
    String resourceDisplayName,
    String resourceType,
    String patientId,
    String sampleId,
    String url,
    String displayName,
    Map<String, Object> metadata,
    String urlTemplate) {

  /** The served row, with {@code url} in place of the stored one. */
  public ResourceTableRow toRow(String url) {
    return new ResourceTableRow(
        studyId,
        resourceId,
        resourceDisplayName,
        resourceType,
        patientId,
        sampleId,
        url,
        displayName,
        metadata);
  }
}
