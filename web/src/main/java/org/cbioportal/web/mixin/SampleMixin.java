package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.cbioportal.model.Patient;

public class SampleMixin {

    private Integer internalId;

    private String sampleType;

    private String stableId;

    @JsonUnwrapped
    private Patient patient;
}
