package org.cbioportal.infrastructure.repository.clickhouse.cancerstudy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.cbioportal.AbstractClickhouseIntegrationTest;
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

  @Autowired private ClickhouseCancerStudyMapper mapper;

  @BeforeEach
  void setup() {
    repository = new ClickhouseCancerStudyRepository(mapper);
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
}
