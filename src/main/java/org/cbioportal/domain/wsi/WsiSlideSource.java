package org.cbioportal.domain.wsi;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Server-side-only pixel source for one servable slide.
 *
 * <p>{@code sealedSource} is the opaque, upstream-sealed slide source (AES-GCM, bound to the slide
 * key) that the tile capability forwards verbatim as its {@code enc} claim. cBioPortal cannot
 * decrypt it, but it is still a capability component: it is excluded from JSON serialization and
 * from {@link #toString()} as a defensive measure, and callers must only return {@link
 * WsiSlideAccess} from REST endpoints.
 */
public record WsiSlideSource(
    String slideKey,
    @JsonIgnore String sealedSource,
    WsiTileMetadata tileMetadata,
    WsiThumbnail thumbnail) {

  @Override
  public String toString() {
    return "WsiSlideSource[slideKey=" + slideKey + ", sealedSource=<redacted>]";
  }
}
