package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GenesetMixin {

    @JsonIgnore
    private Integer internalId;
}
