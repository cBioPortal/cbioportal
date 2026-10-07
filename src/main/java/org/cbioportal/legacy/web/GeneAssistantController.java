package org.cbioportal.legacy.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import org.cbioportal.legacy.model.GeneAssistantResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Validated
@RestController
@RequestMapping("/api")
@ConditionalOnProperty(name = "assistant.enabled", havingValue = "true")
public class GeneAssistantController {

  @Value("${assistant.url}")
  private String assistantUrl;

  @Autowired private RestTemplate restTemplate;

  @Operation(description = "Send query to assistant microservice")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = GeneAssistantResponse.class)))
  @PostMapping(
      value = "/assistant",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GeneAssistantResponse> fetchGeneAssistantResponse(
      @RequestBody Map<String, String> body) {

    String message = body.get("message");

    String url = assistantUrl;

    Map<String, String> request = new HashMap<>();
    request.put("message", message);

    String response;
    try {
      response = restTemplate.postForObject(url, request, String.class);
    } catch (Exception ex) {
      GeneAssistantResponse error = new GeneAssistantResponse();
      error.setAiResponse("Assistant service error: " + ex.getMessage());
      return ResponseEntity.status(500).body(error);
    }

    GeneAssistantResponse geneAssistantResponse = new GeneAssistantResponse();
    geneAssistantResponse.setAiResponse(response);

    return ResponseEntity.ok(geneAssistantResponse);
  }
}
