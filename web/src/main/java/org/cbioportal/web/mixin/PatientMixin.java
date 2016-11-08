package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cbioportal.model.CancerStudy;

public class PatientMixin {

    @JsonIgnore
    private Integer internalId;
    private String stableId;
    @JsonIgnore
    private Integer cancerStudyId;
    @JsonProperty("cancerStudyId")
    private String cancerStudyIdentifier;
    private CancerStudy cancerStudy;
}
