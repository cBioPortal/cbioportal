package org.cbioportal.infrastructure.repository.clickhouse.gene_panel_data;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.GenePanelData;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
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
public class ClickhouseGenePanelDataMapperTest {

  // study_tcga_pub has 15 samples. The mutation profile study_tcga_pub_mutations has sample_profile
  // rows for samples tcga-a1-a0sb-01 (testpanel2), -a0sd-01, -a0se-01, -a0sh-01 (testpanel2),
  // -a0sn-01 (testpanel1) and -a0si-01, -a0sj-01, -a0sk-01, -a0sm-01, -a0so-01, -a0sp-01 (no
  // panel). All other samples are not profiled. acc_tcga contains a sample with the same stable id
  // tcga-a1-a0sb-01 that has no sample_profile rows.

  private static final String STUDY = "study_tcga_pub";
  private static final String MUTATIONS = "study_tcga_pub_mutations";

  @Autowired private ClickhouseGenePanelDataMapper mapper;

  @Test
  public void profileIdsReturnEverySampleOfTheStudyProfiledOrNot() {
    List<GenePanelData> result = mapper.getGenePanelDataByProfileIds(Set.of(MUTATIONS));

    assertEquals(15, result.size());
    assertTrue(result.stream().allMatch(d -> d.getMolecularProfileId().equals(MUTATIONS)));
    assertTrue(result.stream().allMatch(d -> d.getStudyId().equals(STUDY)));
    assertEquals(
        "profiled samples",
        11,
        result.stream().filter(d -> Boolean.TRUE.equals(d.getProfiled())).count());
  }

  @Test
  public void genePanelIdIsNullWhenTheSampleHasNoPanel() {
    Map<String, GenePanelData> bySample =
        bySampleId(mapper.getGenePanelDataByProfileIds(Set.of(MUTATIONS)));

    assertEquals("testpanel2", bySample.get("tcga-a1-a0sb-01").getGenePanelId());
    assertEquals("testpanel1", bySample.get("tcga-a1-a0sn-01").getGenePanelId());
    GenePanelData profiledWithoutPanel = bySample.get("tcga-a1-a0si-01");
    assertTrue(profiledWithoutPanel.getProfiled());
    assertNull(profiledWithoutPanel.getGenePanelId());
    GenePanelData notProfiled = bySample.get("tcga-a1-a0sf-01");
    assertFalse(notProfiled.getProfiled());
    assertNull(notProfiled.getGenePanelId());
  }

  @Test
  public void patientIdsAreReturned() {
    Map<String, GenePanelData> bySample =
        bySampleId(mapper.getGenePanelDataByProfileIds(Set.of(MUTATIONS)));

    assertEquals("tcga-a1-a0sd", bySample.get("tcga-a1-a0sd-01").getPatientId());
  }

  @Test
  public void severalProfilesAreCrossedWithTheSamplesOfTheirStudies() {
    List<GenePanelData> result =
        mapper.getGenePanelDataByProfileIds(
            Set.of(MUTATIONS, "study_tcga_pub_gistic", "acc_tcga_mutations"));

    Map<String, Long> perProfile =
        result.stream()
            .collect(
                Collectors.groupingBy(GenePanelData::getMolecularProfileId, Collectors.counting()));
    assertEquals(Long.valueOf(15), perProfile.get(MUTATIONS));
    assertEquals(Long.valueOf(15), perProfile.get("study_tcga_pub_gistic"));
    assertEquals(
        "acc_tcga has four samples", Long.valueOf(4), perProfile.get("acc_tcga_mutations"));
  }

  @Test
  public void unknownProfilesAreIgnored() {
    assertTrue(mapper.getGenePanelDataByProfileIds(Set.of("no_such_profile")).isEmpty());
    assertEquals(
        15, mapper.getGenePanelDataByProfileIds(Set.of(MUTATIONS, "no_such_profile")).size());
  }

  @Test
  public void pairsReturnExactlyTheRequestedSamplesOfTheRequestedProfiles() {
    List<GenePanelData> result =
        mapper.getGenePanelDataByProfileSamplePairs(
            Set.of(MUTATIONS, "study_tcga_pub_gistic"),
            List.of(
                pair(MUTATIONS, "tcga-a1-a0sb-01"),
                pair(MUTATIONS, "tcga-a1-a0sf-01"),
                pair("study_tcga_pub_gistic", "tcga-a1-a0sd-01")));

    assertEquals(3, result.size());
    Set<String> keys =
        result.stream()
            .map(d -> d.getMolecularProfileId() + "/" + d.getSampleId())
            .collect(Collectors.toSet());
    assertEquals(
        Set.of(
            MUTATIONS + "/tcga-a1-a0sb-01",
            MUTATIONS + "/tcga-a1-a0sf-01",
            "study_tcga_pub_gistic/tcga-a1-a0sd-01"),
        keys);
  }

  @Test
  public void pairsOnlyMatchSamplesOfTheProfilesOwnStudy() {
    // tcga-a1-a0sb-01 exists in study_tcga_pub and in acc_tcga
    List<GenePanelData> result =
        mapper.getGenePanelDataByProfileSamplePairs(
            Set.of("acc_tcga_mutations", MUTATIONS),
            List.of(
                pair("acc_tcga_mutations", "tcga-a1-a0sb-01"), pair(MUTATIONS, "tcga-a1-a0sb-01")));

    assertEquals(2, result.size());
    Map<String, GenePanelData> byProfile =
        result.stream().collect(Collectors.toMap(GenePanelData::getMolecularProfileId, d -> d));
    assertEquals("acc_tcga", byProfile.get("acc_tcga_mutations").getStudyId());
    assertFalse(byProfile.get("acc_tcga_mutations").getProfiled());
    assertEquals(STUDY, byProfile.get(MUTATIONS).getStudyId());
    assertTrue(byProfile.get(MUTATIONS).getProfiled());
  }

  @Test
  public void duplicatePairsAndUnknownPairsDoNotAddRows() {
    List<GenePanelData> result =
        mapper.getGenePanelDataByProfileSamplePairs(
            Set.of(MUTATIONS, "no_such_profile"),
            List.of(
                pair(MUTATIONS, "tcga-a1-a0sb-01"),
                pair(MUTATIONS, "tcga-a1-a0sb-01"),
                pair(MUTATIONS, "no_such_sample"),
                pair("no_such_profile", "tcga-a1-a0sb-01")));

    assertEquals(1, result.size());
  }

  private SampleMolecularIdentifier pair(String profileId, String sampleId) {
    SampleMolecularIdentifier identifier = new SampleMolecularIdentifier();
    identifier.setMolecularProfileId(profileId);
    identifier.setSampleId(sampleId);
    return identifier;
  }

  private Map<String, GenePanelData> bySampleId(List<GenePanelData> data) {
    return data.stream().collect(Collectors.toMap(GenePanelData::getSampleId, d -> d));
  }
}
