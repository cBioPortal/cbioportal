package org.cbioportal.web.mixin.summary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cbioportal.model.Sample.SampleType;

public class SampleSummaryMixin {

    @JsonIgnore
    private Integer internalId;
    private String stableId;
    private SampleType sampleType;
    @JsonIgnore
    private Integer patientId;
    private String typeOfCancerId;
}
