package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ClinicalEventDataMixin {

    @JsonIgnore
    private Integer clinicalEventId;
}
