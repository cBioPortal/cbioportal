package org.cbioportal.domain.clinical_data_enrichment;

/**
 * Enumeration of statistical test methods used for clinical data enrichment analysis.
 *
 * <p>Each method is appropriate for different data types and number of groups:
 *
 * <ul>
 *   <li><b>KRUSKAL_WALLIS</b>: For numerical data across 3+ groups
 *   <li><b>WILCOXON</b>: For numerical data between exactly 2 groups
 *   <li><b>CHI_SQUARED</b>: For categorical data across any number of groups
 * </ul>
 */
public enum EnrichmentTestMethod {

  /**
   * Kruskal-Wallis test - non-parametric method for comparing distributions of numerical data
   * across multiple (3+) groups.
   *
   * <p>This test determines whether the distributions differ significantly between groups without
   * assuming normal distribution. It's the multi-group extension of the Wilcoxon test.
   */
  KRUSKAL_WALLIS("Kruskal Wallis Test"),

  /**
   * Wilcoxon rank-sum test (Mann-Whitney U test) - non-parametric method for comparing
   * distributions of numerical data between exactly two groups.
   *
   * <p>This test is mathematically equivalent to the Kruskal-Wallis test when there are only 2
   * groups, but is labeled differently for clarity. It's also known as the Mann-Whitney U test.
   */
  WILCOXON("Wilcoxon Test"),

  /**
   * Chi-squared test of independence - method for testing whether categorical variable
   * distributions differ significantly across groups.
   *
   * <p>This test analyzes contingency tables to determine if the distribution of categories varies
   * between groups beyond what would be expected by chance.
   */
  CHI_SQUARED("Chi-squared Test");

  private final String displayName;

  EnrichmentTestMethod(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Returns the human-readable display name for this test method.
   *
   * <p>This name is used in API responses and UI displays.
   *
   * @return the display name (e.g., "Kruskal Wallis Test")
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Creates an EnrichmentTestMethod from a display name string.
   *
   * <p>Performs case-sensitive matching against the display names of all enum values.
   *
   * @param displayName the display name to match
   * @return the corresponding EnrichmentTestMethod, or null if no match found
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

  /**
   * Returns the display name of this test method.
   *
   * @return the display name string
   */
  @Override
  public String toString() {
    return displayName;
  }
}
