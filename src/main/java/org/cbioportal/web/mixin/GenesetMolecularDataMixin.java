package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GenesetMolecularDataMixin {

    @JsonProperty("geneticProfileId")
    private String molecularProfileId;
}
