package org.cbioportal.legacy.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ClinicalEventDataMixin {

    @JsonIgnore
    private Long clinicalEventId;
}
