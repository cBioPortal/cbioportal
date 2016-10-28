package org.cbioportal.web.mixin.summary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GeneticDataSummaryMixin {

    @JsonIgnore
    private Integer geneticProfileId;
    @JsonProperty("geneticProfileId")
    private String geneticProfileStableId;
    private Integer entrezGeneId;
    @JsonIgnore
    private Integer sampleId;
    @JsonProperty("sampleStableId")
    private String sampleStableId;
    private String value;
}
