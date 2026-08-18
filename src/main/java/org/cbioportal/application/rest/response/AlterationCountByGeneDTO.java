package org.cbioportal.application.rest.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Set;

@Schema(
    name = "AlterationCountByGene",
    description = "Represents alteration counts grouped by gene")
public record AlterationCountByGeneDTO(
    Integer numberOfAlteredCases,
    Integer numberOfAlteredCasesOnPanel,
    Integer totalCount,
    Integer numberOfProfiledCases,
    Set<String> matchingGenePanelIds,
    Integer entrezGeneId,
    String hugoGeneSymbol,
    BigDecimal qValue) {
  @JsonProperty("uniqueEventKey")
  @Schema(hidden = true)
  public String uniqueEventKey() {
    return hugoGeneSymbol;
  }

  @JsonProperty("hugoGeneSymbols")
  @Schema(hidden = true)
  public String[] hugoGeneSymbols() {
    return hugoGeneSymbol == null ? null : new String[] {hugoGeneSymbol};
  }

  @JsonProperty("entrezGeneIds")
  @Schema(hidden = true)
  public Integer[] entrezGeneIds() {
    return entrezGeneId == null ? null : new Integer[] {entrezGeneId};
  }
}
