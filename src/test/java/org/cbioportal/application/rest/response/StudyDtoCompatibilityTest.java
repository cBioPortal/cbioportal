package org.cbioportal.application.rest.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class StudyDtoCompatibilityTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void typeOfCancerDtoSerializesOnlyCancerTypeId() {
    TypeOfCancerDTO dto = new TypeOfCancerDTO();
    dto.setCancerTypeId("acc");

    JsonNode json = objectMapper.valueToTree(dto);

    assertThat(json.get("cancerTypeId").asText()).isEqualTo("acc");
    assertFalse("id alias must not be present", json.has("id"));
    assertFalse("typeOfCancerId alias must not be present", json.has("typeOfCancerId"));
  }

  @Test
  public void resourceCountDtoSerializesOnlyCancerStudyIdentifier() {
    ResourceCountDTO dto = new ResourceCountDTO();
    dto.setStudyId("study_1");

    JsonNode json = objectMapper.valueToTree(dto);

    assertThat(json.get("cancerStudyIdentifier").asText()).isEqualTo("study_1");
    assertFalse("studyId must not be serialized", json.has("studyId"));
  }
}
