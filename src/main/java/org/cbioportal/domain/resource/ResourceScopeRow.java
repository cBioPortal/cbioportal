package org.cbioportal.domain.resource;

/**
 * One requested resource as one requested study knows it.
 *
 * @param resourceId the resource
 * @param studyId the study, by identifier
 * @param customMetadata the study's {@code custom_metadata} contract for the resource, blank when
 *     it declares none or does not define the resource at all
 * @param hasRows whether the study may hold rows of the resource: true where it holds some, and
 *     taken as true without looking where it declares a contract, since the compact table copies
 *     whatever rows such a pair has. False only for a definition with no contract and no rows.
 */
public record ResourceScopeRow(
    String resourceId, String studyId, String customMetadata, boolean hasRows) {

  public boolean declared() {
    return customMetadata != null && !customMetadata.isBlank();
  }
}
