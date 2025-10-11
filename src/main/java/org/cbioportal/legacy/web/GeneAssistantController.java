package org.cbioportal.legacy.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.cbioportal.application.assistant.GeneAssistantService;
import org.cbioportal.legacy.model.GeneAssistantResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController()
@ConditionalOnProperty(name = "spring.ai.enabled", havingValue = "true")
public class GeneAssistantController {

  private final GeneAssistantService geneAssistantService;

  @Autowired
  public GeneAssistantController(GeneAssistantService geneAssistantService) {
    this.geneAssistantService = geneAssistantService;
  }

  @Operation(description = "Send query to AI model for gene assistance")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = GeneAssistantResponse.class)))
  @PostMapping(value = "/api/assistant", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GeneAssistantResponse> fetchGeneAssistantResponse(
      @RequestBody String message) {

    String response = geneAssistantService.generateResponse(message);
    GeneAssistantResponse geneAssistantResponse = new GeneAssistantResponse();
    geneAssistantResponse.setAiResponse(response);

    return ResponseEntity.ok(geneAssistantResponse);
  }
}
