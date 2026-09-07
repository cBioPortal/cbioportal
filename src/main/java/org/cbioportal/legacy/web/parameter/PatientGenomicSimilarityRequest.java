package org.cbioportal.legacy.web.parameter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request body for the patient genomic similarity endpoint.
 *
 * <p>The caller specifies a mutation molecular profile within a study (e.g. {@code
 * brca_tcga_mutations}), a reference patient, a set of genes to compare on, and how many similar
 * patients to return. Similarity is computed as the Jaccard index over the queried gene set.
 */
public class PatientGenomicSimilarityRequest {

  /**
   * ID of the mutation molecular profile to query (e.g. {@code brca_tcga_mutations}). Determines
   * which mutation dataset is used when building each patient's alteration vector.
   */
  @NotBlank private String molecularProfileId;

  /**
   * Patient ID of the reference patient. The returned results are ranked by their Jaccard
   * similarity to this patient.
   */
  @NotBlank private String referencePatientId;

  /**
   * Hugo gene symbols to include in the comparison. Jaccard similarity is computed only over these
   * genes, so choosing a focused, clinically relevant gene panel typically yields more informative
   * results than using the whole exome. Capped at 200 to prevent runaway queries.
   */
  @NotEmpty
  @Size(max = 200)
  private List<String> hugoGeneSymbols;

  /** Maximum number of similar patients to return, ranked from most to least similar. */
  @NotNull
  @Min(1)
  @Max(500)
  private Integer topN = 10;

  public String getMolecularProfileId() {
    return molecularProfileId;
  }

  public void setMolecularProfileId(String molecularProfileId) {
    this.molecularProfileId = molecularProfileId;
  }

  public String getReferencePatientId() {
    return referencePatientId;
  }

  public void setReferencePatientId(String referencePatientId) {
    this.referencePatientId = referencePatientId;
  }

  public List<String> getHugoGeneSymbols() {
    return hugoGeneSymbols;
  }

  public void setHugoGeneSymbols(List<String> hugoGeneSymbols) {
    this.hugoGeneSymbols = hugoGeneSymbols;
  }

  public Integer getTopN() {
    return topN;
  }

  public void setTopN(Integer topN) {
    this.topN = topN;
  }
}
