package org.cbioportal.legacy.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.legacy.model.util.Select;

public class GeneFilterQuery extends BaseAlterationFilter implements Serializable {

  private String hugoGeneSymbol;
  private Integer entrezGeneId;
  private List<CNA> alterations;

  public GeneFilterQuery() {}

  public GeneFilterQuery(
      String hugoGeneSymbol,
      Integer entrezGeneId,
      List<CNA> alterations,
      boolean includeDriver,
      boolean includeVUS,
      boolean includeUnknownOncogenicity,
      Select<String> tiersSelect,
      boolean includeUnknownTier,
      boolean includeGermline,
      boolean includeSomatic,
      boolean includeUnknownStatus) {
    super(
        includeDriver,
        includeVUS,
        includeUnknownOncogenicity,
        includeGermline,
        includeSomatic,
        includeUnknownStatus,
        tiersSelect,
        includeUnknownTier);
    this.hugoGeneSymbol = hugoGeneSymbol;
    this.entrezGeneId = entrezGeneId;
    this.alterations = alterations;
  }

  /**
   * Deserializes a GeneFilterQuery from a legacy string format used in stored dynamic virtual
   * studies (e.g. "BRAF" or "BRAF:AMP"). All alteration inclusion flags default to {@code true}
   * (include everything), matching the behaviour of {@link
   * BaseAlterationFilter#BaseAlterationFilter()}.
   */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static GeneFilterQuery fromString(String geneQueryString) {
    String[] parts = geneQueryString.trim().split(":");
    String hugoGeneSymbol = parts[0].trim();
    if (hugoGeneSymbol.isEmpty()) {
      throw new IllegalArgumentException(
          "Cannot parse gene query string: '" + geneQueryString + "'");
    }
    List<CNA> alterations = new ArrayList<>();
    for (int i = 1; i < parts.length; i++) {
      for (String token : parts[i].trim().split("\\s+")) {
        if (!token.isEmpty()) {
          alterations.add(CNA.valueOf(token.toUpperCase()));
        }
      }
    }
    GeneFilterQuery query = new GeneFilterQuery();
    query.setHugoGeneSymbol(hugoGeneSymbol);
    query.setAlterations(alterations.isEmpty() ? null : alterations);
    return query;
  }

  public String getHugoGeneSymbol() {
    return hugoGeneSymbol;
  }

  public void setHugoGeneSymbol(String hugoGeneSymbol) {
    this.hugoGeneSymbol = hugoGeneSymbol;
  }

  public Integer getEntrezGeneId() {
    return entrezGeneId;
  }

  public void setEntrezGeneId(int entrezGeneId) {
    this.entrezGeneId = entrezGeneId;
  }

  public List<CNA> getAlterations() {
    return alterations;
  }

  public void setAlterations(List<CNA> alterations) {
    this.alterations = alterations;
  }
}
