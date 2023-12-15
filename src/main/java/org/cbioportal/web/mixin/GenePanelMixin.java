package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GenePanelMixin {

    @JsonIgnore
    private Integer internalId;
    @JsonProperty("genePanelId")
    private String stableId;
}
