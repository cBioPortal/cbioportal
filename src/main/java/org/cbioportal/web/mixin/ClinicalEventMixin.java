package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClinicalEventMixin {

    @JsonIgnore
    private Integer clinicalEventId;
    @JsonProperty("startNumberOfDaysSinceDiagnosis")
    private Integer startDate;
    @JsonProperty("endNumberOfDaysSinceDiagnosis")
    private Integer stopDate;
}
