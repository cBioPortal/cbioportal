package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(name = "GenericAssayMeta", description = "Represents generic assay metadata")
public record GenericAssayMetaDTO(
    String stableId, String entityType, Map<String, String> genericEntityMetaProperties) {}
