package org.cbioportal.domain.cancerstudy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class CancerStudyMetadataCompatibilityTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void typeOfCancerSerializesOnlyId() {
    JsonNode cancerType =
        objectMapper.valueToTree(new TypeOfCancer("acc", "name", "blue", "short", "parent"));

    assertThat(cancerType.get("id").asText()).isEqualTo("acc");
    assertFalse("cancerTypeId alias must not be present", cancerType.has("cancerTypeId"));
    assertFalse("typeOfCancerId alias must not be present", cancerType.has("typeOfCancerId"));
  }

  @Test
  public void resourceCountSerializesOnlyCancerStudyIdentifier() {
    JsonNode resourceCount =
        objectMapper.valueToTree(
            new ResourceCount(
                "resource", "display", "desc", "type", "priority", true, "study_1", "meta", 1, 2));

    assertThat(resourceCount.get("cancerStudyIdentifier").asText()).isEqualTo("study_1");
    assertFalse("studyId alias must not be present", resourceCount.has("studyId"));
  }
}
