package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.cbioportal.model.CancerStudy;

public class PatientMixin {

    private Integer internalId;
    private String stableId;
    private Integer cancerStudyId;

    @JsonUnwrapped
    private CancerStudy cancerStudy;
}
