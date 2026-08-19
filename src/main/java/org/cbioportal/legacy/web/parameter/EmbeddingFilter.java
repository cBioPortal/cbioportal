package org.cbioportal.legacy.web.parameter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request body for {@code POST /study/embeddings}. {@code studyIds} is required and must be
 * non-empty; {@code reductionTechnique} and {@code embeddingType} are optional filters — when
 * omitted, the query matches all values for that field.
 */
public class EmbeddingFilter {
  @NotNull
  @Size(min = 1)
  private List<String> studyIds;

  private String reductionTechnique;

  private String embeddingType;

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
