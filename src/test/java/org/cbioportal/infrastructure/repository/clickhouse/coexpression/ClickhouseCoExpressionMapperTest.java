package org.cbioportal.infrastructure.repository.clickhouse.coexpression;

import static org.junit.Assert.*;

import java.util.List;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseCoExpressionMapperTest {

  // Test data in study_tcga_pub_mrna (profile 3, cancer_study_identifier = study_tcga_pub):
  // Samples: 2,3,6,8,9,10,12,13 -> sample_unique_ids: study_tcga_pub_tcga-a1-a0sd-01, etc.
  //
  // AKT2 (entrez 208):  -0.8097, 0.7360, -1.0225, -0.8922, 0.7247, 0.3537,  1.2702, -0.1419
  // AKT1 (entrez 207):   0.5210,-0.3140,  1.1550,  0.2890,-0.8670, 0.4230, -1.3640,  0.7120
  // BRAF (entrez 673):    0.3450,-0.2180,  NA,      0.8910,-0.5670, NA,      1.2340, -0.4560
  // KRAS (entrez 3845):   0.5,    0.5,     0.5,     0.5,    0.5,    0.5,     0.5,     0.5

  private static final String STUDY_ID = "study_tcga_pub";
  private static final String PROFILE_TYPE = "mrna";

  @Autowired private ClickhouseCoExpressionMapper mapper;

  @Test
  public void testSameProfileCoExpression() {
    List<CoExpressionResult> results =
        mapper.getCoExpressions(STUDY_ID, PROFILE_TYPE, STUDY_ID, PROFILE_TYPE, "AKT2", null, 0.0);

    // AKT2 as reference gene: should find AKT1 and BRAF (not KRAS — constant value filtered out)
    assertTrue("Should return at least 1 result", results.size() >= 1);

    // AKT1 should be present with all 8 samples (both genes have full data)
    CoExpressionResult akt1 =
        results.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().orElse(null);
    assertNotNull("AKT1 should be in co-expression results", akt1);
    assertEquals("AKT1 should use all 8 samples", 8, (int) akt1.getNumSamples());
    assertTrue(
        "AKT1 correlation should be finite", Double.isFinite(akt1.getSpearmansCorrelation()));

    // BRAF has NA for 2 samples — should use only the 6 overlapping samples
    CoExpressionResult braf =
        results.stream().filter(r -> r.getEntrezGeneId() == 673).findFirst().orElse(null);
    assertNotNull("BRAF should be in co-expression results", braf);
    assertEquals(
        "BRAF should use only 6 samples (2 excluded due to NA values)",
        6,
        (int) braf.getNumSamples());

    // KRAS should be present but with null correlation (constant expression value)
    CoExpressionResult kras =
        results.stream().filter(r -> r.getEntrezGeneId() == 3845).findFirst().orElse(null);
    assertNotNull("KRAS should be present with null correlation", kras);
    assertNull(
        "KRAS correlation should be null (constant expression)", kras.getSpearmansCorrelation());
  }

  @Test
  public void testMissingValueHandling() {
    // Use BRAF as reference gene — it has NA for samples 6 and 10
    // The INNER JOIN should automatically exclude those samples from all correlations
    List<CoExpressionResult> results =
        mapper.getCoExpressions(STUDY_ID, PROFILE_TYPE, STUDY_ID, PROFILE_TYPE, "BRAF", null, 0.0);

    // AKT2 has all 8 values, but BRAF reference only has 6 valid values,
    // so the join should produce only 6 paired samples
    CoExpressionResult akt2 =
        results.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().orElse(null);
    assertNotNull("AKT2 should be in co-expression results when BRAF is reference", akt2);
    assertEquals(
        "AKT2 vs BRAF should use 6 samples (BRAF has 2 NAs)", 6, (int) akt2.getNumSamples());

    // AKT1 also has all 8 values, same logic applies
    CoExpressionResult akt1 =
        results.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().orElse(null);
    assertNotNull("AKT1 should be in co-expression results when BRAF is reference", akt1);
    assertEquals(
        "AKT1 vs BRAF should use 6 samples (BRAF has 2 NAs)", 6, (int) akt1.getNumSamples());
  }

  @Test
  public void testSampleFiltering() {
    // Request co-expression for only a subset of samples
    String[] sampleSubset =
        new String[] {
          "study_tcga_pub_tcga-a1-a0sd-01", // sample 2
          "study_tcga_pub_tcga-a1-a0se-01", // sample 3
          "study_tcga_pub_tcga-a1-a0sh-01", // sample 6
          "study_tcga_pub_tcga-a1-a0sj-01" // sample 8
        };

    List<CoExpressionResult> results =
        mapper.getCoExpressions(
            STUDY_ID, PROFILE_TYPE, STUDY_ID, PROFILE_TYPE, "AKT2", sampleSubset, 0.0);

    // AKT1 should have 4 samples (all 4 requested samples have valid data for both genes)
    CoExpressionResult akt1 =
        results.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().orElse(null);
    assertNotNull("AKT1 should be present with sample subset", akt1);
    assertEquals("AKT1 should use all 4 requested samples", 4, (int) akt1.getNumSamples());

    // BRAF has NA for sample 6 (position 3), so only 3 of 4 requested samples have valid data
    CoExpressionResult braf =
        results.stream().filter(r -> r.getEntrezGeneId() == 673).findFirst().orElse(null);
    assertNotNull("BRAF should be present with sample subset", braf);
    assertEquals("BRAF should use 3 samples (sample 6 has NA)", 3, (int) braf.getNumSamples());
  }

  @Test
  public void testThresholdFiltering() {
    List<CoExpressionResult> allResults =
        mapper.getCoExpressions(STUDY_ID, PROFILE_TYPE, STUDY_ID, PROFILE_TYPE, "AKT2", null, 0.0);

    List<CoExpressionResult> filteredResults =
        mapper.getCoExpressions(STUDY_ID, PROFILE_TYPE, STUDY_ID, PROFILE_TYPE, "AKT2", null, 0.9);

    assertTrue(
        "Higher threshold should return fewer or equal results",
        filteredResults.size() <= allResults.size());

    for (CoExpressionResult result : filteredResults) {
      // Filtered results include genes with valid correlations meeting threshold,
      // plus genes with null correlation (constant/near-constant)
      if (result.getSpearmansCorrelation() != null) {
        assertTrue(
            "All correlated results should meet threshold",
            Math.abs(result.getSpearmansCorrelation()) >= 0.9);
      }
    }
  }

  @Test
  public void testConstantGeneReturnedWithNullCorrelation() {
    // KRAS has all identical values (0.5) — should be present but with null correlation
    List<CoExpressionResult> results =
        mapper.getCoExpressions(STUDY_ID, PROFILE_TYPE, STUDY_ID, PROFILE_TYPE, "AKT2", null, 0.0);

    CoExpressionResult kras =
        results.stream().filter(r -> r.getEntrezGeneId() == 3845).findFirst().orElse(null);
    assertNotNull("Constant gene should be present in results", kras);
    assertNull("Constant gene should have null correlation", kras.getSpearmansCorrelation());
  }

  @Test
  public void testMinimumSampleCountReturnsNullCorrelation() {
    // Request only 2 samples — below the minimum of 3 required for correlation
    // Genes should still be returned but with null correlation
    String[] twoSamples =
        new String[] {
          "study_tcga_pub_tcga-a1-a0sd-01", // sample 2
          "study_tcga_pub_tcga-a1-a0se-01" // sample 3
        };

    List<CoExpressionResult> results =
        mapper.getCoExpressions(
            STUDY_ID, PROFILE_TYPE, STUDY_ID, PROFILE_TYPE, "AKT2", twoSamples, 0.0);

    assertFalse("Should return results even with fewer than 3 samples", results.isEmpty());
    for (CoExpressionResult result : results) {
      assertNull(
          "All results should have null correlation when fewer than 3 samples",
          result.getSpearmansCorrelation());
    }
  }
}
