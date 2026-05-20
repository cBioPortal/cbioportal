package org.cbioportal.application.rest.response;

import java.math.BigDecimal;

public record ClinicalDataEnrichmentDTO(
    ClinicalAttributeDTO clinicalAttribute, BigDecimal score, String method, BigDecimal pValue) {}
