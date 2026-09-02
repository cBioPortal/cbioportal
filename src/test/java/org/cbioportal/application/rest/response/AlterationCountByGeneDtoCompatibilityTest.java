package org.cbioportal.application.rest.response;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class AlterationCountByGeneDtoCompatibilityTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void serializesLegacyComputedFieldsOnAlterationCountByGeneDto() {
    AlterationCountByGeneDTO dto =
        new AlterationCountByGeneDTO(1, 2, 3, 4, null, 672, "BRCA1", null);

    JsonNode json = objectMapper.valueToTree(dto);

    assertEquals("BRCA1", json.get("uniqueEventKey").asText());
    assertEquals("BRCA1", json.get("hugoGeneSymbols").get(0).asText());
    assertEquals(672, json.get("entrezGeneIds").get(0).asInt());
  }

  @Test
  public void serializesLegacyComputedFieldsOnCopyNumberCountByGeneDto() {
    CopyNumberCountByGeneDTO dto =
        new CopyNumberCountByGeneDTO(1, 2, 3, 4, null, 672, "BRCA1", null, -2, "17q");

    JsonNode json = objectMapper.valueToTree(dto);

    assertEquals("672-2", json.get("uniqueEventKey").asText());
    assertEquals("BRCA1", json.get("hugoGeneSymbols").get(0).asText());
    assertEquals(672, json.get("entrezGeneIds").get(0).asInt());
  }
}
