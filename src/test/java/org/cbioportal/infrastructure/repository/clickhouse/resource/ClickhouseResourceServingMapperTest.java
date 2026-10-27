package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;
import org.cbioportal.domain.resource.ResourceServing;
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

/** Exercises the serving lookup SQL against the resource fixture rows. */
@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseResourceServingMapperTest {

  private static final String STUDY = "wsi_test_study";
  private static final String SLIDE_KEY = "2351e12d4955";
  private static final String UNSERVABLE_SLIDE_KEY = "b3286836fc27";
  private static final String OTHER_RESOURCE_KEY = "0f0e0d0c0b0a";
  private static final String SEALED_SOURCE =
      "AQEBAQEBAQEBAQEBMc_tL9dUso8ZSDg1JZIFeeclGKwdmpgGohT4JR793-tUE3oED7qLjtQhP_usT08BCC6kpTQl6az5"
          + "wJ6AMvgEt1zCZgqnjk2gcXeEODZFCRVg80fRaEk2FQaFx_eROJgD6ajaxtdJ1OJw4aKN06MJD0Hc6zBDxi77WsoS";

  @Autowired private ClickhouseResourceServingMapper mapper;

  @Test
  public void findsARowByItsServingKey() {
    Map<String, Object> row = mapper.getServing(STUDY, "WSI_SAMPLE", SLIDE_KEY, "WSI-PATIENT");

    assertNotNull(row);
    assertEquals(
        new ResourceServing("WSI_SAMPLE", SLIDE_KEY, SEALED_SOURCE, 128, 64),
        ClickhouseResourceServingRepository.servingOf("WSI_SAMPLE", SLIDE_KEY, row));
  }

  @Test
  public void findsOnlyThePatientsRows() {
    assertNotNull(mapper.getServing(STUDY, "WSI_SAMPLE", SLIDE_KEY, "WSI-PATIENT"));
    assertNull(mapper.getServing(STUDY, "WSI_SAMPLE", SLIDE_KEY, "SNAPSHOT-PATIENT"));
  }

  @Test
  public void servesAnyResourceWithServingData() {
    // OTHER_SLIDES is not a WSI resource; its row carries serving data, so it can be opened under
    // its own resource id, and only there.
    assertNotNull(mapper.getServing(STUDY, "OTHER_SLIDES", OTHER_RESOURCE_KEY, "WSI-PATIENT"));
    assertNull(mapper.getServing(STUDY, "WSI_SAMPLE", OTHER_RESOURCE_KEY, "WSI-PATIENT"));
  }

  @Test
  public void findsNoServingKeyForARowWithoutServingData() {
    // The unmatched WSI_PATIENT slide cannot serve tiles, so it has no serving object.
    assertNull(mapper.getServing(STUDY, "WSI_PATIENT", UNSERVABLE_SLIDE_KEY, "WSI-PATIENT"));
  }

  @Test
  public void returnsNullForAnotherStudyOrAnUnknownKey() {
    assertNull(mapper.getServing("wsi_snapshot_study", "WSI_SAMPLE", SLIDE_KEY, "WSI-PATIENT"));
    assertNull(mapper.getServing("no_such_study", "WSI_SAMPLE", SLIDE_KEY, "WSI-PATIENT"));
    assertNull(mapper.getServing(STUDY, "WSI_SAMPLE", "0".repeat(12), "WSI-PATIENT"));
  }
}
