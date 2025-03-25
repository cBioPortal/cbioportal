package org.cbioportal.legacy.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cbioportal.legacy.model.CancerStudy;

public class MolecularProfileMixin {

    @JsonIgnore
    private Integer molecularProfileId;
    @JsonProperty("molecularProfileId")
    private String stableId;
    @JsonIgnore
    private Integer cancerStudyId;
    @JsonProperty("studyId")
    private String cancerStudyIdentifier;
    @JsonProperty("study")
    private CancerStudy cancerStudy;

}
