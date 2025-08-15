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
import java.util.Collections;
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
 * <p>This class replicates the original logic from ClinicalDataEnrichmentUtil using Datumbox
 * Framework for statistical computations.
 */
public final class ClinicalDataEnrichmentUtil {

  private ClinicalDataEnrichmentUtil() {}

  // Data structures for test preparation
  private record KruskalWallisTestData(
      TransposeDataCollection transposeCollection, Set<Double> distinctValues) {}

  private record ChiSquaredTestData(DataTable2D dataTable, Set<String> allCategories) {}

  /** Performs Kruskal-Wallis test on numerical clinical data across multiple groups. */
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
                  BigDecimal.valueOf(pValue),
                  method);
          enrichments.add(clinicalEnrichment);
        }
      }
    }

    return enrichments;
  }

  /** Performs Chi-squared test on categorical clinical data across multiple groups. */
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
                BigDecimal.valueOf(pValue),
                EnrichmentTestMethod.CHI_SQUARED);
        enrichments.add(clinicalEnrichment);
      }
    }

    return enrichments;
  }

  /** Builds test data for Kruskal-Wallis test. */
  private static KruskalWallisTestData buildKruskalWallisTestData(
      String attributeId, List<Map<String, List<Double>>> dataByGroupAndAttribute) {

    TransposeDataCollection transposeCollection = new TransposeDataCollection();
    Set<Double> distinctValues = new HashSet<>();
    int groupIndex = 0;

    // Single pass: extract data and build structures simultaneously
    for (Map<String, List<Double>> groupData : dataByGroupAndAttribute) {
      List<Double> values = groupData.get(attributeId);
      if (values != null && !values.isEmpty()) {
        // Cast to Collection<Object> for FlatDataCollection compatibility
        transposeCollection.put(
            groupIndex++, new FlatDataCollection(Collections.singleton(values)));
        distinctValues.addAll(values);
      }
    }

    return new KruskalWallisTestData(transposeCollection, distinctValues);
  }

  /** Builds test data for Chi-squared test. */
  private static ChiSquaredTestData buildChiSquaredTestData(
      String attributeId, List<Map<String, ClinicalDataCountItem>> countsByGroupAndAttribute) {

    Set<String> allCategories = extractAllCategories(attributeId, countsByGroupAndAttribute);
    DataTable2D dataTable =
        buildContingencyTable(attributeId, countsByGroupAndAttribute, allCategories);

    return new ChiSquaredTestData(dataTable, allCategories);
  }

  /** Extracts all possible categories for Chi-squared test. */
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

  /** Builds contingency table for Chi-squared test. */
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
