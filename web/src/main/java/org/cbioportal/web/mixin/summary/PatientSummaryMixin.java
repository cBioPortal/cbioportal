package org.cbioportal.web.mixin.summary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cbioportal.model.ClinicalData;

import java.util.List;

public class PatientSummaryMixin {

    @JsonIgnore
    private Integer internalId;
    private String stableId;
    @JsonIgnore
    private Integer cancerStudyId;
    private List<ClinicalData> clinicalDataList;
}
