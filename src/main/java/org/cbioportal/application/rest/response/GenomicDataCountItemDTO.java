package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(
    name = "GenomicDataCountItem",
    description = "Represents genomic data counts for a single item")
public record GenomicDataCountItemDTO(
    String hugoGeneSymbol, String profileType, List<GenomicDataCountDTO> counts) {}
