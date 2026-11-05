package org.cbioportal.domain.resource;

/**
 * One requested resource as one requested study knows it, read from its definition and from the
 * sorting keys of {@code resource} and {@code resource_compact_derived}.
 *
 * @param resourceId the resource
 * @param studyId the study, by identifier
 * @param customMetadata the study's {@code custom_metadata} contract for the resource, blank when
 *     it declares none or does not define the resource at all
 * @param defined whether the study has a definition of the resource
 * @param hasRows whether the study holds rows of the resource in {@code resource}
 * @param compactCurrent whether the compact table's copy of the pair is current: it holds as many
 *     rows as {@code resource} does, and they were copied under the contract the definition
 *     declares now. False when the derived tables were not repopulated after the study's rows or
 *     its contract changed, or when the pair is not copied at all because it declares no contract.
 */
public record ResourceScopeRow(
    String resourceId,
    String studyId,
    String customMetadata,
    boolean defined,
    boolean hasRows,
    boolean compactCurrent) {

  public boolean declared() {
    return customMetadata != null && !customMetadata.isBlank();
  }
}
