package org.cbioportal.domain.resource;

/**
 * One requested resource as one requested study knows it, read from its definition and from the
 * sorting key of {@code resource}.
 *
 * @param resourceId the resource
 * @param studyId the study, by identifier
 * @param customMetadata the study's {@code custom_metadata} contract for the resource, blank when
 *     it declares none or does not define the resource at all
 * @param defined whether the study has a definition of the resource
 * @param mayHoldRows whether the study may hold rows of the resource: true where it holds some, and
 *     taken as true without looking where it declares a contract. False only for a definition with
 *     no contract and no rows.
 */
public record ResourceScopeRow(
    String resourceId,
    String studyId,
    String customMetadata,
    boolean defined,
    boolean mayHoldRows) {

  public boolean declared() {
    return customMetadata != null && !customMetadata.isBlank();
  }
}
