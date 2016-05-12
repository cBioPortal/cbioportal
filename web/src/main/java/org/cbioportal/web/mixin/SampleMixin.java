package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.cbioportal.model.Patient;
import org.cbioportal.model.SampleType;
import org.cbioportal.model.TypeOfCancer;

public class SampleMixin {

    private Integer internalId;
    private String stableId;
    private SampleType sampleType;
    private Integer patientId;
    private String typeOfCancerId;

    @JsonUnwrapped
    private TypeOfCancer typeOfCancer;

    @JsonUnwrapped
    private Patient patient;
}
