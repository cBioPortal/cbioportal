package org.cbioportal.legacy.web;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.service.exception.AccessForbiddenException;
import org.cbioportal.legacy.service.exception.DuplicateVirtualStudyException;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/public_virtual_studies")
public class PublicVirtualStudiesController {

  private static final Logger LOG = LoggerFactory.getLogger(PublicVirtualStudiesController.class);

  private final String requiredPublisherApiKey;

  private final VirtualStudyService virtualStudyService;

  public PublicVirtualStudiesController(
      @Value("${session.endpoint.publisher-api-key:}") String requiredPublisherApiKey,
      VirtualStudyService virtualStudyService) {
    this.requiredPublisherApiKey = requiredPublisherApiKey;
    this.virtualStudyService = virtualStudyService;
  }

  @GetMapping
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = VirtualStudy.class)))
  public ResponseEntity<List<VirtualStudy>> getPublicVirtualStudies() {
    List<VirtualStudy> virtualStudies = virtualStudyService.getPublishedVirtualStudies();
    return new ResponseEntity<>(virtualStudies, HttpStatus.OK);
  }

  @PostMapping("/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = VirtualStudy.class)))
  public ResponseEntity<Void> publishVirtualStudy(
      @PathVariable String id,
      @RequestHeader(value = "X-PUBLISHER-API-KEY") String providedPublisherApiKey,
      @RequestParam(required = false) String typeOfCancerId,
      @RequestParam(required = false) String pmid,
      @RequestBody(required = false) VirtualStudyData virtualStudyData) {
    ensureProvidedPublisherApiKeyCorrect(providedPublisherApiKey);
    virtualStudyService.publishVirtualStudy(id, typeOfCancerId, pmid, virtualStudyData);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  @ApiResponse(responseCode = "200", description = "OK")
  public ResponseEntity<Void> unPublishVirtualStudy(
      @PathVariable String id,
      @RequestParam(defaultValue = "true") boolean softDelete,
      @RequestHeader(value = "X-PUBLISHER-API-KEY") String providedPublisherApiKey) {
    ensureProvidedPublisherApiKeyCorrect(providedPublisherApiKey);
    if (softDelete) {
      virtualStudyService.unPublishVirtualStudy(id);
    } else {
      virtualStudyService.dropPublicVirtualStudy(id);
    }
    return ResponseEntity.ok().build();
  }

  private void ensureProvidedPublisherApiKeyCorrect(String providedPublisherApiKey) {
    if (requiredPublisherApiKey.isBlank()
        || !requiredPublisherApiKey.equals(providedPublisherApiKey)) {
      throw new AccessForbiddenException("The provided publisher API key is not correct.");
    }
  }

  @ExceptionHandler(DuplicateVirtualStudyException.class)
  public ResponseEntity<String> handleDuplicateVirtualStudyException(
      DuplicateVirtualStudyException e) {
    LOG.error("Duplicate virtual study error: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
  }
}
