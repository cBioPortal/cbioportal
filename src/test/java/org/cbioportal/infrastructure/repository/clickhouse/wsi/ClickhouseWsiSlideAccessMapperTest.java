package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;
import org.cbioportal.domain.wsi.WsiSlideSource;
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

/** Exercises the slide lookup SQL against the resource fixture rows. */
@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseWsiSlideAccessMapperTest {

  private static final String WSI_TEST_STUDY = "wsi_test_study";
  private static final String WSI_SNAPSHOT_STUDY = "wsi_snapshot_study";

  private static final String SAMPLE_SLIDE_KEY = "2351e12d4955";
  private static final String PATIENT_SLIDE_KEY = "b3286836fc27";
  private static final String OTHER_RESOURCE_SLIDE_KEY = "0f0e0d0c0b0a";
  private static final String SAMPLE_SEALED_SOURCE =
      "AQEBAQEBAQEBAQEBMc_tL9dUso8ZSDg1JZIFeeclGKwdmpgGohT4JR793-tUE3oED7qLjtQhP_usT08BCC6kpTQl6az5"
          + "wJ6AMvgEt1zCZgqnjk2gcXeEODZFCRVg80fRaEk2FQaFx_eROJgD6ajaxtdJ1OJw4aKN06MJD0Hc6zBDxi77WsoS";

  @Autowired private ClickhouseWsiSlideAccessMapper mapper;

  @Test
  public void readsTheCapabilityInputsOfAServableSlide() {
    Map<String, Object> row =
        mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", SAMPLE_SLIDE_KEY);

    assertNotNull(row);
    assertEquals(
        new WsiSlideSource(SAMPLE_SLIDE_KEY, SAMPLE_SEALED_SOURCE, 128, 64),
        ClickhouseWsiSlideAccessRepository.servableSource(SAMPLE_SLIDE_KEY, row));
  }

  @Test
  public void findsUnmatchedPatientSlidesThatCannotServeTiles() {
    Map<String, Object> row =
        mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", PATIENT_SLIDE_KEY);

    assertNotNull(row);
    assertNull(ClickhouseWsiSlideAccessRepository.servableSource(PATIENT_SLIDE_KEY, row));
  }

  @Test
  public void returnsNullForUnknownStudy() {
    assertNull(mapper.getSlideAccess("no_such_study", "WSI-PATIENT", SAMPLE_SLIDE_KEY));
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
    assertNull(mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", "0".repeat(12)));
  }

  @Test
  public void returnsNullForNonWsiResourceRow() {
    // The OTHER_SLIDES row is a slide row with a slide key and complete serving
    // metadata, but it belongs to a resource other than WSI_SAMPLE/WSI_PATIENT.
    assertNull(mapper.getSlideAccess(WSI_TEST_STUDY, "WSI-PATIENT", OTHER_RESOURCE_SLIDE_KEY));
  }
}
