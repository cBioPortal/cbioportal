package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
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
  private static final String SAMPLE_SEALED_SOURCE =
      "AQEBAQEBAQEBAQEBMc_tL9dUso8ZSDg1JZIFeeclGKwdmpgGohT4JR793-tUE3oED7qLjtQhP_usT08BCC6kpTQl6az5"
          + "wJ6AMvgEt1zCZgqnjk2gcXeEODZFCRVg80fRaEk2FQaFx_eROJgD6ajaxtdJ1OJw4aKN06MJD0Hc6zBDxi77WsoS";

  @Autowired private ClickhouseWsiSlideAccessMapper mapper;

  @Test
  public void readsServingMetadataForTheSlideKey() {
    Map<String, Object> row =
        mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", SAMPLE_SLIDE_KEY);

    assertNotNull(row);
    assertEquals(SAMPLE_SLIDE_KEY, row.get("slide_key"));
    // sealed_source is read from the private wsi_serving object only to be forwarded as the
    // capability's enc claim.
    assertEquals(SAMPLE_SEALED_SOURCE, row.get("sealed_source"));
    for (String removed : new String[] {"image_id", "source_url", "thumbnail_url"}) {
      assertFalse("mapper selects " + removed, row.containsKey(removed));
    }
    assertEquals(128, ((Number) row.get("thumbnail_width")).intValue());
    assertEquals(64, ((Number) row.get("thumbnail_height")).intValue());
    assertEquals("image/jpeg", row.get("thumbnail_content_type"));
  }

  @Test
  public void servableFixtureRowPassesTheServabilityChecks() {
    assertTrue(
        ClickhouseWsiSlideAccessRepositoryTest.isServableRow(
            mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", SAMPLE_SLIDE_KEY),
            new ObjectMapper()));
  }

  @Test
  public void findsUnmatchedPatientSlidesWithoutServingData() {
    Map<String, Object> row =
        mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", PATIENT_SLIDE_KEY);

    assertNotNull(row);
    assertEquals(PATIENT_SLIDE_KEY, row.get("slide_key"));
    // A slide that cannot serve tiles has no wsi_serving object, so it is not servable.
    assertEquals("", row.get("sealed_source"));
    assertFalse(ClickhouseWsiSlideAccessRepositoryTest.isServableRow(row, new ObjectMapper()));
  }

  @Test
  public void resolvesOnlyBySlideKey() {
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
