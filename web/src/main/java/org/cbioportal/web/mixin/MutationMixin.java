package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MutationMixin {
    
    @JsonProperty("variantAllele")
    private String tumorSeqAllele;
    @JsonProperty("refseqMrnaId")
    private String oncotatorRefseqMrnaId;
    @JsonProperty("proteinPosStart")
    private Integer oncotatorProteinPosStart;
    @JsonProperty("proteinPosEnd")
    private Integer oncotatorProteinPosEnd;
    @JsonProperty("chr")
    private String chr;
}
