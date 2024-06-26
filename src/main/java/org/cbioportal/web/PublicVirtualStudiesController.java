package org.cbioportal.web;

import com.mongodb.BasicDBObject;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/api/public_virtual_studies")
public class PublicVirtualStudiesController {

    private static final Logger LOG = LoggerFactory.getLogger(PublicVirtualStudiesController.class);

    public static final String ALL_USERS = "*";

    private final String requiredPublisherApiKey;

    private final SessionServiceRequestHandler sessionServiceRequestHandler;

    private final String sessionServiceURL;

    private final CancerTypeService cancerTypeService;

    public PublicVirtualStudiesController(
        @Value("${session.endpoint.publisher-api-key:}") String requiredPublisherApiKey,
        SessionServiceRequestHandler sessionServiceRequestHandler,
        @Value("${session.service.url:}") String sessionServiceURL,
        CancerTypeService cancerTypeService
    ) {
        this.requiredPublisherApiKey = requiredPublisherApiKey;
        this.sessionServiceRequestHandler = sessionServiceRequestHandler;
        this.sessionServiceURL = sessionServiceURL;
        this.cancerTypeService = cancerTypeService;
    }

    @GetMapping
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = VirtualStudy.class)))
    public ResponseEntity<List<VirtualStudy>> getPublicVirtualStudies() {
        //TODO move this logic to sessionServiceRequestHandler?
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("data.users", ALL_USERS);
        ResponseEntity<List<VirtualStudy>> responseEntity = new RestTemplate().exchange(
            sessionServiceURL + "/virtual_study/query/fetch",
            HttpMethod.POST,
            new HttpEntity<>(basicDBObject.toString(), sessionServiceRequestHandler.getHttpHeaders()),
            new ParameterizedTypeReference<>() {
            });

        List<VirtualStudy> virtualStudies = responseEntity.getBody();
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
        if (requiredPublisherApiKey.isBlank()
            || !requiredPublisherApiKey.equals(providedPublisherApiKey)) {
            throw new AccessForbiddenException("The provided publisher API key is not correct.");
        }
        VirtualStudyData virtualStudyDataToPublish = makeCopyForPublishing(virtualStudyData);
        if (typeOfCancerId != null) {
            try {
                cancerTypeService.getCancerType(typeOfCancerId);
                virtualStudyDataToPublish.setTypeOfCancerId(typeOfCancerId);
            } catch (CancerTypeNotFoundException e) {
                LOG.error("No cancer type with id={} were found.", typeOfCancerId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The cancer type is not valid.", e);
            }
        }
        if (pmid != null) {
            virtualStudyDataToPublish.setPmid(pmid);
        }
        //TODO move this logic to sessionServiceRequestHandler?
        ResponseEntity<VirtualStudy> responseEntity = new RestTemplate().exchange(
            sessionServiceURL + "/virtual_study",
            HttpMethod.POST,
            new HttpEntity<>(virtualStudyDataToPublish, sessionServiceRequestHandler.getHttpHeaders()),
            new ParameterizedTypeReference<>() {
            });

        return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
    }

    @PostMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = VirtualStudy.class)))
    public ResponseEntity<VirtualStudy> publishVirtualStudy(
        @PathVariable String id,
        @RequestHeader(value = "X-PUBLISHER-API-KEY") String providedPublisherApiKey,
        @RequestParam(required = false) String typeOfCancerId,
        @RequestParam(required = false) String pmid
    ) {
        if (requiredPublisherApiKey.isBlank()
            || !requiredPublisherApiKey.equals(providedPublisherApiKey)) {
            throw new AccessForbiddenException("The provided publisher API key is not correct.");
        }
        ResponseEntity<VirtualStudy> responseEntity = getVirtualStudyById(id);
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        VirtualStudy virtualStudy = responseEntity.getBody();
        if (!statusCode.is2xxSuccessful() || virtualStudy == null) {
            LOG.error("The downstream server replied with statusCode={} and body={}." +
                    " Replying with the same status code to the client.",
                statusCode, virtualStudy);
            throw new IllegalStateException("The downstream server response is not successful");
        }
        return publishVirtualStudyData(virtualStudy.getData(), providedPublisherApiKey, typeOfCancerId, pmid);
    }

    private ResponseEntity<VirtualStudy> getVirtualStudyById(String id) {
        return new RestTemplate()
            .exchange(sessionServiceURL + "/virtual_study/" + id,
                HttpMethod.GET,
                new HttpEntity<>(sessionServiceRequestHandler.getHttpHeaders()),
                VirtualStudy.class);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "OK")
    public void retractVirtualStudy(
        @PathVariable String id,
        @RequestHeader(value = "X-PUBLISHER-API-KEY") String providedPublisherApiKey
    ) {
        if (requiredPublisherApiKey.isBlank()
            || !requiredPublisherApiKey.equals(providedPublisherApiKey)) {
            throw new AccessForbiddenException("The provided publisher API key is not correct.");
        }
        ResponseEntity<VirtualStudy> responseEntity = getVirtualStudyById(id);
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        VirtualStudy virtualStudy = responseEntity.getBody();
        if (!statusCode.is2xxSuccessful() || virtualStudy == null) {
            LOG.error("The downstream server replied with statusCode={} and body={}." +
                    " Replying with the same status code to the client.",
                statusCode, virtualStudy);
            throw new IllegalStateException("The downstream server response is not successful");
        }
        VirtualStudyData data = virtualStudy.getData();
        data.setUsers(Collections.emptySet());
        new RestTemplate()
            .put(sessionServiceURL + "/virtual_study/" + id,
                new HttpEntity<>(data, sessionServiceRequestHandler.getHttpHeaders()));
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
