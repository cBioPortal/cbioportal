package org.cbioportal.web;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.cbioportal.service.exception.AccessForbiddenException;
import org.cbioportal.service.impl.vs.PublishedVirtualStudyService;
import org.cbioportal.web.parameter.VirtualStudy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
//TODO published or shared? what is the best name?
//TODO change enpoint to /api/published_virtual_studies
@RequestMapping("/api/public_virtual_studies")
public class PublishedVirtualStudiesController {

    private final String requiredPublisherApiKey;
    private final PublishedVirtualStudyService publishedVirtualStudyService;
    private final Boolean vsMode;

    public PublishedVirtualStudiesController(
        @Value("${session.endpoint.publisher-api-key:}") String requiredPublisherApiKey,
        PublishedVirtualStudyService publishedVirtualStudyService,
        @Value("${vs_mode:false}") Boolean vsMode
    ) {
        this.requiredPublisherApiKey = requiredPublisherApiKey;
        this.publishedVirtualStudyService = publishedVirtualStudyService;
        this.vsMode = vsMode;
    }

    @GetMapping
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = VirtualStudy.class)))
    public ResponseEntity<List<VirtualStudy>> getPublishedVirtualStudies() {
        //TODO find out a better way to disable published virtual studies implementation on the frontend
        List<VirtualStudy> virtualStudies = vsMode ? List.of() : publishedVirtualStudyService.getAllPublishedVirtualStudies();
        return new ResponseEntity<>(virtualStudies, HttpStatus.OK);
    }

    @PostMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = VirtualStudy.class)))
    public ResponseEntity<Void> publishVirtualStudy(
        @PathVariable String id,
        @RequestHeader(value = "X-PUBLISHER-API-KEY") String providedPublisherApiKey,
        @RequestParam(required = false) String typeOfCancerId,
        @RequestParam(required = false) String pmid
    ) {
        ensureProvidedPublisherApiKeyCorrect(providedPublisherApiKey);
        publishedVirtualStudyService.publishVirtualStudy(id, typeOfCancerId, pmid);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<Void> unPublishVirtualStudy(
        @PathVariable String id,
        @RequestHeader(value = "X-PUBLISHER-API-KEY") String providedPublisherApiKey
    ) {
        ensureProvidedPublisherApiKeyCorrect(providedPublisherApiKey);
        publishedVirtualStudyService.unPublishVirtualStudy(id);
        return ResponseEntity.ok().build();
    }

    private void ensureProvidedPublisherApiKeyCorrect(String providedPublisherApiKey) {
        if (requiredPublisherApiKey.isBlank()
            || !requiredPublisherApiKey.equals(providedPublisherApiKey)) {
            throw new AccessForbiddenException("The provided publisher API key is not correct.");
        }
    }
}
