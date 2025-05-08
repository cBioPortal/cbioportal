package org.cbioportal.legacy.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class CosmicMutation implements Serializable {

  @NotNull private String cosmicMutationId;
  @NotNull private String proteinChange;
  private String keyword;
  @NotNull private Integer count;

  public String getCosmicMutationId() {
    return cosmicMutationId;
  }

  public void setCosmicMutationId(String cosmicMutationId) {
    this.cosmicMutationId = cosmicMutationId;
  }

  public String getProteinChange() {
    return proteinChange;
  }

  public void setProteinChange(String proteinChange) {
    this.proteinChange = proteinChange;
  }

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }
}
