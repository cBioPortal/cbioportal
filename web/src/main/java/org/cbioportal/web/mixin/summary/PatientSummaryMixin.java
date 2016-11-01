package org.cbioportal.web.mixin.summary;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PatientSummaryMixin {

    @JsonIgnore
    private Integer internalId;
    private String stableId;
    @JsonIgnore
    private Integer cancerStudyId;
}
