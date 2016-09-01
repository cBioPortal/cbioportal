package org.cbioportal.web.mixin.summary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.summary.SampleSummary;

import java.util.List;

public class SampleSummaryMixin {

    @JsonIgnore
    private Integer internalId;
    private String stableId;
    private SampleSummary.SampleType sampleType;
    @JsonIgnore
    private Integer patientId;
    private String typeOfCancerId;
    private List<ClinicalData> clinicalDataList;
}
