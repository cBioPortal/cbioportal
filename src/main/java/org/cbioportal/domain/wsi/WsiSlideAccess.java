package org.cbioportal.domain.wsi;

/** The tile capability for one slide the caller may view. */
public record WsiSlideAccess(
    String slideKey, String accessToken, String tokenType, int expiresIn) {}
