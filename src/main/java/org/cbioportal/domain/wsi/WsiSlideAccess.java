package org.cbioportal.domain.wsi;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Browser-facing access bundle for one authorized slide. */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record WsiSlideAccess(
    String imageId,
    String sourceUrl,
    WsiTileMetadata tileMetadata,
    WsiThumbnail thumbnail,
    String accessToken,
    String tokenType,
    int expiresIn) {}
