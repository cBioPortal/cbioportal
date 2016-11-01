package org.cbioportal.web.mixin.summary;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GeneticDataSummaryMixin {

    @JsonIgnore
    private Integer geneticProfileId;
    private Integer entrezGeneId;
    @JsonIgnore
    private Integer sampleId;
    private String value;
}
