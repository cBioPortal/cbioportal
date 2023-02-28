package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClinicalAttributeCountMixin {

    @JsonProperty("clinicalAttributeId")
    private String attrId;
}
