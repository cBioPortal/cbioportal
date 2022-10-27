package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MutationMixin {

    @JsonProperty("variantAllele")
    private String tumorSeqAllele;
    @JsonProperty("refseqMrnaId")
    private String mutationRefseqMrnaId;
    @JsonProperty("proteinPosStart")
    private Integer mutationProteinPosStart;
    @JsonProperty("proteinPosEnd")
    private Integer mutationProteinPosEnd;
    @JsonProperty("chr")
    private String chr;
    @JsonProperty("namespaceColumns")
    private String annotationJSON;
}
