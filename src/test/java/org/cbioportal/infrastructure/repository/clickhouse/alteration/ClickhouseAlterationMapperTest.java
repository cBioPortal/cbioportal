package org.cbioportal.infrastructure.repository.clickhouse.alteration;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.cbioportal.domain.studyview.StudyViewFilterFactory;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.CNA;
import org.cbioportal.legacy.model.MutationEventType;
import org.cbioportal.legacy.persistence.helper.AlterationFilterHelper;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
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
public class ClickhouseAlterationMapperTest {
  private static final String STUDY_TCGA_PUB = "study_tcga_pub";
  private static final String STUDY_ACC_TCGA = "acc_tcga";
  private static final String STUDY_GENIE_PUB = "study_genie_pub";

  @Autowired private ClickhouseAlterationMapper mapper;

  @Test
  public void getMutatedGenes() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));
    var alterationCountByGenes =
        mapper.getMutatedGenes(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));
    assertEquals(3, alterationCountByGenes.size());

    var testBrca1AlterationCount =
        alterationCountByGenes.stream()
            .filter(a -> Objects.equals(a.getHugoGeneSymbol(), "BRCA1"))
            .findFirst();
    assert (testBrca1AlterationCount.isPresent());
    assertEquals(Integer.valueOf(5), testBrca1AlterationCount.get().getTotalCount());
  }

  @Test
  public void getMutatedGenesWithAlterationFilter() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    // Create AlterationFilter
    AlterationFilter alterationFilter = new AlterationFilter();
    Map<MutationEventType, Boolean> mutationEventTypeFilterMap = new HashMap<>();
    mutationEventTypeFilterMap.put(MutationEventType.nonsense_mutation, Boolean.TRUE);
    mutationEventTypeFilterMap.put(MutationEventType.other, Boolean.FALSE);
    alterationFilter.setMutationEventTypes(mutationEventTypeFilterMap);

    var alterationCountByGenes =
        mapper.getMutatedGenes(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            AlterationFilterHelper.build(alterationFilter));
    assertEquals(2, alterationCountByGenes.size());

    AlterationFilter onlyMutationStatusFilter = new AlterationFilter();
    onlyMutationStatusFilter.setMutationEventTypes(new HashMap<>());
    onlyMutationStatusFilter.setIncludeGermline(false);
    onlyMutationStatusFilter.setIncludeSomatic(false);
    onlyMutationStatusFilter.setIncludeUnknownStatus(true);

    var alterationCountByGenes1 =
        mapper.getMutatedGenes(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            AlterationFilterHelper.build(onlyMutationStatusFilter));
    assertEquals(1, alterationCountByGenes1.size());

    AlterationFilter mutationTypeAndStatusFilter = new AlterationFilter();
    mutationTypeAndStatusFilter.setMutationEventTypes(mutationEventTypeFilterMap);
    mutationTypeAndStatusFilter.setMutationEventTypes(new HashMap<>());
    mutationTypeAndStatusFilter.setIncludeGermline(false);
    mutationTypeAndStatusFilter.setIncludeSomatic(false);
    mutationTypeAndStatusFilter.setIncludeUnknownStatus(true);

    var alterationCountByGenes2 =
        mapper.getMutatedGenes(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            AlterationFilterHelper.build(onlyMutationStatusFilter));
    assertEquals(1, alterationCountByGenes2.size());
  }

  @Test
  public void getCnaGenes() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));
    var alterationCountByGenes =
        mapper.getCnaGenes(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));
    assertEquals(3, alterationCountByGenes.size());

    // Test cna count for akt1
    var testAKT1AlterationCount =
        alterationCountByGenes.stream()
            .filter(a -> Objects.equals(a.getHugoGeneSymbol(), "AKT1"))
            .mapToInt(c -> c.getTotalCount().intValue())
            .sum();
    assertEquals(3, testAKT1AlterationCount);
  }

  @Test
  public void getCnaGenesWithAlterationFilter() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    // Create AlterationFilter
    AlterationFilter alterationFilter = new AlterationFilter();
    Map<CNA, Boolean> cnaEventTypeFilterMap = new HashMap<>();
    cnaEventTypeFilterMap.put(CNA.HOMDEL, false);
    cnaEventTypeFilterMap.put(CNA.AMP, true);
    alterationFilter.setCopyNumberAlterationEventTypes(cnaEventTypeFilterMap);

    var alterationCountByGenes =
        mapper.getCnaGenes(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            AlterationFilterHelper.build(alterationFilter));
    assertEquals(2, alterationCountByGenes.size());

    // Test cna count for akt1 filtering for AMP
    var testAKT1AlterationCount =
        alterationCountByGenes.stream()
            .filter(a -> Objects.equals(a.getHugoGeneSymbol(), "AKT1"))
            .mapToInt(c -> c.getTotalCount().intValue())
            .sum();
    assertEquals(2, testAKT1AlterationCount);
  }

  @Test
  public void getStructuralVariantGenes() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB, STUDY_ACC_TCGA));
    var alterationCountByGenes =
        mapper.getStructuralVariantGenes(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));
    assertEquals(8, alterationCountByGenes.size());

    // Test sv count for eml4 which is in one study
    var testeml4AlterationCount =
        alterationCountByGenes.stream()
            .filter(a -> Objects.equals(a.getHugoGeneSymbol(), "eml4"))
            .mapToInt(c -> c.getTotalCount().intValue())
            .sum();
    assertEquals(1, testeml4AlterationCount);

    // Test sv count for ncoa4 which is in both studies
    var testncoa4AlterationCount =
        alterationCountByGenes.stream()
            .filter(a -> Objects.equals(a.getHugoGeneSymbol(), "ncoa4"))
            .mapToInt(c -> c.getTotalCount().intValue())
            .sum();
    assertEquals(3, testncoa4AlterationCount);
  }

  @Test
  public void getTotalProfiledCountsByGene() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    // Testing profiled counts on samples with gene panel data and WES for one study
    var totalProfiledCountsForMutationsMap =
        mapper.getTotalProfiledCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            "MUTATION_EXTENDED",
            List.of());
    var totalProfiledCountsForCnaMap =
        mapper.getTotalProfiledCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            "COPY_NUMBER_ALTERATION",
            List.of());
    var sampleProfiledCountsForMutationsWithoutPanelDataMap =
        mapper.getSampleProfileCountWithoutPanelData(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            "MUTATION_EXTENDED");
    var sampleProfiledCountsForCnaWithoutPanelDataMap =
        mapper.getSampleProfileCountWithoutPanelData(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            "COPY_NUMBER_ALTERATION");

    // Assert the count of genes with profiled cases for mutations
    assertEquals(5, totalProfiledCountsForMutationsMap.size());
    // Assert the count of genes with profiled cases for CNA
    assertEquals(5, totalProfiledCountsForCnaMap.size());
    // Assert the profiled counts for mutations without panel data (WES)
    assertEquals(6, sampleProfiledCountsForMutationsWithoutPanelDataMap);
    // Assert the profiled counts for CNA without panel data (WES)
    assertEquals(11, sampleProfiledCountsForCnaWithoutPanelDataMap);

    // Assert the profiled counts for AKT2 mutations
    // AKT2 is on testpanel2 in STUDY_TCGA_PUB
    var akt2TotalProfiledCountsForMutations =
        totalProfiledCountsForMutationsMap.stream()
            .filter(c -> c.getHugoGeneSymbol().equals("AKT2"))
            .findFirst();
    assertTrue(akt2TotalProfiledCountsForMutations.isPresent());
    assertEquals(
        4, akt2TotalProfiledCountsForMutations.get().getNumberOfProfiledCases().intValue());
    // Assert the profiled counts for BRCA1 mutations
    // BRCA1 is on testpanel1 in STUDY_TCGA_PUB
    var brca1TotalProfiledCountsForMutations =
        totalProfiledCountsForMutationsMap.stream()
            .filter(c -> c.getHugoGeneSymbol().equals("BRCA1"))
            .findFirst();
    assertTrue(brca1TotalProfiledCountsForMutations.isPresent());
    assertEquals(
        1, brca1TotalProfiledCountsForMutations.get().getNumberOfProfiledCases().intValue());
    // Assert the profiled counts for AKT1 mutations
    // AKT1 is on both testpanel1 and testpanel2 in STUDY_TCGA_PUB
    var akt1TotalProfiledCountsForMutations =
        totalProfiledCountsForMutationsMap.stream()
            .filter(c -> c.getHugoGeneSymbol().equals("AKT1"))
            .findFirst();
    assertTrue(akt1TotalProfiledCountsForMutations.isPresent());
    assertEquals(
        5, akt1TotalProfiledCountsForMutations.get().getNumberOfProfiledCases().intValue());

    // Assert the profiled counts for AKT2 CNA
    // AKT2 is on testpanel2 in STUDY_TCGA_PUB
    var akt2TotalProfiledCountsForCna =
        totalProfiledCountsForCnaMap.stream()
            .filter(c -> c.getHugoGeneSymbol().equals("AKT2"))
            .findFirst();
    assertTrue(akt2TotalProfiledCountsForCna.isPresent());
    assertEquals(6, akt2TotalProfiledCountsForCna.get().getNumberOfProfiledCases().intValue());
    // Assert the profiled counts for BRCA1 CNA
    // BRCA1 is on testpanel1 in STUDY_TCGA_PUB
    var brca1TotalProfiledCountsForCna =
        totalProfiledCountsForCnaMap.stream()
            .filter(c -> c.getHugoGeneSymbol().equals("BRCA1"))
            .findFirst();
    assertTrue(brca1TotalProfiledCountsForCna.isPresent());
    assertEquals(2, brca1TotalProfiledCountsForCna.get().getNumberOfProfiledCases().intValue());
    // Assert the profiled counts for AKT1 CNA
    // AKT1 is on both testpanel1 and testpanel2 in STUDY_TCGA_PUB
    var akt1TotalProfiledCountsForCna =
        totalProfiledCountsForCnaMap.stream()
            .filter(c -> c.getHugoGeneSymbol().equals("AKT1"))
            .findFirst();
    assertTrue(akt1TotalProfiledCountsForCna.isPresent());
    assertEquals(8, akt1TotalProfiledCountsForCna.get().getNumberOfProfiledCases().intValue());

    // Testing profiled counts on combined studies
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB, STUDY_GENIE_PUB));

    // Testing profiled counts on samples with gene panel data and WES for a combined study
    var totalProfiledCountsForMutationsMap1 =
        mapper.getTotalProfiledCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            "MUTATION_EXTENDED",
            List.of());
    var totalProfiledCountsForCnaMap1 =
        mapper.getTotalProfiledCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            "COPY_NUMBER_ALTERATION",
            List.of());
    var sampleProfiledCountsForMutationsWithoutPanelDataMap1 =
        mapper.getSampleProfileCountWithoutPanelData(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            "MUTATION_EXTENDED");
    var sampleProfiledCountsForCnaWithoutPanelDataMap1 =
        mapper.getSampleProfileCountWithoutPanelData(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            "COPY_NUMBER_ALTERATION");

    // Assert the count of genes with profiled cases for mutations in a combined study
    assertEquals(8, totalProfiledCountsForMutationsMap1.size());
    // Assert the count of genes with profiled cases for CNA in a combined study
    assertEquals(8, totalProfiledCountsForCnaMap1.size());
    // Assert the profiled counts for mutations without panel data (WES) in a combined study
    assertEquals(8, sampleProfiledCountsForMutationsWithoutPanelDataMap1);
    // Assert the profiled counts for CNA without panel data (WES) in a combined study
    assertEquals(12, sampleProfiledCountsForCnaWithoutPanelDataMap1);

    // Assert the profiled counts for BRCA1 mutations
    // BRCA1 is on testpanel1 in STUDY_TCGA_PUB
    var brca1TotalProfiledCountsForMutations1 =
        totalProfiledCountsForMutationsMap1.stream()
            .filter(c -> c.getHugoGeneSymbol().equals("BRCA1"))
            .findFirst();
    assertTrue(brca1TotalProfiledCountsForMutations1.isPresent());
    assertEquals(
        1, brca1TotalProfiledCountsForMutations1.get().getNumberOfProfiledCases().intValue());
    // Assert the profiled counts for BRCA2 mutations
    // BRCA2 is on testpanel3 and testpanel4 in STUDY_GENIE_PUB
    var brca2TotalProfiledCountsForMutations1 =
        totalProfiledCountsForMutationsMap1.stream()
            .filter(c -> c.getHugoGeneSymbol().equals("BRCA2"))
            .findFirst();
    assertTrue(brca2TotalProfiledCountsForMutations1.isPresent());
    assertEquals(
        2, brca2TotalProfiledCountsForMutations1.get().getNumberOfProfiledCases().intValue());
    // Assert the profiled counts for AKT2 mutations
    // AKT2 is on testpanel2 in STUDY_TCGA_PUB and testpanel4 in STUDY_GENIE_PUB
    var akt2TotalProfiledCountsForMutations1 =
        totalProfiledCountsForMutationsMap1.stream()
            .filter(c -> c.getHugoGeneSymbol().equals("AKT2"))
            .findFirst();
    assertTrue(akt2TotalProfiledCountsForMutations1.isPresent());
    assertEquals(
        4, akt2TotalProfiledCountsForMutations1.get().getNumberOfProfiledCases().intValue());

    // Assert the profiled counts for BRCA1 CNA
    // BRCA1 is on testpanel1 in STUDY_TCGA_PUB
    var brca1TotalProfiledCountsForCna1 =
        totalProfiledCountsForCnaMap1.stream()
            .filter(c -> c.getHugoGeneSymbol().equals("BRCA1"))
            .findFirst();
    assertTrue(brca1TotalProfiledCountsForCna1.isPresent());
    assertEquals(2, brca1TotalProfiledCountsForCna1.get().getNumberOfProfiledCases().intValue());
    // Assert the profiled counts for BRCA2 CNA
    // BRCA2 is on testpanel3 and testpanel4 in STUDY_GENIE_PUB
    var brca2TotalProfiledCountsForCna1 =
        totalProfiledCountsForCnaMap1.stream()
            .filter(c -> c.getHugoGeneSymbol().equals("BRCA2"))
            .findFirst();
    assertTrue(brca2TotalProfiledCountsForCna1.isPresent());
    assertEquals(3, brca2TotalProfiledCountsForCna1.get().getNumberOfProfiledCases().intValue());
    // Assert the profiled counts for AKT2 CNA
    // AKT2 is on testpanel2 in STUDY_TCGA_PUB and testpanel4 in STUDY_GENIE_PUB
    var akt2TotalProfiledCountsForCna1 =
        totalProfiledCountsForCnaMap1.stream()
            .filter(c -> c.getHugoGeneSymbol().equals("AKT2"))
            .findFirst();
    assertTrue(akt2TotalProfiledCountsForCna1.isPresent());
    assertEquals(7, akt2TotalProfiledCountsForCna1.get().getNumberOfProfiledCases().intValue());
  }

  @Test
  public void testMutatedGenesOnPanelCount() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));
    var alterationCountByGenes =
        mapper.getMutatedGenes(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));

    // Test BRCA1: 4 total samples, 3 on-panel (samples 7,12,13 use WES), 1 off-panel (sample 6 uses
    // testpanel2)
    var brca1 =
        alterationCountByGenes.stream()
            .filter(a -> Objects.equals(a.getHugoGeneSymbol(), "BRCA1"))
            .findFirst()
            .orElse(null);

    assertNotNull("BRCA1 should be present", brca1);
    assertEquals("BRCA1 total altered cases", Integer.valueOf(4), brca1.getNumberOfAlteredCases());
    assertEquals(
        "BRCA1 on-panel altered cases", Integer.valueOf(3), brca1.getNumberOfAlteredCasesOnPanel());
    // totalCount = 5: sample 6 has 2 mutation events, samples 7,12,13 each have 1 (including sample
    // 7's 'na' status which passes mutation_status != 'UNCALLED' filter)
    assertEquals("BRCA1 total count", Integer.valueOf(5), brca1.getTotalCount());
  }

  @Test
  public void testCnaGenesOnPanelCount() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));
    var alterationCountByGenes =
        mapper.getCnaGenes(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));

    // Test AKT2: 1 total sample, 0 on-panel (sample 1 uses testpanel1 which doesn't contain AKT2)
    var akt2 =
        alterationCountByGenes.stream()
            .filter(a -> Objects.equals(a.getHugoGeneSymbol(), "AKT2"))
            .findFirst()
            .orElse(null);

    assertNotNull("AKT2 should be present", akt2);
    assertEquals("AKT2 total altered cases", Integer.valueOf(1), akt2.getNumberOfAlteredCases());
    assertEquals(
        "AKT2 on-panel altered cases", Integer.valueOf(0), akt2.getNumberOfAlteredCasesOnPanel());
    assertEquals("AKT2 total count", Integer.valueOf(1), akt2.getTotalCount());
  }

  @Test
  public void testStructuralVariantGenesOnPanelCount() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB, STUDY_ACC_TCGA));
    var alterationCountByGenes =
        mapper.getStructuralVariantGenes(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));

    // Test ncoa4: 2 total altered cases, 2 on-panel (both samples 1 and 15 use WES which contains
    // all genes)
    var ncoa4 =
        alterationCountByGenes.stream()
            .filter(a -> Objects.equals(a.getHugoGeneSymbol(), "ncoa4"))
            .findFirst()
            .orElse(null);

    assertNotNull("ncoa4 should be present", ncoa4);
    assertEquals("ncoa4 total altered cases", Integer.valueOf(2), ncoa4.getNumberOfAlteredCases());
    assertEquals(
        "ncoa4 on-panel altered cases", Integer.valueOf(2), ncoa4.getNumberOfAlteredCasesOnPanel());
    assertEquals("ncoa4 total count", Integer.valueOf(3), ncoa4.getTotalCount());
  }
}
