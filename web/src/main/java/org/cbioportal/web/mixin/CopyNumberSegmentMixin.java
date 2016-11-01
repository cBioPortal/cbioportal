package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CopyNumberSegmentMixin {

    @JsonIgnore
    private Integer segId;
    private Integer cancerStudyId;
    private Integer sampleId;
    private String chr;
    private Integer start;
    private Integer end;
    private Integer numProbes;
    private Double segmentMean;
}
