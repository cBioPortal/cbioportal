package org.cbioportal.web;

import com.mongodb.BasicDBObject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.web.parameter.VirtualStudy;
import org.cbioportal.web.parameter.VirtualStudyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/api/public_virtual_studies")
public class PublicVirtualStudiesController {

    private static final Logger LOG = LoggerFactory.getLogger(PublicVirtualStudiesController.class);
    
    @Value("${session.endpoint.publisher-api-key:}")
    private String requiredPublisherApiKey;
    
    public static final String ALL_USERS = "*";
    @Autowired
    SessionServiceRequestHandler sessionServiceRequestHandler;
    
    @Value("${session.service.url:}")
    private String sessionServiceURL;

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

        return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
    }

    @PostMapping
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = VirtualStudy.class)))
    public ResponseEntity<VirtualStudy> publishVirtualStudyData(
        @RequestBody VirtualStudyData virtualStudyData,
        @RequestHeader(value = "X-PUBLISHER-API-KEY") String providedPublisherApiKey
    ) {
        if (requiredPublisherApiKey == null
            || requiredPublisherApiKey.isBlank()
            || !requiredPublisherApiKey.equals(providedPublisherApiKey)) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        VirtualStudyData virtualStudyDataToPublish = makeCopyForPublishing(virtualStudyData);
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
        @RequestHeader(value = "X-PUBLISHER-API-KEY") String providedPublisherApiKey
    ) {
        if (requiredPublisherApiKey == null
            || requiredPublisherApiKey.isBlank()
            || !requiredPublisherApiKey.equals(providedPublisherApiKey)) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<VirtualStudy> responseEntity = getVirtualStudyById(id);
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            LOG.error("The downstream server replied with statusCode={} and body={}." +
                    " Replying with the same status code to the client.",
                statusCode, responseEntity.getBody());
            return new ResponseEntity<>(null, statusCode);
        }
        if (responseEntity.getBody() == null) {
            LOG.error("The downstream server replied without body and statusCode={}." +
                    " Replying with internal server error status code to the client.",
                statusCode);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return publishVirtualStudyData(responseEntity.getBody().getData(), providedPublisherApiKey);
    }

    private ResponseEntity<VirtualStudy> getVirtualStudyById(String id) {
        return new RestTemplate()
            .exchange(sessionServiceURL + "/virtual_study/" + id,
                HttpMethod.GET,
                new HttpEntity<>(sessionServiceRequestHandler.getHttpHeaders()),
                VirtualStudy.class);
    }

    //TODO Removing does not work. Is there caching?
    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity retractVirtualStudy(
        @PathVariable String id,
        @RequestHeader(value = "X-PUBLISHER-API-KEY") String providedPublisherApiKey
    ) {
        if (requiredPublisherApiKey == null
            || requiredPublisherApiKey.isBlank()
            || !requiredPublisherApiKey.equals(providedPublisherApiKey)) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<VirtualStudy> responseEntity = getVirtualStudyById(id);
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            LOG.error("The downstream server replied with statusCode={} and body={}." +
                    " Replying with the same status code to the client.",
                statusCode, responseEntity.getBody());
            return new ResponseEntity<>(null, statusCode);
        }
        if (responseEntity.getBody() == null) {
            LOG.error("The downstream server replied without body and statusCode={}." +
                    " Replying with internal server error status code to the client.",
                statusCode);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } 
        VirtualStudy virtualStudy = responseEntity.getBody();
        VirtualStudyData data = virtualStudy.getData();
        data.setUsers(Collections.emptySet());
        new RestTemplate()
            .put(sessionServiceURL + "/virtual_study/" + id,
                new HttpEntity<>(data, sessionServiceRequestHandler.getHttpHeaders()));
        return new ResponseEntity(HttpStatus.OK);
    }

    private VirtualStudyData makeCopyForPublishing(VirtualStudyData virtualStudyData) {
        VirtualStudyData virtualStudyDataToPublish = new VirtualStudyData();
        virtualStudyDataToPublish.setName(virtualStudyData.getName());
        virtualStudyDataToPublish.setDescription(virtualStudyData.getDescription());
        virtualStudyDataToPublish.setStudies(virtualStudyData.getStudies());
        virtualStudyDataToPublish.setStudyViewFilter(virtualStudyData.getStudyViewFilter());
        virtualStudyDataToPublish.setUsers(Set.of(ALL_USERS));
        return virtualStudyDataToPublish;
    }
}
