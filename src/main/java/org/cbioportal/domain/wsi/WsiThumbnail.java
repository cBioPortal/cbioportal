package org.cbioportal.domain.wsi;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Immutable, pre-rendered thumbnail artifact for one slide. */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record WsiThumbnail(String sourceUrl, int width, int height, String contentType) {}
