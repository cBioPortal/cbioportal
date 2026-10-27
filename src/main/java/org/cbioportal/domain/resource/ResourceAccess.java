package org.cbioportal.domain.resource;

/** A tile capability for one resource row the caller may read. */
public record ResourceAccess(
    String resourceId, String key, String accessToken, String tokenType, int expiresIn) {}
