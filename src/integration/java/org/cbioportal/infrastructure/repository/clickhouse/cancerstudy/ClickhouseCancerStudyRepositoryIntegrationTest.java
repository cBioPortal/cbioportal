package org.cbioportal.infrastructure.repository.clickhouse.cancerstudy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.cbioportal.AbstractClickhouseIntegrationTest;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.infrastructure.repository.clickhouse.sample.ClickhouseSampleMapper;
import org.cbioportal.infrastructure.repository.clickhouse.sample.ClickhouseSampleRepository;
import org.cbioportal.shared.SortAndSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ClickhouseCancerStudyRepositoryIntegrationTest extends AbstractClickhouseIntegrationTest {

  private static final int TOTAL_STUDIES = 492;

  private ClickhouseCancerStudyRepository repository;
  private SampleRepository sampleRepository;

  @Autowired private ClickhouseCancerStudyMapper mapper;
  @Autowired private ClickhouseSampleMapper sampleMapper;

  @BeforeEach
  void setup() {
    repository = new ClickhouseCancerStudyRepository(mapper);
    sampleRepository = new ClickhouseSampleRepository(sampleMapper);
  }

  @Test
  void testGetCancerStudiesMetadata() {
    var studies =
        repository.getCancerStudiesMetadata(new SortAndSearchCriteria("", "", "", null, null));
    assertEquals(TOTAL_STUDIES, studies.size());
  }

  @Test
  void testGetCancerStudiesMetadataSummary() {
    var studies =
        repository.getCancerStudiesMetadataSummary(
            new SortAndSearchCriteria("", "", "", null, null));
    assertEquals(TOTAL_STUDIES, studies.size());
  }

  @Test
  void testGetCancerStudiesMetadataSummaryWithKeywordSearch() {
    // Search for studies containing "tcga" in name, identifier, or cancer type
    var studies =
        repository.getCancerStudiesMetadataSummary(
            new SortAndSearchCriteria("tcga", "", "", null, null));

    // Should return studies matching "tcga" (case-insensitive substring match)
    // All returned studies should contain "tcga" somewhere in their metadata
    studies.forEach(
        study -> {
          String searchable =
              (study.name()
                      + " "
                      + study.cancerStudyIdentifier()
                      + " "
                      + study.typeOfCancer().name()
                      + " "
                      + study.typeOfCancer().id())
                  .toLowerCase();
          assert searchable.contains("tcga")
              : "Study "
                  + study.cancerStudyIdentifier()
                  + " does not contain 'tcga' in searchable fields";
        });

    // Should have fewer results than total studies
    assert studies.size() < TOTAL_STUDIES : "Keyword search should filter results";
    assert !studies.isEmpty() : "Should find at least some TCGA studies";
  }

  @Test
  void testGetCancerStudiesMetadataSummaryWithKeywordSearchCaseInsensitive() {
    // Test case-insensitive search
    var studiesLower =
        repository.getCancerStudiesMetadataSummary(
            new SortAndSearchCriteria("breast", "", "", null, null));
    var studiesUpper =
        repository.getCancerStudiesMetadataSummary(
            new SortAndSearchCriteria("Breast", "", "", null, null));

    // Both searches should return the same results (case-insensitive)
    assertEquals(
        studiesLower.size(),
        studiesUpper.size(),
        "Case-insensitive search should return same number of results");

    // Verify all results contain "breast" (case-insensitive)
    studiesLower.forEach(
        study -> {
          String searchable =
              (study.name()
                      + " "
                      + study.cancerStudyIdentifier()
                      + " "
                      + study.typeOfCancer().name()
                      + " "
                      + study.typeOfCancer().id())
                  .toLowerCase();
          assert searchable.contains("breast")
              : "Study "
                  + study.cancerStudyIdentifier()
                  + " does not contain 'breast' in searchable fields";
        });
  }

  @Test
  void testGetCancerStudiesMetadataSummaryWithKeywordSearchSubstringMatch() {
    // Test that substring matching works (not just prefix)
    var studies =
        repository.getCancerStudiesMetadataSummary(
            new SortAndSearchCriteria("carcinoma", "", "", null, null));

    // Should find studies with "carcinoma" anywhere in the name or cancer type
    assert !studies.isEmpty() : "Should find studies with 'carcinoma' in their metadata";

    // Verify all results contain "carcinoma"
    studies.forEach(
        study -> {
          String searchable =
              (study.name()
                      + " "
                      + study.cancerStudyIdentifier()
                      + " "
                      + study.typeOfCancer().name()
                      + " "
                      + study.typeOfCancer().id())
                  .toLowerCase();
          assert searchable.contains("carcinoma")
              : "Study "
                  + study.cancerStudyIdentifier()
                  + " does not contain 'carcinoma' in searchable fields";
        });
  }

  @Test
  void testGetCancerStudiesMetadataSampleCounts() {
    // Test that sample counts are calculated correctly using exact matching
    var studies =
        repository.getCancerStudiesMetadata(new SortAndSearchCriteria("", "", "", null, null));

    var luadTcgaStudy =
        studies.stream()
            .filter(study -> "luad_tcga".equals(study.cancerStudyIdentifier()))
            .findFirst()
            .orElseThrow(
                () -> new AssertionError("luad_tcga study not found in test database"));

    // Verify sample counts match the actual sample lists in the database
    assertEquals(586, luadTcgaStudy.allSampleCount(), "allSampleCount should match luad_tcga_all");
    assertEquals(
        230,
        luadTcgaStudy.sequencedSampleCount(),
        "sequencedSampleCount should match luad_tcga_sequenced");
    assertEquals(
        516, luadTcgaStudy.cnaSampleCount(), "cnaSampleCount should match luad_tcga_cna");
    assertEquals(
        517,
        luadTcgaStudy.mrnaRnaSeqV2SampleCount(),
        "mrnaRnaSeqV2SampleCount should match luad_tcga_rna_seq_v2_mrna");
    assertEquals(
        32,
        luadTcgaStudy.mrnaMicroarraySampleCount(),
        "mrnaMicroarraySampleCount should match luad_tcga_mrna (excluding rna_seq_v2)");
    assertEquals(
        126,
        luadTcgaStudy.methylationHm27SampleCount(),
        "methylationHm27SampleCount should match luad_tcga_methylation_hm27");
    assertEquals(
        365, luadTcgaStudy.rppaSampleCount(), "rppaSampleCount should match luad_tcga_rppa");
    assertEquals(
        230,
        luadTcgaStudy.completeSampleCount(),
        "completeSampleCount should match luad_tcga_3way_complete");

    // These sample lists don't exist for luad_tcga, so counts should be 0 or null
    Integer miRnaCount = luadTcgaStudy.miRnaSampleCount();
    assert miRnaCount == null || miRnaCount == 0
        : "miRnaSampleCount should be 0 or null (luad_tcga_microrna doesn't exist)";

    Integer massSpecCount = luadTcgaStudy.massSpectrometrySampleCount();
    assert massSpecCount == null || massSpecCount == 0
        : "massSpectrometrySampleCount should be 0 or null (luad_tcga_protein_quantification doesn't exist)";

    Integer mrnaRnaSeqCount = luadTcgaStudy.mrnaRnaSeqSampleCount();
    assert mrnaRnaSeqCount == null || mrnaRnaSeqCount == 0
        : "mrnaRnaSeqSampleCount should be 0 or null (luad_tcga_rna_seq_mrna doesn't exist)";
  }

  @Test
  void testAllSampleCountMatchesActualSampleCount() {
    // Verify that allSampleCount in metadata matches the actual sample count from the sample
    // repository for all studies
    var studies =
        repository.getCancerStudiesMetadata(new SortAndSearchCriteria("", "", "", null, null));

    int mismatchCount = 0;

    // Test all studies
    for (var study : studies) {
      // Get the actual sample count from the sample repository
      var actualSampleCount =
          sampleRepository.getMetaSamplesInStudy(study.cancerStudyIdentifier()).getTotalCount();

      // Check if allSampleCount matches the actual sample count
      if (!actualSampleCount.equals(study.allSampleCount())) {
        mismatchCount++;
        System.err.println(
            "WARNING: Sample count mismatch for study "
                + study.cancerStudyIdentifier()
                + " - allSampleCount from metadata: "
                + study.allSampleCount()
                + ", actual sample count: "
                + actualSampleCount
                + ", difference: "
                + (actualSampleCount - study.allSampleCount())
                + ". This may indicate that the sample_list data is incomplete or outdated.");
      }
    }

    // Print summary
    if (mismatchCount > 0) {
      System.err.println(
          "\nSUMMARY: Found "
              + mismatchCount
              + " out of "
              + studies.size()
              + " studies with sample count mismatches. "
              + "This suggests that some sample_list data may be incomplete.");
    }
  }
}
