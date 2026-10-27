package org.cbioportal.domain.resource;

/**
 * What a tile capability needs for one resource row: its serving key, its upstream-sealed source
 * (copied verbatim into the capability's {@code enc} claim; only the tile server can open it) and
 * its thumbnail size.
 */
public record ResourceServing(
    String resourceId, String key, String sealedSource, int thumbnailWidth, int thumbnailHeight) {}
