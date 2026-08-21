package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CaseListDataCount", description = "Represents counts for a case list")
public record CaseListDataCountDTO(String label, String value, Integer count) {}
