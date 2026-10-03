package org.cbioportal.domain.wsi;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Browser-facing access bundle for one authorized slide.
 *
 * <p>This record must never carry the server-side image identifier or any slide/thumbnail object
 * URI. Those values exist only in {@link WsiSlideSource} and inside the encrypted capability.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record WsiSlideAccess(
    String slideKey,
    WsiTileMetadata tileMetadata,
    WsiThumbnail thumbnail,
    String accessToken,
    String tokenType,
    int expiresIn) {}
