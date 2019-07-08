package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TreatmentMixin {
	
	@JsonIgnore
    private Integer id;
    @JsonProperty("treatmentId")
    private String stableId;
}