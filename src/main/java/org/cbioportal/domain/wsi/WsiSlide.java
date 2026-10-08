package org.cbioportal.domain.wsi;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * One slide in the browser-facing hierarchy. Slides are addressed only by the opaque {@code
 * slideKey}; the server-side image identifier, slide barcode and resource-data row identifiers are
 * never exposed.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record WsiSlide(
    String slideKey,
    String stainName,
    String stainGroup,
    boolean isHne,
    boolean isIhc,
    String magnification,
    Long fileSizeBytes,
    boolean canServeTiles,
    String slideType,
    String sampleId,
    String matchLevel,
    String specimenKey) {}
