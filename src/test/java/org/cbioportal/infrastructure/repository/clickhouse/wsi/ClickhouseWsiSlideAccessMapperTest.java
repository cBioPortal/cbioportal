package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;
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

/** Exercises the access lookup SQL against the resource_data fixture rows. */
@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseWsiSlideAccessMapperTest {

  private static final long WSI_TEST_STUDY = 9001L;
  private static final long WSI_SNAPSHOT_STUDY = 9002L;

  @Autowired private ClickhouseWsiSlideAccessMapper mapper;

  @Test
  public void readsServingMetadataForTheExactRow() {
    Map<String, Object> row =
        mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", "WSI_SAMPLE", 900101L);

    assertNotNull(row);
    assertEquals("3020726", row.get("image_id"));
    assertEquals("s3://bucket/3020726.svs", row.get("source_url"));
    assertEquals("s3://bucket/3020726.jpg", row.get("thumbnail_url"));
    assertEquals(128, ((Number) row.get("thumbnail_width")).intValue());
    assertEquals(64, ((Number) row.get("thumbnail_height")).intValue());
    assertEquals("image/jpeg", row.get("thumbnail_content_type"));
  }

  @Test
  public void returnsNullForWrongPatient() {
    assertNull(mapper.getSlideAccess(WSI_TEST_STUDY, "SNAPSHOT-PATIENT", "WSI_SAMPLE", 900101L));
  }

  @Test
  public void returnsNullForWrongStudy() {
    assertNull(mapper.getSlideAccess(WSI_SNAPSHOT_STUDY, "WSI-PATIENT", "WSI_SAMPLE", 900101L));
  }

  @Test
  public void returnsNullForWrongRow() {
    assertNull(mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", "WSI_SAMPLE", 900102L));
    assertNull(mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", "WSI_SAMPLE", 999999L));
  }

  @Test
  public void returnsNullForWrongResource() {
    assertNull(mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", "WSI_PATIENT", 900101L));
  }

  @Test
  public void returnsNullForNonWsiResourceRow() {
    // 900103 is a WHOLE_SLIDE_IMAGE row with complete serving metadata, but it belongs to a
    // resource other than WSI_SAMPLE/WSI_PATIENT.
    assertNull(mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", "OTHER_SLIDES", 900103L));
  }
}
