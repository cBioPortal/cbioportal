package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MutationSpectrumMixin {

    @JsonProperty("CtoA")
    private Integer ctoA;
    @JsonProperty("CtoG")
    private Integer ctoG;
    @JsonProperty("CtoT")
    private Integer ctoT;
    @JsonProperty("TtoA")
    private Integer ttoA;
    @JsonProperty("TtoC")
    private Integer ttoC;
    @JsonProperty("TtoG")
    private Integer ttoG;
}
