package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AscnSegment", description = "Represents an allele-specific copy number segment")
public record AscnSegmentDTO(
    Long segId,
    Long cancerStudyId,
    Long sampleId,
    Integer tcn,
    Integer lcn,
    Double cellularFraction,
    Double purity,
    Double ploidy,
    Double dipLogR) {}
