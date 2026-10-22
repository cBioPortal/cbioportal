package org.cbioportal.infrastructure.repository.clickhouse.fusion;

import static org.junit.Assert.*;

import java.util.List;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.StructuralVariant;
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
public class ClickhouseFusionMapperTest {

  private static final String STUDY_TCGA_PUB = "study_tcga_pub";
  private static final String STUDY_ACC_TCGA = "acc_tcga";

  @Autowired private ClickhouseFusionMapper mapper;

  @Test
  public void getFusionsRestrictsResultsToRequestedStudies() {
    List<StructuralVariant> tcgaPub = mapper.getFusions(List.of(STUDY_TCGA_PUB), null, null);
    List<StructuralVariant> accTcga = mapper.getFusions(List.of(STUDY_ACC_TCGA), null, null);
    List<StructuralVariant> both =
        mapper.getFusions(List.of(STUDY_TCGA_PUB, STUDY_ACC_TCGA), null, null);

    assertFalse(tcgaPub.isEmpty());
    assertFalse(accTcga.isEmpty());
    assertTrue(tcgaPub.stream().allMatch(sv -> STUDY_TCGA_PUB.equals(sv.getStudyId())));
    assertTrue(accTcga.stream().allMatch(sv -> STUDY_ACC_TCGA.equals(sv.getStudyId())));
    assertEquals(tcgaPub.size() + accTcga.size(), both.size());
  }

  @Test
  public void getFusionsFiltersByMolecularProfile() {
    var result = mapper.getFusions(List.of(STUDY_TCGA_PUB), List.of("study_tcga_pub_sv"), null);

    assertFalse(result.isEmpty());
    assertTrue(
        result.stream().allMatch(sv -> "study_tcga_pub_sv".equals(sv.getMolecularProfileId())));
  }

  @Test
  public void getFusionsFiltersByEitherPartnerGene() {
    var all = mapper.getFusions(List.of(STUDY_TCGA_PUB), null, null);
    int entrezGeneId = all.get(0).getSite1EntrezGeneId();

    var result = mapper.getFusions(List.of(STUDY_TCGA_PUB), null, List.of(entrezGeneId));

    assertFalse(result.isEmpty());
    assertTrue(
        result.stream()
            .allMatch(
                sv ->
                    Integer.valueOf(entrezGeneId).equals(sv.getSite1EntrezGeneId())
                        || Integer.valueOf(entrezGeneId).equals(sv.getSite2EntrezGeneId())));
  }
}
