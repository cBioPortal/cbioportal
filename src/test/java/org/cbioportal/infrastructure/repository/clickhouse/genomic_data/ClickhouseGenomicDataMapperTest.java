package org.cbioportal.infrastructure.repository.clickhouse.genomic_data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cbioportal.domain.studyview.StudyViewFilterFactory;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenomicDataCount;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.web.parameter.DataFilterValue;
import org.cbioportal.legacy.web.parameter.GenomicDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
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
public class ClickhouseGenomicDataMapperTest {
  private static final String STUDY_TCGA_PUB = "study_tcga_pub";
  private static final String STUDY_ACC_TCGA = "acc_tcga";
  private static final String HUGO_GENE_SYMBOL = "AKT1";

  @Autowired private ClickhouseGenomicDataMapper mapper;

  @Test
  public void getCNACounts() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    GenomicDataFilter genomicDataFilterCNA = new GenomicDataFilter("AKT1", "cna");
    List<GenomicDataCountItem> actualCountsCNA =
        mapper.getCNACounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of(genomicDataFilterCNA));
    List<GenomicDataCountItem> expectedCountsCNA =
        List.of(
            new GenomicDataCountItem(
                "AKT1",
                "cna",
                List.of(
                    new GenomicDataCount("Homozygously deleted", "-2", 2),
                    new GenomicDataCount("Heterozygously deleted", "-1", 2),
                    new GenomicDataCount("Diploid", "0", 2),
                    new GenomicDataCount("Gained", "1", 2),
                    new GenomicDataCount("Amplified", "2", 2),
                    new GenomicDataCount("NA", "NA", 5))));
    assertThat(actualCountsCNA)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedCountsCNA);

    GenomicDataFilter genomicDataFilterGISTIC = new GenomicDataFilter("AKT1", "gistic");
    List<GenomicDataCountItem> actualCountsGISTIC =
        mapper.getCNACounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of(genomicDataFilterGISTIC));
    List<GenomicDataCountItem> expectedCountsGISTIC =
        List.of(
            new GenomicDataCountItem(
                "AKT1",
                "gistic",
                List.of(
                    new GenomicDataCount("Homozygously deleted", "-2", 2),
                    new GenomicDataCount("Heterozygously deleted", "-1", 3),
                    new GenomicDataCount("Diploid", "0", 3),
                    new GenomicDataCount("Gained", "1", 3),
                    new GenomicDataCount("Amplified", "2", 3),
                    new GenomicDataCount("NA", "NA", 1))));
    assertThat(actualCountsGISTIC)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedCountsGISTIC);
  }

  @Test
  public void getMutationCounts() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    GenomicDataFilter genomicDataFilterMutation = new GenomicDataFilter("AKT1", "cna");
    Map<String, Integer> actualMutationCounts =
        mapper.getMutationCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            genomicDataFilterMutation);
    Map<String, Integer> expectedMutationCounts = new HashMap<>();
    expectedMutationCounts.put("mutatedCount", 2);
    expectedMutationCounts.put("notMutatedCount", 8);
    expectedMutationCounts.put("notProfiledCount", 5);
    assertThat(actualMutationCounts)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedMutationCounts);
  }

  @Test
  public void getMutationCountsByType() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    GenomicDataFilter genomicDataFilterMutation = new GenomicDataFilter("AKT1", "mutation");
    List<GenomicDataCountItem> actualMutationCountsByType =
        mapper.getMutationCountsByType(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of(genomicDataFilterMutation),
            false,
            HUGO_GENE_SYMBOL);
    List<GenomicDataCountItem> expectedMutationCountsByType =
        List.of(
            new GenomicDataCountItem(
                "AKT1",
                "mutations",
                List.of(
                    new GenomicDataCount("nonsense mutation", "nonsense_mutation", 2, 1),
                    new GenomicDataCount("missense mutation", "missense_mutation", 1, 1))));
    assertThat(actualMutationCountsByType)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedMutationCountsByType);
  }

  @Test
  public void getMutationCountsByTypeAddSampleId() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    ArrayList<String> studyIds = new ArrayList<>();
    studyIds.add(STUDY_TCGA_PUB);
    studyViewFilter.setStudyIds(studyIds);

    GenomicDataFilter genomicDataFilterMutation = new GenomicDataFilter("AKT1", "mutation");
    List<GenomicDataCountItem> mutationCountsByType =
        mapper.getMutationCountsByType(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of(genomicDataFilterMutation),
            true,
            HUGO_GENE_SYMBOL);

    assertThat(mutationCountsByType)
        .flatExtracting(GenomicDataCountItem::getCounts)
        .extracting(GenomicDataCount::getSampleIds)
        .allSatisfy(
            sampleIds ->
                assertThat(sampleIds)
                    .as("sampleIds should be populated when includeSampleIds=true")
                    .isNotNull()
                    .isNotEmpty());

    // In the case of multiple studies the query should return the sampleIds for both studies for
    // that particular gene
    studyViewFilter.getStudyIds().add(STUDY_ACC_TCGA);
    List<GenomicDataCountItem> mutationCountsByType2 =
        mapper.getMutationCountsByType(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of(genomicDataFilterMutation),
            true,
            HUGO_GENE_SYMBOL);
    List<String> allSampleIds =
        mutationCountsByType2.stream()
            .flatMap(item -> item.getCounts().stream())
            .flatMap(count -> count.getSampleIds().stream())
            .toList();

    assertThat(allSampleIds).anySatisfy(id -> assertThat(id).startsWith(STUDY_TCGA_PUB + "_"));

    assertThat(allSampleIds).anySatisfy(id -> assertThat(id).startsWith(STUDY_ACC_TCGA + "_"));
  }

  @Test
  public void getMutationCountsByTypeNoSampleId() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    GenomicDataFilter genomicDataFilterMutation = new GenomicDataFilter("AKT1", "mutation");
    List<GenomicDataCountItem> mutationCountsByType =
        mapper.getMutationCountsByType(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of(genomicDataFilterMutation),
            false,
            HUGO_GENE_SYMBOL);

    assertThat(mutationCountsByType)
        .flatExtracting(GenomicDataCountItem::getCounts)
        .extracting(GenomicDataCount::getSampleIds)
        .containsOnlyNulls();
  }

  @Test
  public void getProteinExpressionCounts() {
    // Testing combined study missing samples when one lacks a relevant genomic profile
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB, STUDY_ACC_TCGA));

    GenomicDataBinFilter genomicDataBinFilterRPPA = new GenomicDataBinFilter();
    genomicDataBinFilterRPPA.setHugoGeneSymbol("AKT1");
    genomicDataBinFilterRPPA.setProfileType("rppa");

    List<ClinicalDataCount> actualRPPACounts1 =
        mapper.getGenomicDataBinCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of(genomicDataBinFilterRPPA));

    ClinicalDataCount expectedRPPACount1 = new ClinicalDataCount();
    expectedRPPACount1.setAttributeId("AKT1rppa");
    expectedRPPACount1.setValue("0.7360");
    expectedRPPACount1.setCount(1);
    ClinicalDataCount expectedRPPACount2 = new ClinicalDataCount();
    expectedRPPACount2.setAttributeId("AKT1rppa");
    expectedRPPACount2.setValue("-0.8097");
    expectedRPPACount2.setCount(1);
    ClinicalDataCount expectedRPPACount3 = new ClinicalDataCount();
    expectedRPPACount3.setAttributeId("AKT1rppa");
    expectedRPPACount3.setValue("-0.1260");
    expectedRPPACount3.setCount(1);
    ClinicalDataCount expectedRPPACountNA = new ClinicalDataCount();
    expectedRPPACountNA.setAttributeId("AKT1rppa");
    expectedRPPACountNA.setValue("NA");
    expectedRPPACountNA.setCount(16);

    List<ClinicalDataCount> expectedRPPACounts1 =
        List.of(expectedRPPACount1, expectedRPPACount2, expectedRPPACount3, expectedRPPACountNA);
    assertThat(actualRPPACounts1)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedRPPACounts1);

    // Testing NA filtering on combined study missing samples when one lacks a relevant genomic
    // profile
    // Make genomic data filter to put in study view filter
    GenomicDataFilter genomicDataFilterRPPA = new GenomicDataFilter("AKT1", "rppa");
    DataFilterValue dataFilterValue = new DataFilterValue();
    dataFilterValue.setValue("NA");
    genomicDataFilterRPPA.setValues(List.of(dataFilterValue));
    studyViewFilter.setGenomicDataFilters(List.of(genomicDataFilterRPPA));

    List<ClinicalDataCount> actualRPPACounts2 =
        mapper.getGenomicDataBinCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of(genomicDataBinFilterRPPA));

    ClinicalDataCount expectedRPPACount = new ClinicalDataCount();
    expectedRPPACount.setAttributeId("AKT1rppa");
    expectedRPPACount.setValue("NA");
    expectedRPPACount.setCount(16);

    List<ClinicalDataCount> expectedRPPACounts2 = List.of(expectedRPPACount);
    assertThat(actualRPPACounts2)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedRPPACounts2);
  }

  @Test
  public void getMolecularProfileCounts() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    var profiles = new ArrayList<String>(Arrays.asList("mutations"));
    var profileGroups = new ArrayList<List<String>>(Arrays.asList(profiles));

    studyViewFilter.setGenomicProfiles(profileGroups);

    var molecularProfileCounts =
        mapper.getMolecularProfileSampleCounts(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    var size =
        molecularProfileCounts.stream()
            .filter(gc -> gc.getValue().equals("mutations"))
            .findFirst()
            .get()
            .getCount()
            .intValue();
    assertEquals(11, size);
  }

  @Test
  public void getMolecularProfileCountsMultipleStudies() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB, STUDY_ACC_TCGA));

    var profiles = new ArrayList<String>(Arrays.asList("mutations"));
    var profileGroups = new ArrayList<List<String>>(Arrays.asList(profiles));

    studyViewFilter.setGenomicProfiles(profileGroups);

    var molecularProfileCounts =
        mapper.getMolecularProfileSampleCounts(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    var size =
        molecularProfileCounts.stream()
            .filter(gc -> gc.getValue().equals("mutations"))
            .findFirst()
            .get()
            .getCount()
            .intValue();
    assertEquals(11, size);
  }

  @Test
  public void getMolecularProfileCountsMultipleProfilesUnion() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    var profiles = new ArrayList<String>(Arrays.asList("mutations", "mrna"));
    var profileGroups = new ArrayList<List<String>>(Arrays.asList(profiles));

    studyViewFilter.setGenomicProfiles(profileGroups);

    var molecularProfileCounts =
        mapper.getMolecularProfileSampleCounts(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    var sizeMutations =
        molecularProfileCounts.stream()
            .filter(gc -> gc.getValue().equals("mutations"))
            .findFirst()
            .get()
            .getCount()
            .intValue();
    assertEquals(11, sizeMutations);

    var sizeMrna =
        molecularProfileCounts.stream()
            .filter(gc -> gc.getValue().equals("mrna"))
            .findFirst()
            .get()
            .getCount()
            .intValue();
    assertEquals(9, sizeMrna);
  }

  @Test
  public void getMolecularProfileCountsMultipleProfilesIntersect() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    var profile1 = new ArrayList<String>(Arrays.asList("mutations"));
    var profile2 = new ArrayList<String>(Arrays.asList("mrna"));
    var profileGroups = new ArrayList<List<String>>(Arrays.asList(profile1, profile2));

    studyViewFilter.setGenomicProfiles(profileGroups);

    var molecularProfileCounts =
        mapper.getMolecularProfileSampleCounts(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    var sizeMutations =
        molecularProfileCounts.stream()
            .filter(gc -> gc.getValue().equals("mutations"))
            .findFirst()
            .get()
            .getCount()
            .intValue();
    assertEquals(10, sizeMutations);
  }
}
