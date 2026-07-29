package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
public class ClickhouseWsiHierarchyMapperTest {

  @Autowired private ClickhouseWsiHierarchyMapper mapper;

  @Test
  public void readsActivePatientHierarchy() {
    String hierarchy = mapper.getPatientHierarchy("msk_spectrum_tme_2022", "P-0055908");

    assertTrue(hierarchy.contains("\"patient_id\":\"P-0055908\""));
    assertTrue(hierarchy.contains("\"match_level\":\"PART\""));
    assertTrue(hierarchy.contains("\"match_level\":\"BLOCK\""));
    assertTrue(hierarchy.contains("\"match_level\":\"UNMATCHED\""));
    assertTrue(hierarchy.contains("\"image_id\":\"3020726\""));
    assertTrue(hierarchy.contains("\"image_id\":\"3020691\""));
    assertTrue(hierarchy.contains("\"image_id\":\"3020648\""));
  }

  @Test
  public void returnsNullForUnknownPatient() {
    assertEquals(null, mapper.getPatientHierarchy("msk_spectrum_tme_2022", "missing"));
  }

  @Test
  public void readsOnlyActiveManifestVersion() {
    String hierarchy = mapper.getPatientHierarchy("wsi_versioned_study", "VERSIONED-PATIENT");

    assertTrue(hierarchy.contains("\"sample_id\":\"active-sample\""));
    assertTrue(hierarchy.contains("\"image_id\":\"active-slide\""));
    assertTrue(!hierarchy.contains("\"sample_id\":\"old-sample\""));
  }

  @Test
  public void readsEmptyHierarchyPayload() {
    String hierarchy = mapper.getPatientHierarchy("wsi_empty_hierarchy_study", "EMPTY-PATIENT");

    assertEquals(
        "{\"patient_id\":\"EMPTY-PATIENT\",\"samples\":[],\"slide_associations\":[]}", hierarchy);
  }

  @Test
  public void returnsNullWhenManifestIsMissing() {
    assertEquals(
        null, mapper.getPatientHierarchy("wsi_missing_manifest_study", "MISSING-MANIFEST"));
  }
}
