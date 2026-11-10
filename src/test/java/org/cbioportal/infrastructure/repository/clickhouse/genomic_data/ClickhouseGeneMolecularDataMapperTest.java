package org.cbioportal.infrastructure.repository.clickhouse.genomic_data;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.model.MolecularProfile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@JdbcTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseGeneMolecularDataMapperTest {

  // Test data for study_tcga_pub_mrna (study_tcga_pub), sample list study_tcga_pub_mrna with
  // samples
  // tcga-a1-a0sd-01, -a0se-01, -a0sh-01, -a0sj-01, -a0sk-01, -a0sm-01, -a0so-01, -a0sp-01
  // (in that order):
  //
  // AKT2 (entrez 208): -0.8097, 0.7360, -1.0225, -0.8922, 0.7247, 0.3537, 1.2702, -0.1419
  // AKT1 (entrez 207):  0.5210,-0.3140,  1.1550,  0.2890,-0.8670, 0.4230,-1.3640,  0.7120
  // BRAF (entrez 673):  0.3450,-0.2180,  NA,      0.8910,-0.5670, NA,     1.2340, -0.4560
  //                     (NA values are not present in the derived table)
  // KRAS (entrez 3845): 0.5 for all samples

  private static final String STUDY_ID = "study_tcga_pub";
  private static final String PROFILE_ID = "study_tcga_pub_mrna";
  private static final String PROFILE_TYPE = "mrna";
  private static final String SAMPLE_LIST_ID = "study_tcga_pub_mrna";

  @Autowired private ClickhouseGeneMolecularDataMapper mapper;

  @Test
  public void fetchesSingleGeneForSampleList() {
    List<GeneMolecularData> result = bySampleList(List.of(208), false);

    assertEquals(8, result.size());
    Map<String, GeneMolecularData> bySample = bySampleId(result);
    GeneMolecularData first = bySample.get("tcga-a1-a0sd-01");
    assertEquals(PROFILE_ID, first.getMolecularProfileId());
    assertEquals(STUDY_ID, first.getStudyId());
    assertEquals("tcga-a1-a0sd", first.getPatientId());
    assertEquals(Integer.valueOf(208), first.getEntrezGeneId());
    assertNull("gene is only populated for DETAILED", first.getGene());
    assertValue("-0.8097", first);
    assertValue("0.736", bySample.get("tcga-a1-a0se-01"));
    assertValue("-0.1419", bySample.get("tcga-a1-a0sp-01"));
  }

  @Test
  public void fetchesMultipleGenes() {
    List<GeneMolecularData> result = bySampleList(List.of(207, 208), false);

    assertEquals(16, result.size());
    assertEquals(Set.of(207, 208), entrezGeneIds(result));
    assertEquals(
        "every gene is returned for every sample",
        8,
        result.stream().filter(d -> d.getEntrezGeneId() == 207).count());
  }

  @Test
  public void nullGeneListReturnsAllGenesOfTheProfile() {
    List<GeneMolecularData> result = bySampleList(null, false);

    assertEquals(Set.of(207, 208, 673, 3845), entrezGeneIds(result));
    assertEquals("8 samples x 4 genes minus the 2 NA values of BRAF", 30, result.size());
  }

  @Test
  public void fetchesBySampleIds() {
    List<GeneMolecularData> result =
        mapper.getGeneMolecularData(
            STUDY_ID,
            PROFILE_ID,
            PROFILE_TYPE,
            List.of(207),
            null,
            new String[] {"study_tcga_pub_tcga-a1-a0sd-01", "study_tcga_pub_tcga-a1-a0sj-01"},
            false);

    assertEquals(2, result.size());
    Map<String, GeneMolecularData> bySample = bySampleId(result);
    assertValue("0.521", bySample.get("tcga-a1-a0sd-01"));
    assertValue("0.289", bySample.get("tcga-a1-a0sj-01"));
  }

  @Test
  public void samplesWithoutDataInTheProfileAreNotReturned() {
    // tcga-a1-a0sb-01 exists in the study but has no mrna measurement
    List<GeneMolecularData> result =
        mapper.getGeneMolecularData(
            STUDY_ID,
            PROFILE_ID,
            PROFILE_TYPE,
            List.of(207),
            null,
            new String[] {"study_tcga_pub_tcga-a1-a0sb-01", "study_tcga_pub_tcga-a1-a0sd-01"},
            false);

    assertEquals(1, result.size());
    assertEquals("tcga-a1-a0sd-01", result.get(0).getSampleId());
  }

  @Test
  public void missingValuesAreNotReturned() {
    // NA values are not stored in genetic_alteration_derived, so they are simply absent
    List<GeneMolecularData> result = bySampleList(List.of(673), false);

    assertEquals(6, result.size());
    Map<String, GeneMolecularData> bySample = bySampleId(result);
    assertFalse(bySample.containsKey("tcga-a1-a0sh-01"));
    assertFalse(bySample.containsKey("tcga-a1-a0sm-01"));
    assertValue("0.345", bySample.get("tcga-a1-a0sd-01"));
  }

  @Test
  public void detailedProjectionPopulatesGene() {
    List<GeneMolecularData> result = bySampleList(List.of(207), true);

    assertEquals(8, result.size());
    for (GeneMolecularData data : result) {
      assertNotNull(data.getGene());
      assertEquals(Integer.valueOf(207), data.getGene().getEntrezGeneId());
      assertEquals("AKT1", data.getGene().getHugoGeneSymbol());
      assertEquals("protein-coding", data.getGene().getType());
    }
  }

  @Test
  public void unknownGeneUnknownSampleListAndUnknownProfileReturnNothing() {
    assertTrue(bySampleList(List.of(999999999), false).isEmpty());
    assertTrue(
        mapper
            .getGeneMolecularData(
                STUDY_ID, PROFILE_ID, PROFILE_TYPE, List.of(207), "no_such_list", null, false)
            .isEmpty());
    assertTrue(
        mapper
            .getGeneMolecularData(
                STUDY_ID, "study_tcga_pub_nope", "nope", List.of(207), SAMPLE_LIST_ID, null, false)
            .isEmpty());
  }

  @Test
  public void dataIsRestrictedToTheStudyOfTheProfile() {
    assertTrue(
        "no data may be returned for a different study even when the sample list matches",
        mapper
            .getGeneMolecularData(
                "other_study",
                "other_study_mrna",
                PROFILE_TYPE,
                List.of(207),
                SAMPLE_LIST_ID,
                null,
                false)
            .isEmpty());
  }

  @Test
  public void multipleProfilesReturnAllSamplesOfEachProfile() {
    List<GeneMolecularData> result =
        mapper.getGeneMolecularDataInMultipleProfiles(
            List.of(profile(PROFILE_ID), profile("study_tcga_pub_gistic")),
            List.of(207),
            null,
            false);

    Map<String, List<GeneMolecularData>> byProfile =
        result.stream().collect(Collectors.groupingBy(GeneMolecularData::getMolecularProfileId));
    assertEquals(Set.of(PROFILE_ID, "study_tcga_pub_gistic"), byProfile.keySet());
    assertEquals(8, byProfile.get(PROFILE_ID).size());
    assertEquals(14, byProfile.get("study_tcga_pub_gistic").size());
    for (GeneMolecularData data : result) {
      assertEquals(STUDY_ID, data.getStudyId());
      assertEquals(Integer.valueOf(207), data.getEntrezGeneId());
      assertNull(data.getGene());
    }
    assertValue("0.521", bySampleId(byProfile.get(PROFILE_ID)).get("tcga-a1-a0sd-01"));
  }

  @Test
  public void multipleProfilesMultipleGenes() {
    List<GeneMolecularData> result =
        mapper.getGeneMolecularDataInMultipleProfiles(
            List.of(profile(PROFILE_ID)), List.of(207, 208), null, false);

    assertEquals(16, result.size());
    assertEquals(Set.of(207, 208), entrezGeneIds(result));
  }

  @Test
  public void multipleProfilesRestrictedToSamplesAreLookedUpInEveryProfileOfTheStudy() {
    List<GeneMolecularData> result =
        mapper.getGeneMolecularDataInMultipleProfiles(
            List.of(profile(PROFILE_ID), profile("study_tcga_pub_gistic")),
            List.of(207),
            new String[] {"study_tcga_pub_tcga-a1-a0sd-01", "study_tcga_pub_tcga-a1-a0sj-01"},
            false);

    Map<String, List<GeneMolecularData>> byProfile =
        result.stream().collect(Collectors.groupingBy(GeneMolecularData::getMolecularProfileId));
    assertEquals(2, byProfile.get(PROFILE_ID).size());
    assertTrue(
        byProfile.get("study_tcga_pub_gistic").stream()
            .allMatch(d -> Set.of("tcga-a1-a0sd-01", "tcga-a1-a0sj-01").contains(d.getSampleId())));
  }

  @Test
  public void multipleProfilesDetailedProjectionPopulatesGene() {
    List<GeneMolecularData> result =
        mapper.getGeneMolecularDataInMultipleProfiles(
            List.of(profile(PROFILE_ID)), List.of(208), null, true);

    assertEquals(8, result.size());
    assertEquals("AKT2", result.get(0).getGene().getHugoGeneSymbol());
    assertEquals(Integer.valueOf(208), result.get(0).getGene().getEntrezGeneId());
  }

  @Test
  public void multipleProfilesWithoutDataReturnNothing() {
    // the mutation profile and an unknown profile hold no gene-level measurements
    assertTrue(
        mapper
            .getGeneMolecularDataInMultipleProfiles(
                List.of(profile("study_tcga_pub_mutations"), profile("study_tcga_pub_nope")),
                List.of(207),
                null,
                false)
            .isEmpty());
    assertTrue(
        mapper
            .getGeneMolecularDataInMultipleProfiles(
                List.of(profile(PROFILE_ID)), List.of(999999999), null, false)
            .isEmpty());
  }

  private MolecularProfile profile(String stableId) {
    MolecularProfile profile = new MolecularProfile();
    profile.setStableId(stableId);
    profile.setCancerStudyIdentifier(STUDY_ID);
    return profile;
  }

  private List<GeneMolecularData> bySampleList(List<Integer> entrezGeneIds, boolean includeGene) {
    return mapper.getGeneMolecularData(
        STUDY_ID, PROFILE_ID, PROFILE_TYPE, entrezGeneIds, SAMPLE_LIST_ID, null, includeGene);
  }

  private Map<String, GeneMolecularData> bySampleId(List<GeneMolecularData> data) {
    return data.stream()
        .collect(Collectors.toMap(GeneMolecularData::getSampleId, d -> d, (a, b) -> a));
  }

  private Set<Integer> entrezGeneIds(List<GeneMolecularData> data) {
    return data.stream().map(GeneMolecularData::getEntrezGeneId).collect(Collectors.toSet());
  }

  private void assertValue(String expected, GeneMolecularData actual) {
    assertNotNull(actual);
    assertEquals(0, new BigDecimal(expected).compareTo(new BigDecimal(actual.getValue())));
  }
}
