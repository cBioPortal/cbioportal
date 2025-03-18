package org.cbioportal.legacy.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ClinicalDataCountMixin {

    @JsonIgnore
    private String attributeId;
}
