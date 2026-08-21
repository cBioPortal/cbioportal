package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AlleleSpecificCopyNumber", description = "Represents allele-specific copy number")
public record AlleleSpecificCopyNumberDTO(
    Integer ascnIntegerCopyNumber,
    String ascnMethod,
    Float ccfExpectedCopiesUpper,
    Float ccfExpectedCopies,
    String clonal,
    Integer minorCopyNumber,
    Integer expectedAltCopies,
    Integer totalCopyNumber) {}
