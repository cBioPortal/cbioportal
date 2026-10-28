package org.cbioportal.domain.ascn;

import java.io.Serializable;

public record AscnSegment(
    Long segId,
    Long cancerStudyId,
    Long sampleId,
    Integer tcn,
    Integer lcn,
    Double cellularFraction,
    Double purity,
    Double ploidy,
    Double dipLogR)
    implements Serializable {}
