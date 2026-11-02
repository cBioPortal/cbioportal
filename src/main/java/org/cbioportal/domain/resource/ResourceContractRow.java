package org.cbioportal.domain.resource;

/**
 * One distinct {@code resource_definition.custom_metadata} value in scope for a request, with the
 * lowest study identifier that carries it.
 *
 * <p>A cohort can span studies, and the contract is declared per (resource, study), so one request
 * can turn up several. Studies that have rows for the resource but declare no contract are reported
 * too, as a blank {@link #customMetadata()}: whether every study in scope declares one decides
 * whether the contract can be treated as the complete column list.
 */
public record ResourceContractRow(String customMetadata, String firstStudy) {

  public boolean declared() {
    return customMetadata != null && !customMetadata.isBlank();
  }
}
