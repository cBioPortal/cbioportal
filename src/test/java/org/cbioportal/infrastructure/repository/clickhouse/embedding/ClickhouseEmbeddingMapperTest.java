package org.cbioportal.infrastructure.repository.clickhouse.embedding;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.cbioportal.domain.embedding.EmbeddingRow;
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
public class ClickhouseEmbeddingMapperTest {

  private static final String STUDY_TCGA_PUB = "study_tcga_pub";
  private static final String STUDY_ACC_TCGA = "acc_tcga";

  @Autowired private ClickhouseEmbeddingMapper mapper;

  @Test
  public void getEmbeddingDataInStudy() {
    var studyIds = List.of(STUDY_TCGA_PUB, STUDY_ACC_TCGA);
    List<EmbeddingRow> embeddingWithDataList =
        mapper.getEmbeddingDataInStudy("pca", "sample", studyIds);
    assertEquals(4, embeddingWithDataList.size());
  }
}
