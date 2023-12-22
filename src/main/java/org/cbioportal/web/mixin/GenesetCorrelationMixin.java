package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GenesetCorrelationMixin {

    @JsonProperty("expressionGeneticProfileId")
    private String expressionMolecularProfileId;
    @JsonProperty("zScoreGeneticProfileId")
    private String zScoreMolecularProfileId;
}
