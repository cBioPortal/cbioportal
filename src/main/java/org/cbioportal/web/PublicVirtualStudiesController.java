package org.cbioportal.web;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.cbioportal.service.CancerTypeService;
import org.cbioportal.service.exception.AccessForbiddenException;
import org.cbioportal.service.exception.CancerTypeNotFoundException;
import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.web.parameter.VirtualStudy;
import org.cbioportal.web.parameter.VirtualStudyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.NoSuchElementException;
import java.util.Set;

@Controller
@RequestMapping("/api/public_virtual_studies")
public class PublicVirtualStudiesController {

    private static final Logger LOG = LoggerFactory.getLogger(PublicVirtualStudiesController.class);

    public static final String ALL_USERS = "*";

    private final String requiredPublisherApiKey;

    private final SessionServiceRequestHandler sessionServiceRequestHandler;

    private final CancerTypeService cancerTypeService;

    public PublicVirtualStudiesController(
        @Value("${session.endpoint.publisher-api-key:}") String requiredPublisherApiKey,
        SessionServiceRequestHandler sessionServiceRequestHandler,
        CancerTypeService cancerTypeService
    ) {
        this.requiredPublisherApiKey = requiredPublisherApiKey;
        this.sessionServiceRequestHandler = sessionServiceRequestHandler;
        this.cancerTypeService = cancerTypeService;
    }

    @GetMapping
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = VirtualStudy.class)))
    public ResponseEntity<List<VirtualStudy>> getPublicVirtualStudies() {
        List<VirtualStudy> virtualStudies = sessionServiceRequestHandler.getVirtualStudiesAccessibleToUser(ALL_USERS);
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
        publishVirtualStudy(id, typeOfCancerId, pmid);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<Void> unPublishVirtualStudy(
        @PathVariable String id,
        @RequestHeader(value = "X-PUBLISHER-API-KEY") String providedPublisherApiKey
    ) {
        ensureProvidedPublisherApiKeyCorrect(providedPublisherApiKey);
        unPublishVirtualStudy(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Publishes virtual study optionally updating metadata fields
     * @param id - id of public virtual study to publish
     * @param typeOfCancerId - if specified (not null) update type of cancer of published virtual study
     * @param pmid  - if specified (not null) update PubMed ID of published virtual study
     */
    private void publishVirtualStudy(String id, String typeOfCancerId, String pmid) {
        VirtualStudy virtualStudyDataToPublish = sessionServiceRequestHandler.getVirtualStudyById(id);
        VirtualStudyData virtualStudyData = virtualStudyDataToPublish.getData();
        updateStudyMetadataFieldsIfSpecified(virtualStudyData, typeOfCancerId, pmid);
        virtualStudyData.setUsers(Set.of(ALL_USERS));
        sessionServiceRequestHandler.updateVirtualStudy(virtualStudyDataToPublish);
    }

    /**
     * Un-publish virtual study
     * @param id - id of public virtual study to un-publish
     */
    private void unPublishVirtualStudy(String id) {
        VirtualStudy virtualStudyToUnPublish = sessionServiceRequestHandler.getVirtualStudyById(id);
        if (virtualStudyToUnPublish == null) {
            throw new NoSuchElementException("The virtual study with id=" + id + " has not been found in the public list.");
        }
        VirtualStudyData virtualStudyData = virtualStudyToUnPublish.getData();
        Set<String> users = virtualStudyData.getUsers();
        if (users == null || users.isEmpty() || !users.contains(ALL_USERS)) {
            throw new NoSuchElementException("The virtual study with id=" + id + " has not been found in the public list.");
        }
        virtualStudyData.setUsers(Set.of(virtualStudyData.getOwner()));
        sessionServiceRequestHandler.updateVirtualStudy(virtualStudyToUnPublish);
    }

    private void ensureProvidedPublisherApiKeyCorrect(String providedPublisherApiKey) {
        if (requiredPublisherApiKey.isBlank()
            || !requiredPublisherApiKey.equals(providedPublisherApiKey)) {
            throw new AccessForbiddenException("The provided publisher API key is not correct.");
        }
    }

    private void updateStudyMetadataFieldsIfSpecified(VirtualStudyData virtualStudyData, String typeOfCancerId, String pmid) {
        if (typeOfCancerId != null) {
            try {
                cancerTypeService.getCancerType(typeOfCancerId);
                virtualStudyData.setTypeOfCancerId(typeOfCancerId);
            } catch (CancerTypeNotFoundException e) {
                LOG.error("No cancer type with id={} were found.", typeOfCancerId);
                throw new IllegalArgumentException( "The cancer type is not valid: " + typeOfCancerId);
            }
        }
        if (pmid != null) {
            virtualStudyData.setPmid(pmid);
        }
    }

}
