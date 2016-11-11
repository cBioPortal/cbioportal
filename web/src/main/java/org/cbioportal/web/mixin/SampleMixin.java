package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cbioportal.model.Patient;
import org.cbioportal.model.Sample;

public class SampleMixin {

    @JsonIgnore
    private Integer internalId;
    private String stableId;
    private Sample.SampleType sampleType;
    @JsonIgnore
    private Integer patientId;
    @JsonProperty("patientId")
    private String patientStableId;
    private String typeOfCancerId;
    private Patient patient;
}
