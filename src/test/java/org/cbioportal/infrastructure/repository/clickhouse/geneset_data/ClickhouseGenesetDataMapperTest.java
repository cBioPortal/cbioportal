package org.cbioportal.infrastructure.repository.clickhouse.geneset_data;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.GenesetMolecularData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@JdbcTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseGenesetDataMapperTest {

  // The test data has no sample order for the gene set profile, so one is added: the scores of
  // morf_atrx (-0.0670, -0.6270, -1.2266, ...) and hinata_nfkb_matrix (1.0106, -0.0662, -0.8585,
  // ...)
  // belong to the samples tcga-a1-a0sd-01, -a0se-01 and -a0sh-01 (internal ids 2, 3 and 6).

  private static final String PROFILE = "study_tcga_pub_gsva_scores";

  @Autowired private ClickhouseGenesetDataMapper mapper;
  @Autowired private DataSource dataSource;

  @Before
  public void addSampleOrderOfTheGenesetProfile() {
    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    Integer existing =
        jdbc.queryForObject(
            "SELECT count() FROM genetic_profile_samples WHERE genetic_profile_id = 9",
            Integer.class);
    if (existing != null && existing == 0) {
      jdbc.update(
          "INSERT INTO genetic_profile_samples (genetic_profile_id, ordered_sample_list) VALUES (9, '2,3,6,')");
    }
  }

  @Test
  public void unpacksTheScoresOfEveryGenesetPerSample() {
    List<GenesetMolecularData> result = mapper.getGenesetData(PROFILE, null, null);

    assertEquals(6, result.size());
    Map<String, String> values = byGenesetAndSample(result);
    assertEquals("-0.067", values.get("morf_atrx/tcga-a1-a0sd-01"));
    assertEquals("-0.627", values.get("morf_atrx/tcga-a1-a0se-01"));
    assertEquals("-1.2266", values.get("morf_atrx/tcga-a1-a0sh-01"));
    assertEquals("1.0106", values.get("hinata_nfkb_matrix/tcga-a1-a0sd-01"));
    assertEquals("-0.0662", values.get("hinata_nfkb_matrix/tcga-a1-a0se-01"));
    assertEquals("-0.8585", values.get("hinata_nfkb_matrix/tcga-a1-a0sh-01"));
    GenesetMolecularData first = result.get(0);
    assertEquals(PROFILE, first.getMolecularProfileId());
    assertEquals("study_tcga_pub", first.getStudyId());
    assertEquals(
        first.getSampleId().substring(0, first.getSampleId().length() - 3), first.getPatientId());
  }

  @Test
  public void genesetsAndSamplesRestrictTheResult() {
    List<GenesetMolecularData> byGeneset =
        mapper.getGenesetData(PROFILE, null, List.of("morf_atrx"));
    assertEquals(3, byGeneset.size());
    assertTrue(byGeneset.stream().allMatch(d -> d.getGenesetId().equals("morf_atrx")));

    List<GenesetMolecularData> bySample =
        mapper.getGenesetData(PROFILE, List.of("tcga-a1-a0se-01", "tcga-a1-a0sb-01"), null);
    assertEquals("only samples of the profile are returned", 2, bySample.size());
    assertTrue(bySample.stream().allMatch(d -> d.getSampleId().equals("tcga-a1-a0se-01")));
  }

  @Test
  public void profilesWithoutGenesetDataOrUnknownProfilesReturnNothing() {
    assertTrue(mapper.getGenesetData("study_tcga_pub_treatment_ic50", null, null).isEmpty());
    assertTrue(mapper.getGenesetData("no_such_profile", null, null).isEmpty());
    assertTrue(mapper.getGenesetData(PROFILE, null, List.of("no_such_geneset")).isEmpty());
  }

  private Map<String, String> byGenesetAndSample(List<GenesetMolecularData> data) {
    return data.stream()
        .collect(
            Collectors.toMap(
                d -> d.getGenesetId() + "/" + d.getSampleId(), GenesetMolecularData::getValue));
  }
}
