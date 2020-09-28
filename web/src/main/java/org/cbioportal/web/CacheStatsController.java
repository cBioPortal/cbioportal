package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.service.CacheStatisticsService;
import org.cbioportal.service.exception.CacheNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Qualifier;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import java.util.ArrayList;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Validated
@Api(tags = "CacheStats", hidden = true, description = " ")
@ApiIgnore
@Profile({"ehcache-heap", "ehcache-disk", "ehcache-hybrid", "redis"})
public class CacheStatsController {

    @Autowired
    public CacheStatisticsService cacheStatisticsService;

    @RequestMapping(value = "/{cache}/keysInCache", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get list of keys in cache")
    public ResponseEntity<List<String>> getKeysInCache(
        @ApiParam(required = true, value = "Cache name")
        @PathVariable String cache) throws CacheNotFoundException {
        List<String> strings = cacheStatisticsService.getKeysInCache(cache);
        return new ResponseEntity<>(strings, HttpStatus.OK);
    }

    @RequestMapping(value = "/{cache}/keyCountsPerClass", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get counts of keys per repository class")
    public ResponseEntity<List<String>> getKeyCountsPerClass(
        @ApiParam(required = true, value = "Cache name")
        @PathVariable String cache) throws CacheNotFoundException {
        List<String> strings = cacheStatisticsService.getKeyCountsPerClass(cache);
        return new ResponseEntity<>(strings, HttpStatus.OK);
    }

    @RequestMapping(value = "/cacheStatistics", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get general cache statistics")
    public ResponseEntity<String> getCacheStatistics() throws CacheNotFoundException {
        return new ResponseEntity<>(cacheStatisticsService.getCacheStatistics(), HttpStatus.OK);
    }
}
