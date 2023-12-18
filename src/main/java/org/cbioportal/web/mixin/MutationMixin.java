package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import io.swagger.v3.oas.annotations.media.Schema;

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

    @Schema(type = "java.util.Map")
    @JsonProperty("namespaceColumns")
    private Object annotationJSON;
}
