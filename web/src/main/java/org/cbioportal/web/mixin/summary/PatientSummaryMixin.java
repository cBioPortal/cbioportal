package org.cbioportal.web.mixin.summary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PatientSummaryMixin {

    @JsonIgnore
    private Integer internalId;
    private String stableId;
    @JsonIgnore
    private Integer cancerStudyId;
    @JsonProperty("cancerStudyId")
    private String cancerStudyIdentifier;
}
