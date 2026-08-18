package org.cbioportal.application.rest.response;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class StudyDtoCompatibilityTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void serializesLegacyFieldNamesOnTypeOfCancerDto() {
    TypeOfCancerDTO dto = new TypeOfCancerDTO();
    dto.setCancerTypeId("acc");

    JsonNode json = objectMapper.valueToTree(dto);

    assertEquals("acc", json.get("cancerTypeId").asText());
    assertEquals("acc", json.get("id").asText());
    assertEquals("acc", json.get("typeOfCancerId").asText());
  }

  @Test
  public void serializesLegacyFieldNamesOnResourceCountDto() {
    ResourceCountDTO dto = new ResourceCountDTO();
    dto.setStudyId("study_1");

    JsonNode json = objectMapper.valueToTree(dto);

    assertEquals("study_1", json.get("studyId").asText());
    assertEquals("study_1", json.get("cancerStudyIdentifier").asText());
  }
}
