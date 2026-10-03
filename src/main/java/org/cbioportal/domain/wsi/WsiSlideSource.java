package org.cbioportal.domain.wsi;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Server-side-only pixel source for one servable slide.
 *
 * <p>The image identifier and object URIs are needed to mint the encrypted tile capability, but
 * must never be serialized to a browser or written to logs. The identifying components are excluded
 * from JSON serialization and from {@link #toString()} as a defensive measure; callers must still
 * only return {@link WsiSlideAccess} from REST endpoints.
 */
public record WsiSlideSource(
    String slideKey,
    @JsonIgnore String imageId,
    @JsonIgnore String sourceUrl,
    @JsonIgnore String thumbnailSourceUrl,
    WsiTileMetadata tileMetadata,
    WsiThumbnail thumbnail) {

  @Override
  public String toString() {
    return "WsiSlideSource[slideKey=" + slideKey + ", imageId=<redacted>, sources=<redacted>]";
  }
}
