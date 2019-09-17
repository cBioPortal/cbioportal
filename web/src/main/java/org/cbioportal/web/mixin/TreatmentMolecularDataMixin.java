package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TreatmentMolecularDataMixin {

    @JsonProperty("geneticProfileId")
    private String molecularProfileId;
}
