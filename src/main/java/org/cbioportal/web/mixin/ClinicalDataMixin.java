package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClinicalDataMixin {

    @JsonIgnore
    private Integer internalId;
    @JsonProperty("clinicalAttributeId")
    private String attrId;
    @JsonProperty("value")
    private String attrValue;
}
