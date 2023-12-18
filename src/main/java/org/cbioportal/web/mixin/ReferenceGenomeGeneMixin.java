package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReferenceGenomeGeneMixin {
    @JsonIgnore
    private Integer referenceGenomeId;
}
