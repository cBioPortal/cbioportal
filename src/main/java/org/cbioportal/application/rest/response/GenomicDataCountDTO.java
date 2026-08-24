package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "GenomicDataCount", description = "Represents a genomic data count")
public record GenomicDataCountDTO(
    String label, String value, Integer count, Integer uniqueCount, List<String> sampleIds) {}
