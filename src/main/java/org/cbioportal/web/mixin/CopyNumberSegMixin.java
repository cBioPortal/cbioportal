package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CopyNumberSegMixin {

    @JsonIgnore
    private Long segId;
    @JsonIgnore
    private Integer cancerStudyId;
    @JsonProperty("studyId")
    private String cancerStudyIdentifier;
    @JsonIgnore
    private Integer sampleId;
    @JsonProperty("sampleId")
    private String sampleStableId;
    @JsonProperty("numberOfProbes")
    private Integer numProbes;
    @JsonProperty("chromosome")
    private String chr;
}
