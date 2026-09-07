package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "GenomicDataBin", description = "Represents a genomic data bin")
public record GenomicDataBinDTO(
    String specialValue,
    BigDecimal start,
    BigDecimal end,
    Integer count,
    String hugoGeneSymbol,
    String profileType) {}
