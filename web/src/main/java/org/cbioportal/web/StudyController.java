package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.CancerStudyTags;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.sort.StudySortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.security.access.prepost.PreAuthorize;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PublicApi
@RestController
@Validated
@Api(tags = "B. Studies", description = " ")
public class StudyController {
    @Value("${authenticate:false}")
    private String authenticate;

    @Value("${app.name:unknown}")
    private String appName;
    
    private boolean usingAuth() {
        return !authenticate.isEmpty()
            && !authenticate.equals("false")
            && !authenticate.contains("social_auth");
    }
    
    // This is a stop-gap solution because this endpoint needs caching
    // Right now this method has spontaneous performance problems
    // for the default query. We felt the best stop gap would be
    // to just manually cache that one response.
    private static List<CancerStudy> defaultResponse;
    
    @PostConstruct
    private void warmDefaultResponseCache() {
        if (!usingAuth()) {
            defaultResponse = studyService.getAllStudies(
                null, Projection.SUMMARY.name(),
                10000000, 0,
                null, Direction.ASC.name());
        }
    }

    @Autowired
    private StudyService studyService;

    @RequestMapping(value = "/studies", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all studies")
    public ResponseEntity<List<CancerStudy>> getAllStudies(
        @ApiParam("Search keyword that applies to name and cancer type of the studies")
        @RequestParam(required = false) String keyword,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @ApiParam("Page size of the result list")
        @Max(PagingConstants.MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
        @ApiParam("Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @ApiParam("Name of the property that the result list is sorted by")
        @RequestParam(required = false) StudySortBy sortBy,
        @ApiParam("Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) {
        
        // Only use this feature on the public portal and make sure it is never used
        // on portals using auth, as in auth setting, different users will have different
        // results.
        if (!usingAuth()
                && appName.equals("public-portal")
                && keyword == null
                && projection == Projection.SUMMARY
                && pageSize == 10000000
                && pageNumber == 0
                && sortBy == null
                && direction == Direction.ASC) {
            return new ResponseEntity<>(defaultResponse, HttpStatus.OK);
        }

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, studyService.getMetaStudies(keyword).getTotalCount()
                .toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                studyService.getAllStudies(keyword, projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
    @RequestMapping(value = "/studies/{studyId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get a study")
    public ResponseEntity<CancerStudy> getStudy(
        @ApiParam(required = true, value = "Study ID e.g. acc_tcga")
        @PathVariable String studyId) throws StudyNotFoundException {

        return new ResponseEntity<>(studyService.getStudy(studyId), HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#studyIds, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/studies/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch studies by IDs")
    public ResponseEntity<List<CancerStudy>> fetchStudies(
        @ApiParam(required = true, value = "List of Study IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody List<String> studyIds,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, studyService.fetchMetaStudies(studyIds).getTotalCount()
                .toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                studyService.fetchStudies(studyIds, projection.name()), HttpStatus.OK);
        }

    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
    @RequestMapping(value = "/studies/{studyId}/tags", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get the tags of a study")
    public ResponseEntity<Object> getTags(
        @ApiParam(required = true, value = "Study ID e.g. acc_tcga")
        @PathVariable String studyId) throws JsonParseException, JsonMappingException,
        IOException {

        Map<String,Object> map = new HashMap<String,Object>();
        ObjectMapper mapper = new ObjectMapper();
        CancerStudyTags cancerStudyTags = studyService.getTags(studyId);
        if (cancerStudyTags != null) { //If tags is null an empty map is returned
            map = mapper.readValue(cancerStudyTags.getTags(), Map.class);
        }

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#studyIds, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/studies/tags/fetch", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get the study tags by IDs")
    public ResponseEntity<List<CancerStudyTags>> getTagsForMultipleStudies(
        @ApiParam(required = true, value = "List of Study IDs")
        @RequestBody List<String> studyIds) {

        List<CancerStudyTags> cancerStudyTags = studyService.getTagsForMultipleStudies(studyIds);

        return new ResponseEntity<>(cancerStudyTags, HttpStatus.OK);
    }
}
