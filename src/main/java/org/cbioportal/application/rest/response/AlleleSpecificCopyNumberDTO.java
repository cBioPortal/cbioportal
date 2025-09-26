package org.cbioportal.application.rest.response;

public record AlleleSpecificCopyNumberDTO(
    Integer ascnIntegerCopyNumber,
    String ascnMethod,
    Float ccfExpectedCopiesUpper,
    Float ccfExpectedCopies,
    String clonal,
    Integer minorCopyNumber,
    Integer expectedAltCopies,
    Integer totalCopyNumber) {}
