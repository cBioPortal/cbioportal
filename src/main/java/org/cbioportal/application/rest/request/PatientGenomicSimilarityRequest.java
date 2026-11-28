package org.cbioportal.application.rest.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request body of the patient genomic similarity endpoint: the mutation profile to compare patients
 * on, the reference patient, the genes to compare them on and how many similar patients to return.
 */
public class PatientGenomicSimilarityRequest {

  /** Stable ID of a mutation profile of the study, e.g. {@code brca_tcga_mutations}. */
  @NotBlank private String molecularProfileId;

  /** ID of the patient that the other patients are compared with. */
  @NotBlank private String referencePatientId;

  /**
   * Hugo symbols of the genes to compare the patients on, matched case-insensitively. A focused,
   * clinically relevant gene set gives more informative results than a whole exome.
   */
  @NotEmpty
  @Size(max = 200)
  private List<@NotBlank String> hugoGeneSymbols;

  /** Maximum number of similar patients to return. */
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
