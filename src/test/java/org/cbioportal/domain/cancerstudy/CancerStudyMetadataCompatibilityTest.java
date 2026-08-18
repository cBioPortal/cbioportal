package org.cbioportal.domain.cancerstudy;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class CancerStudyMetadataCompatibilityTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void serializesCompatibilityFieldNamesForCancerTypeAndResourceCount() {
    JsonNode cancerType =
        objectMapper.valueToTree(new TypeOfCancer("acc", "name", "blue", "short", "parent"));
    JsonNode resourceCount =
        objectMapper.valueToTree(
            new ResourceCount(
                "resource", "display", "desc", "type", "priority", true, "study_1", "meta", 1, 2));

    assertEquals("acc", cancerType.get("id").asText());
    assertEquals("acc", cancerType.get("cancerTypeId").asText());
    assertEquals("acc", cancerType.get("typeOfCancerId").asText());
    assertEquals("study_1", resourceCount.get("cancerStudyIdentifier").asText());
    assertEquals("study_1", resourceCount.get("studyId").asText());
  }
}
