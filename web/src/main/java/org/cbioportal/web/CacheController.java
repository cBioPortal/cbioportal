package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@Validated
@ApiIgnore
@Api(tags = "Cache", description = "")
public class CacheController {

    @Autowired
    private CacheService cacheService;
    
    @Value("${cache.endpoint.api-key:not set}")
    private String requiredApiKey;
    
    @Value("${cache.endpoint.enabled:true}")
    private boolean cacheEndpointEnabled;

    @RequestMapping(value = "/cache", method = RequestMethod.DELETE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation("Flush and reinitialize caches")
    public ResponseEntity<String> clearAllCaches(@RequestHeader("X-API-KEY") String providedApiKey) {
        if (!cacheEndpointEnabled)
            return new ResponseEntity<>("Cache endpoint is disabled for this instance.", HttpStatus.NOT_FOUND);
        if ("not set".equals(requiredApiKey) || ! requiredApiKey.equals(providedApiKey))
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        cacheService.evictAllCaches();
        return new ResponseEntity<>("Flushed all caches!!!", HttpStatus.OK);
    }
}
