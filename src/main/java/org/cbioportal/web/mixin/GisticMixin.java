package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class GisticMixin {

    @JsonIgnore
    private Long gisticRoiId;
    @JsonProperty("studyId")
    private String cancerStudyId;
}
