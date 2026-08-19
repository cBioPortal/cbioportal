package org.cbioportal.legacy.web.parameter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request body for {@code POST /study/embeddings}. All three fields are required: {@code studyIds}
 * must be non-empty, and {@code reductionTechnique} / {@code embeddingType} must both be specified
 * so the query narrows to a single embedding definition. Without both, a study with more than one
 * embedding could match multiple definitions at once.
 */
public class EmbeddingFilter {
  @NotNull
  @Size(min = 1)
  private List<String> studyIds;

  @NotNull private String reductionTechnique;

  @NotNull private String embeddingType;

  public List<String> getStudyIds() {
    return studyIds;
  }

  public void setStudyIds(List<String> studyIds) {
    this.studyIds = studyIds;
  }

  public String getReductionTechnique() {
    return reductionTechnique;
  }

  public void setReductionTechnique(String reductionTechnique) {
    this.reductionTechnique = reductionTechnique;
  }

  public String getEmbeddingType() {
    return embeddingType;
  }

  public void setEmbeddingType(String embeddingType) {
    this.embeddingType = embeddingType;
  }
}
