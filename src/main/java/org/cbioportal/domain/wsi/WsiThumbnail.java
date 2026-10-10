package org.cbioportal.domain.wsi;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Browser-facing description of the pre-rendered thumbnail for one slide.
 *
 * <p>The thumbnail object URI is deliberately absent: it is carried only inside the encrypted
 * capability so it never reaches the browser.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record WsiThumbnail(int width, int height, String contentType) {}
