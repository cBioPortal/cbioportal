package org.cbioportal.domain.resource;

/** One distinct value of one metadata key, with how many rows carry it. */
public record ResourceMetadataFacetValue(String metaKey, String value, long count) {}
