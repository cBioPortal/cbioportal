package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MutationMixin {

    @JsonIgnore
    private Integer geneticProfileId;
    @JsonProperty("geneticProfileId")
    private String geneticProfileStableId;
    @JsonIgnore
    private Integer sampleId;
    @JsonProperty("sampleId")
    private String sampleStableId;
    @JsonProperty("variantAllele")
    private String tumorSeqAllele;
    @JsonProperty("refseqMrnaId")
    private String oncotatorRefseqMrnaId;
    @JsonProperty("proteinPosStart")
    private Integer oncotatorProteinPosStart;
    @JsonProperty("proteinPosEnd")
    private Integer oncotatorProteinPosEnd;
}
