package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cbioportal.model.ClinicalAttribute;

public class ClinicalDataMixin {

    @JsonIgnore
    private Integer internalId;
    @JsonProperty("id")
    private String stableId;
    private String attrId;
    private String attrValue;
    private ClinicalAttribute clinicalAttribute;
}
