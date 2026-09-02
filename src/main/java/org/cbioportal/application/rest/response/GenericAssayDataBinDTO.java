package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "GenericAssayDataBin", description = "Represents a generic assay data bin")
public record GenericAssayDataBinDTO(
    String specialValue,
    BigDecimal start,
    BigDecimal end,
    Integer count,
    String stableId,
    String profileType) {}
