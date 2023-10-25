package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MutationMixin {

    @JsonProperty("variantAllele")
    private String tumorSeqAllele;
    @JsonProperty("refseqMrnaId")
    private String refseqMrnaId;
    @JsonProperty("proteinPosStart")
    private Integer proteinPosStart;
    @JsonProperty("proteinPosEnd")
    private Integer proteinPosEnd;
    @JsonProperty("chr")
    private String chr;
    @JsonProperty("namespaceColumns")
    private String annotationJSON;
}
