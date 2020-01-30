package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MutSigMixin {
    @JsonIgnore
    private Integer cancerStudyId;

    @JsonProperty("studyId")
    private String cancerStudyIdentifier;

    @JsonIgnore
    private Integer numbasescovered;

    @JsonProperty("numberOfMutations")
    private Integer nummutations;
}
