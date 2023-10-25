package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClinicalAttributeMixin {

    @JsonProperty("clinicalAttributeId")
    private String attrId;
    @JsonIgnore
    private Integer cancerStudyId;
    @JsonProperty("studyId")
    private String cancerStudyIdentifier;
}
