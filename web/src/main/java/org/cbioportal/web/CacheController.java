package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.service.CacheService;
import org.cbioportal.service.exception.CacheOperationException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
@InternalApi
@Api(tags = "Cache")
public class CacheController {

    @Autowired
    private CacheService cacheService;
    
    @Value("${cache.endpoint.api-key:not set}")
    private String requiredApiKey;
    
    @Value("${cache.endpoint.enabled:false}")
    private boolean cacheEndpointEnabled;

    @RequestMapping(value = "/cache", method = RequestMethod.DELETE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation("Clear and reinitialize caches")
    public ResponseEntity<String> clearAllCaches(
        @ApiParam("Secret API key passed in HTTP header. The key is configured in portal.properties of the portal instance.")
        @RequestHeader(value = "X-API-KEY") String providedApiKey,
        @ApiParam("Clear Spring-managed caches")
        @RequestParam(defaultValue = "true", required = false) final boolean springManagedCache)
        throws CacheOperationException {
        if (!cacheEndpointEnabled) {
            return new ResponseEntity<>("Cache endpoint is disabled for this instance.", HttpStatus.NOT_FOUND);
        }
        if ("not set".equals(requiredApiKey) || ! requiredApiKey.equals(providedApiKey)) {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
        cacheService.clearCaches(springManagedCache);
        return new ResponseEntity<>("Flushed all caches!!!", HttpStatus.OK);
    }

    @RequestMapping(value = "/cache/{studyId}", method = RequestMethod.DELETE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation("Clear and reinitialize caches after import/removal/update of a study")
    public ResponseEntity<String> clearCachesForStudy(
        @ApiParam("Secret API key passed in HTTP header. The key is configured in portal.properties of the portal instance.")
        @RequestHeader(value = "X-API-KEY") String providedApiKey,
        @PathVariable String studyId,
        @ApiParam("Clear Spring-managed caches")
        @RequestParam(defaultValue = "true", required = false) final boolean springManagedCache)
        throws CacheOperationException {
        if (!cacheEndpointEnabled) {
            return new ResponseEntity<>("Cache endpoint is disabled for this instance.", HttpStatus.NOT_FOUND);
        }
        if ("not set".equals(requiredApiKey) || ! requiredApiKey.equals(providedApiKey)) {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
        
        cacheService.clearCachesForStudy(studyId, springManagedCache);
        return new ResponseEntity<>("Flushed "+ studyId +" caches!!!", HttpStatus.OK);
    }
}
