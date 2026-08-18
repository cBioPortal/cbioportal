package org.cbioportal.application.rest.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    String cytoband) {
  @JsonProperty("uniqueEventKey")
  public String uniqueEventKey() {
    if (entrezGeneId == null || alteration == null) {
      return null;
    }
    return entrezGeneId.toString() + alteration;
  }

  @JsonProperty("hugoGeneSymbols")
  public String[] hugoGeneSymbols() {
    return hugoGeneSymbol == null ? null : new String[] {hugoGeneSymbol};
  }

  @JsonProperty("entrezGeneIds")
  public Integer[] entrezGeneIds() {
    return entrezGeneId == null ? null : new Integer[] {entrezGeneId};
  }
}
