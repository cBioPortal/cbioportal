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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
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
        List<VirtualStudy> virtualStudies = sessionServiceRequestHandler.getVirtualStudiesForUser(ALL_USERS);
        return new ResponseEntity<>(virtualStudies, HttpStatus.OK);
    }

    @PostMapping
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = VirtualStudy.class)))
    public ResponseEntity<VirtualStudy> publishVirtualStudyData(
        @RequestBody VirtualStudyData virtualStudyData,
        @RequestHeader(value = "X-PUBLISHER-API-KEY") String providedPublisherApiKey,
        @RequestParam(required = false) String typeOfCancerId,
        @RequestParam(required = false) String pmid
    ) {
        ensureProvidedPublisherApiKeyCorrect(providedPublisherApiKey);
        VirtualStudyData virtualStudyDataToPublish = makeCopyForPublishing(virtualStudyData);
        updateStudyMetadataFieldsIfSpecified(virtualStudyDataToPublish, typeOfCancerId, pmid);
        VirtualStudy virtualStudy = sessionServiceRequestHandler.createVirtualStudy(virtualStudyDataToPublish);

        return new ResponseEntity<>(virtualStudy, HttpStatus.OK);
    }

    @PostMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = VirtualStudy.class)))
    public ResponseEntity<VirtualStudy> publishVirtualStudy(
        @PathVariable String id,
        @RequestHeader(value = "X-PUBLISHER-API-KEY") String providedPublisherApiKey,
        @RequestParam(required = false) String typeOfCancerId,
        @RequestParam(required = false) String pmid
    ) {
        ensureProvidedPublisherApiKeyCorrect(providedPublisherApiKey);
        VirtualStudy virtualStudy = sessionServiceRequestHandler.getVirtualStudyById(id);
        return publishVirtualStudyData(virtualStudy.getData(), providedPublisherApiKey, typeOfCancerId, pmid);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<Void> retractVirtualStudy(
        @PathVariable String id,
        @RequestHeader(value = "X-PUBLISHER-API-KEY") String providedPublisherApiKey
    ) {
        ensureProvidedPublisherApiKeyCorrect(providedPublisherApiKey);
        sessionServiceRequestHandler.softRemoveVirtualStudy(id);
        return ResponseEntity.ok().build();
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

    private VirtualStudyData makeCopyForPublishing(VirtualStudyData virtualStudyData) {
        VirtualStudyData virtualStudyDataToPublish = new VirtualStudyData();
        virtualStudyDataToPublish.setName(virtualStudyData.getName());
        virtualStudyDataToPublish.setDescription(virtualStudyData.getDescription());
        virtualStudyDataToPublish.setStudies(virtualStudyData.getStudies());
        virtualStudyDataToPublish.setStudyViewFilter(virtualStudyData.getStudyViewFilter());
        virtualStudyDataToPublish.setTypeOfCancerId(virtualStudyData.getTypeOfCancerId());
        virtualStudyDataToPublish.setPmid(virtualStudyData.getPmid());
        virtualStudyDataToPublish.setUsers(Set.of(ALL_USERS));
        return virtualStudyDataToPublish;
    }
}
