package org.cbioportal.infrastructure.repository.clickhouse.mutation;

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
public class ClickhouseMutationMapperTest {

  @Autowired private ClickhouseMutationMapper clickhouseMutationMapper;

  @Test
  public void getMutationsInMultipleMolecularProfilesId() {
    String molecularProfileIds = "study_tcga_pub_mutations";
    var sampleIds = List.of("tcga-a1-a0sh-01");
    var entrezGeneIds = List.of(672);

    ProfileSamplePair profileSamplePair = new ProfileSamplePair(molecularProfileIds, sampleIds);

    List<ProfileSamplePair> profileSamplePairs = List.of(profileSamplePair);

    var result =
        clickhouseMutationMapper.getMutationsInMultipleMolecularProfilesId(
            profileSamplePairs, entrezGeneIds, false, "ID", null, null, null, null);

    assertEquals(2, result.size());
    result.forEach(
        mutation -> {
          assertEquals("study_tcga_pub_mutations", mutation.getMolecularProfileId());
          assertEquals("tcga-a1-a0sh-01", mutation.getSampleId());
          assertEquals((Integer) 672, mutation.getEntrezGeneId());
        });
  }

  @Test
  public void getSummaryMutationsInMultipleMolecularProfiles() {
    var molecularProfileIds = List.of("study_tcga_pub_mutations");
    var sampleIds = List.of("tcga-a1-a0sh-01");
    var entrezGeneIds = List.of(672);
  }

  @Test
  public void getDetailedMutationsInMultipleMolecularProfiles() {
    var molecularProfileIds = List.of("study_tcga_pub_mutations");
    var sampleIds = List.of("tcga-a1-a0sh-01");
    var entrezGeneIds = List.of(672);
  }

  @Test
  public void getMetaMutationsInMultipleMolecularProfiles() {
    String molecularProfileIds = "study_tcga_pub_mutations";
    var sampleIds = List.of("tcga-a1-a0sh-01");
    var entrezGeneIds = List.of(672);

    ProfileSamplePair ps = new ProfileSamplePair(molecularProfileIds, sampleIds);

    List<ProfileSamplePair> profileSamplePairs = List.of(ps);

    var result =
        clickhouseMutationMapper.getMetaMutationsInMultipleMolecularProfiles(
            profileSamplePairs, entrezGeneIds, false);

    assertEquals((Integer) 2, result.getTotalCount());
    assertEquals((Integer) 1, result.getSampleCount());
  }
}
