package org.cbioportal.domain.wsi;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record WsiSlide(
    String imageId,
    String stainName,
    String stainGroup,
    boolean isHne,
    boolean isIhc,
    String magnification,
    Long fileSizeBytes,
    boolean canServeTiles,
    String barcode,
    String slideType,
    String sampleId,
    String matchLevel,
    String specimenKey,
    Integer procedureDateDays,
    String timepointSource) {}
