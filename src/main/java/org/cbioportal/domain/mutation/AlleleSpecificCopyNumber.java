package org.cbioportal.domain.mutation;

public record AlleleSpecificCopyNumber(
    Integer ascnIntegerCopyNumber,
    String ascnMethod,
    Float ccfExpectedCopiesUpper,
    Float ccfExpectedCopies,
    String clonal,
    Integer minorCopyNumber,
    Integer expectedAltCopies,
    Integer totalCopyNumber
) {
}
