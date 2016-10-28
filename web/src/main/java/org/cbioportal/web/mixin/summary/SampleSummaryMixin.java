package org.cbioportal.web.mixin.summary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cbioportal.model.Sample.SampleType;

public class SampleSummaryMixin {

    @JsonIgnore
    private Integer internalId;
    private String stableId;
    private SampleType sampleType;
    @JsonIgnore
    private Integer patientId;
    @JsonProperty("patientId")
    private String patientStableId;
    private String typeOfCancerId;
}
