package org.cbioportal.domain.wsi;

/**
 * What the tile capability needs for one servable slide: its upstream-sealed source, which the
 * capability forwards verbatim as its {@code enc} claim, and its thumbnail size. cBioPortal cannot
 * open the sealed source; only the tile server can.
 */
public record WsiSlideSource(
    String slideKey, String sealedSource, int thumbnailWidth, int thumbnailHeight) {}
