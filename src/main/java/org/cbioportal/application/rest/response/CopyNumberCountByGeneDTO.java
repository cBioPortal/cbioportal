package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Set;

@Schema(
    name = "CopyNumberCountByGene",
    description = "Represents copy number counts grouped by gene")
public record CopyNumberCountByGeneDTO(
    Integer numberOfAlteredCases,
    Integer numberOfAlteredCasesOnPanel,
    Integer totalCount,
    Integer numberOfProfiledCases,
    Set<String> matchingGenePanelIds,
    Integer entrezGeneId,
    String hugoGeneSymbol,
    BigDecimal qValue,
    Integer alteration,
    String cytoband) {}
