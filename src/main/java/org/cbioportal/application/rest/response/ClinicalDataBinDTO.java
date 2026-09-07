package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "ClinicalDataBin", description = "Represents a clinical data bin")
public record ClinicalDataBinDTO(
    String specialValue, BigDecimal start, BigDecimal end, Integer count, String attributeId) {}
