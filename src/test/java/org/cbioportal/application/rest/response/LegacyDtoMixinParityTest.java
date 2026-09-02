package org.cbioportal.application.rest.response;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class LegacyDtoMixinParityTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void sampleListDtoDoesNotExposeStudyFieldFromLegacyIgnoredCancerStudy() {
    SampleListDTO dto = new SampleListDTO();
    dto.setSampleListId("list_1");
    dto.setStudyId("study_1");
    dto.setStudy(new CancerStudyDTO());

    JsonNode json = objectMapper.valueToTree(dto);

    assertTrue(json.has("sampleListId"));
    assertTrue(json.has("studyId"));
    assertFalse(json.has("study"));
  }

  @Test
  public void patientDtoExposesCancerStudyInDetailedProjection() {
    PatientDTO dto = new PatientDTO();
    dto.setPatientId("patient_1");
    dto.setStudyId("study_1");
    dto.setCancerStudy(new CancerStudyDTO());
    dto.setStudy(new CancerStudyDTO());

    JsonNode json = objectMapper.valueToTree(dto);

    assertTrue(json.has("patientId"));
    assertTrue(json.has("studyId"));
    assertTrue(json.has("cancerStudy"));
    assertFalse(json.has("study"));
  }
}
