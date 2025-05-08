package org.cbioportal.legacy.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.service.NamespaceAttributeService;
import org.cbioportal.legacy.web.config.PublicApiTags;
import org.cbioportal.legacy.web.config.annotation.PublicApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PublicApi
@RestController()
@RequestMapping("/api")
@Tag(name = PublicApiTags.NAMESPACE_ATTRIBUTES, description = " ")
public class NamespaceAttributeController {

  private final NamespaceAttributeService namespaceAttributeService;

  @Autowired
  public NamespaceAttributeController(NamespaceAttributeService namespaceAttributeService) {
    this.namespaceAttributeService = namespaceAttributeService;
  }

  @PostMapping(path = "/namespace-attributes/fetch")
  @Operation(description = "Fetch namespace attributes")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array = @ArraySchema(schema = @Schema(implementation = NamespaceAttribute.class))))
  public ResponseEntity<List<NamespaceAttribute>> fetchNamespace(
      @Parameter(required = true, description = "List of Study IDs") @RequestBody
          List<String> studyIds) {

    return new ResponseEntity<>(
        namespaceAttributeService.fetchNamespaceAttributes(studyIds), HttpStatus.OK);
  }
}
