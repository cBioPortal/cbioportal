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

  private static final String SAMPLE_SLIDE_KEY = "2351e12d49557627b24fe71e17ec5c64";
  private static final String PATIENT_SLIDE_KEY = "b3286836fc27c777260ada0dcb8a6857";
  private static final String OTHER_RESOURCE_SLIDE_KEY = "0f0e0d0c0b0a09080706050403020100";

  @Autowired private ClickhouseWsiSlideAccessMapper mapper;

  @Test
  public void readsServingMetadataForTheSlideKey() {
    Map<String, Object> row =
        mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", SAMPLE_SLIDE_KEY);

    assertNotNull(row);
    assertEquals(SAMPLE_SLIDE_KEY, row.get("slide_key"));
    // image_id is read from the private wsi_serving object, server-side only, to mint the
    // encrypted capability.
    assertEquals("syn-img-0001", row.get("image_id"));
    assertEquals("s3://bucket/syn-img-0001.svs", row.get("source_url"));
    assertEquals("s3://bucket/syn-img-0001.jpg", row.get("thumbnail_url"));
    assertEquals(128, ((Number) row.get("thumbnail_width")).intValue());
    assertEquals(64, ((Number) row.get("thumbnail_height")).intValue());
    assertEquals("image/jpeg", row.get("thumbnail_content_type"));
  }

  @Test
  public void findsUnmatchedPatientSlides() {
    Map<String, Object> row =
        mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", PATIENT_SLIDE_KEY);

    assertNotNull(row);
    assertEquals("syn-img-0003", row.get("image_id"));
  }

  @Test
  public void doesNotResolveAnImageId() {
    assertNull(mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", "syn-img-0001"));
    assertNull(mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", "syn-legacy-0002"));
  }

  @Test
  public void returnsNullForWrongPatient() {
    assertNull(mapper.getSlideAccess(WSI_TEST_STUDY, "SNAPSHOT-PATIENT", SAMPLE_SLIDE_KEY));
  }

  @Test
  public void returnsNullForWrongStudy() {
    assertNull(mapper.getSlideAccess(WSI_SNAPSHOT_STUDY, "WSI-PATIENT", SAMPLE_SLIDE_KEY));
  }

  @Test
  public void returnsNullForUnknownSlideKey() {
    assertNull(mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", "0".repeat(32)));
  }

  @Test
  public void returnsNullForNonWsiResourceRow() {
    // The OTHER_SLIDES row is a WHOLE_SLIDE_IMAGE row with a slide key and complete serving
    // metadata, but it belongs to a resource other than WSI_SAMPLE/WSI_PATIENT.
    assertNull(mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", OTHER_RESOURCE_SLIDE_KEY));
  }
}
