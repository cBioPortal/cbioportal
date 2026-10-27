package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cbioportal.domain.resource.ResourceIdentifierFixtures.patients;

import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.infrastructure.repository.clickhouse.resource.ClickhouseResourceDataMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The slide viewer lists a patient's slides through the generic resource table API. Its rows must
 * carry the slide fields and the nested wsi_serving object as JSON objects.
 */
@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseWsiResourceRowsTest {

  private static final String STUDY = "wsi_test_study";

  @Autowired private ClickhouseResourceDataMapper mapper;

  private List<ResourceTableRow> rows(String resourceId) {
    return mapper.getResourceTableRows(
        new ResourceTableQuery(
            List.of(STUDY),
            resourceId,
            patients(STUDY, "WSI-PATIENT"),
            List.of(),
            null,
            0,
            10_000,
            null,
            null,
            null));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void returnsThePatientsSampleSlideWithItsServingData() {
    List<ResourceTableRow> rows = rows("WSI_SAMPLE");

    assertThat(rows).hasSize(1);
    ResourceTableRow row = rows.get(0);
    assertThat(row.sampleId()).isEqualTo("WSI-SAMPLE");
    assertThat(row.metadata()).containsEntry("slide_key", "2351e12d4955");
    Map<String, Object> serving = (Map<String, Object>) row.metadata().get("wsi_serving");
    assertThat(serving).containsKey("sealed_source");
    assertThat(serving.get("tile_metadata_json")).isInstanceOf(Map.class);
  }

  @Test
  public void returnsThePatientsUnmatchedSlideWithoutASample() {
    List<ResourceTableRow> rows = rows("WSI_PATIENT");

    assertThat(rows).hasSize(1);
    assertThat(rows.get(0).sampleId()).isNull();
    assertThat(rows.get(0).metadata()).containsEntry("slide_key", "b3286836fc27");
  }
}
