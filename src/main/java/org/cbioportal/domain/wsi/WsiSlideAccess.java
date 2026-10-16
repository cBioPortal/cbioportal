package org.cbioportal.domain.wsi;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Browser-facing access bundle for one authorized slide.
 *
 * <p>This record must never carry the sealed slide source on its own; it reaches the browser only
 * as the {@code enc} claim inside the signed capability.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record WsiSlideAccess(
    String slideKey,
    WsiTileMetadata tileMetadata,
    WsiThumbnail thumbnail,
    String accessToken,
    String tokenType,
    int expiresIn) {}
