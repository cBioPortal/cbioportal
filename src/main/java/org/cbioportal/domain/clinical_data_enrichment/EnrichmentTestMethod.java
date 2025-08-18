package org.cbioportal.domain.clinical_data_enrichment;

/** Enumeration of statistical test methods used for clinical data enrichment analysis. */
public enum EnrichmentTestMethod {

  /**
   * Kruskal-Wallis test - non-parametric method for comparing distributions of numeric data across
   * multiple groups.
   */
  KRUSKAL_WALLIS("Kruskal Wallis Test"),

  /**
   * Wilcoxon rank-sum test - non-parametric method for comparing distributions between two groups.
   */
  WILCOXON("Wilcoxon Test"),

  /**
   * Chi-squared test - method for testing independence between categorical variables across groups.
   */
  CHI_SQUARED("Chi-squared Test");

  private final String displayName;

  EnrichmentTestMethod(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Returns the human-readable display name for this test method.
   *
   * @return the display name used in API responses and UI
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Creates an EnrichmentTestMethod from a display name string.
   *
   * @param displayName the display name to match
   * @return the corresponding EnrichmentTestMethod
   */
  public static EnrichmentTestMethod fromDisplayName(String displayName) {
    if (displayName != null) {
      for (EnrichmentTestMethod method : values()) {
        if (method.displayName.equals(displayName)) {
          return method;
        }
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return displayName;
  }
}
