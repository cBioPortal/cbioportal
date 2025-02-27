package org.cbioportal.domain.clinical_data_enrichment.util;

import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.DataTable2D;
import com.datumbox.framework.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.common.dataobjects.TransposeDataCollection;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;
import com.datumbox.framework.core.statistics.nonparametrics.independentsamples.Chisquare;
import com.datumbox.framework.core.statistics.nonparametrics.independentsamples.KruskalWallis;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cbioportal.domain.clinical_attributes.ClinicalAttribute;
import org.cbioportal.domain.clinical_data_enrichment.ClinicalDataEnrichment;
import org.cbioportal.domain.clinical_data_enrichment.EnrichmentTestMethod;
import org.cbioportal.legacy.model.ClinicalDataCountItem;

/**
 * Utility class for performing statistical tests on clinical data for enrichment analysis.
 *
 * <p>This class provides static methods for conducting non-parametric statistical tests to identify
 * clinically significant differences between sample groups. It uses the Datumbox Framework for
 * statistical computations.
 *
 * <p>Supported tests:
 *
 * <ul>
 *   <li><b>Kruskal-Wallis test</b>: Non-parametric test for comparing numerical data across 3+
 *       groups
 *   <li><b>Wilcoxon test</b>: Special case of Kruskal-Wallis for comparing 2 groups
 *   <li><b>Chi-squared test</b>: Test for comparing categorical data distributions across groups
 * </ul>
 */
public final class ClinicalDataEnrichmentUtil {

  private ClinicalDataEnrichmentUtil() {}

  /**
   * Internal data structure holding prepared data for Kruskal-Wallis test.
   *
   * @param transposeCollection collection of numerical values organized by group
   * @param distinctValues set of all unique values across groups (used to validate test
   *     applicability)
   */
  private record KruskalWallisTestData(
      TransposeDataCollection transposeCollection, Set<Double> distinctValues) {}

  /**
   * Internal data structure holding prepared data for Chi-squared test.
   *
   * @param dataTable contingency table with group × category counts
   * @param allCategories set of all unique categories across groups
   */
  private record ChiSquaredTestData(DataTable2D dataTable, Set<String> allCategories) {}

  /**
   * Performs Kruskal-Wallis test on numerical clinical data across multiple groups.
   *
   * <p>This non-parametric test determines if there are statistically significant differences in
   * the distributions of numerical clinical attributes between groups. For 2 groups, the test is
   * labeled as Wilcoxon (which is mathematically equivalent to Kruskal-Wallis for 2 groups).
   *
   * <p>The test is only performed if:
   *
   * <ul>
   *   <li>There are at least 2 groups with data
   *   <li>There are at least 2 distinct values across all groups
   * </ul>
   *
   * @param numericalAttributes list of numerical clinical attributes to test
   * @param dataByGroupAndAttribute list of maps (one per group) containing attribute ID → list of
   *     values
   * @return list of enrichments with p-values and chi-square statistics for significant attributes
   */
  public static List<ClinicalDataEnrichment> performKruskalWallisTest(
      List<ClinicalAttribute> numericalAttributes,
      List<Map<String, List<Double>>> dataByGroupAndAttribute) {

    List<ClinicalDataEnrichment> enrichments = new ArrayList<>();

    // Determine test name based on number of groups
    EnrichmentTestMethod method =
        dataByGroupAndAttribute.size() == 2
            ? EnrichmentTestMethod.WILCOXON
            : EnrichmentTestMethod.KRUSKAL_WALLIS;

    for (ClinicalAttribute clinicalAttribute : numericalAttributes) {
      String attributeId = clinicalAttribute.attrId();

      // Prepare test data
      KruskalWallisTestData testData =
          buildKruskalWallisTestData(attributeId, dataByGroupAndAttribute);

      // Perform test only if there are more than one group and at least two distinct values
      if (testData.transposeCollection().keySet().size() > 1
          && testData.distinctValues().size() > 1) {
        double pValue = KruskalWallis.getPvalue(testData.transposeCollection());
        if (!Double.isNaN(pValue)) {
          int groupCount = testData.transposeCollection().keySet().size();
          ClinicalDataEnrichment clinicalEnrichment =
              new ClinicalDataEnrichment(
                  clinicalAttribute,
                  BigDecimal.valueOf(
                      ContinuousDistributions.chisquareInverseCdf(pValue, groupCount - 1)),
                  method,
                  BigDecimal.valueOf(pValue));
          enrichments.add(clinicalEnrichment);
        }
      }
    }

    return enrichments;
  }

  /**
   * Performs Chi-squared test on categorical clinical data across multiple groups.
   *
   * <p>This test determines if there are statistically significant differences in the distribution
   * of categorical clinical attributes between groups. It builds a contingency table with groups as
   * rows and categories as columns.
   *
   * <p>The test is only performed if:
   *
   * <ul>
   *   <li>There are at least 2 categories (excluding NA values)
   *   <li>There are at least 2 groups with valid data
   *   <li>The contingency table is valid (no negative counts, sufficient data)
   * </ul>
   *
   * @param categoricalAttributes list of categorical clinical attributes to test
   * @param countsByGroupAndAttribute list of maps (one per group) containing attribute ID → count
   *     item with category frequencies
   * @return list of enrichments with p-values and chi-square statistics for significant attributes
   */
  public static List<ClinicalDataEnrichment> performChiSquaredTest(
      List<ClinicalAttribute> categoricalAttributes,
      List<Map<String, ClinicalDataCountItem>> countsByGroupAndAttribute) {

    List<ClinicalDataEnrichment> enrichments = new ArrayList<>();

    for (ClinicalAttribute clinicalAttribute : categoricalAttributes) {
      String attributeId = clinicalAttribute.attrId();

      // Prepare test data
      ChiSquaredTestData testData = buildChiSquaredTestData(attributeId, countsByGroupAndAttribute);

      // Perform test only if there are multiple categories and valid data table
      if (testData.allCategories().size() > 1
          && testData.dataTable().size() > 1
          && testData.dataTable().isValid()) {
        double pValue = Chisquare.getPvalue(testData.dataTable());
        ClinicalDataEnrichment clinicalEnrichment =
            new ClinicalDataEnrichment(
                clinicalAttribute,
                BigDecimal.valueOf(Chisquare.getScoreValue(testData.dataTable())),
                EnrichmentTestMethod.CHI_SQUARED,
                BigDecimal.valueOf(pValue));
        enrichments.add(clinicalEnrichment);
      }
    }

    return enrichments;
  }

  /**
   * Builds test data structure for Kruskal-Wallis test.
   *
   * <p>Extracts numerical values for the specified attribute from each group and organizes them
   * into a TransposeDataCollection format required by the Datumbox statistical library.
   *
   * @param attributeId the clinical attribute ID to extract data for
   * @param dataByGroupAndAttribute list of maps containing data for all attributes by group
   * @return test data containing transposed collection and set of distinct values
   */
  private static KruskalWallisTestData buildKruskalWallisTestData(
      String attributeId, List<Map<String, List<Double>>> dataByGroupAndAttribute) {

    TransposeDataCollection transposeCollection = new TransposeDataCollection();
    Set<Double> distinctValues = new HashSet<>();
    int groupIndex = 0;

    // Single pass: extract data and build structures simultaneously
    for (Map<String, List<Double>> groupData : dataByGroupAndAttribute) {
      List<Double> values = groupData.get(attributeId);
      if (values != null && !values.isEmpty()) {
        // Convert List<Double> to Collection<Object> for FlatDataCollection compatibility
        Collection<Object> objectValues = new ArrayList<>(values);
        transposeCollection.put(groupIndex++, new FlatDataCollection(objectValues));
        distinctValues.addAll(values);
      }
    }

    return new KruskalWallisTestData(transposeCollection, distinctValues);
  }

  /**
   * Builds test data structure for Chi-squared test.
   *
   * <p>Extracts all categories and builds a contingency table with group counts for each category.
   *
   * @param attributeId the clinical attribute ID to extract data for
   * @param countsByGroupAndAttribute list of maps containing count items for all attributes by
   *     group
   * @return test data containing contingency table and set of all categories
   */
  private static ChiSquaredTestData buildChiSquaredTestData(
      String attributeId, List<Map<String, ClinicalDataCountItem>> countsByGroupAndAttribute) {

    Set<String> allCategories = extractAllCategories(attributeId, countsByGroupAndAttribute);
    DataTable2D dataTable =
        buildContingencyTable(attributeId, countsByGroupAndAttribute, allCategories);

    return new ChiSquaredTestData(dataTable, allCategories);
  }

  /**
   * Extracts all possible categories for Chi-squared test, excluding NA values.
   *
   * <p>Collects all unique category values across all groups for the specified attribute. NA values
   * are explicitly excluded as they represent missing data rather than a valid category.
   *
   * @param attributeId the clinical attribute ID to extract categories for
   * @param countsByGroupAndAttribute list of maps containing count items by group
   * @return set of all unique non-NA category values
   */
  private static Set<String> extractAllCategories(
      String attributeId, List<Map<String, ClinicalDataCountItem>> countsByGroupAndAttribute) {

    Set<String> allCategories = new HashSet<>();

    for (Map<String, ClinicalDataCountItem> groupData : countsByGroupAndAttribute) {
      ClinicalDataCountItem countItem = groupData.get(attributeId);
      if (countItem != null) {
        for (var item : countItem.getCounts()) {
          allCategories.add(item.getValue());
        }
      }
    }

    return allCategories;
  }

  /**
   * Builds contingency table for Chi-squared test.
   *
   * <p>Creates a 2D table where rows represent groups and columns represent categories. Each cell
   * contains the count of samples in that group with that category value. Groups with no non-zero
   * counts are excluded from the table.
   *
   * @param attributeId the clinical attribute ID to build table for
   * @param countsByGroupAndAttribute list of maps containing count items by group
   * @param allCategories set of all categories to include as columns
   * @return contingency table ready for Chi-squared test
   */
  private static DataTable2D buildContingencyTable(
      String attributeId,
      List<Map<String, ClinicalDataCountItem>> countsByGroupAndAttribute,
      Set<String> allCategories) {

    DataTable2D dataTable = new DataTable2D();
    int groupIndex = 0;

    for (Map<String, ClinicalDataCountItem> groupData : countsByGroupAndAttribute) {
      Map<String, Integer> counts = new HashMap<>();
      ClinicalDataCountItem countItem = groupData.get(attributeId);

      // Extract counts for this group
      if (countItem != null) {
        for (var item : countItem.getCounts()) {
          counts.put(item.getValue(), item.getCount());
        }
      }

      // Build normalized row for this group
      AssociativeArray categoryCounts = new AssociativeArray();
      boolean hasNonZeroCount = false;

      for (String category : allCategories) {
        Integer count = counts.getOrDefault(category, 0);
        categoryCounts.put(category, count);
        if (count > 0) {
          hasNonZeroCount = true;
        }
      }

      // Only add group if it has at least one non-zero count
      if (hasNonZeroCount) {
        dataTable.put(groupIndex++, categoryCounts);
      }
    }

    return dataTable;
  }
}
