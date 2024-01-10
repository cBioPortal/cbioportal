package org.cbioportal.web;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.CancerStudyTags;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.utils.security.AccessLevel;
import org.cbioportal.utils.security.PortalSecurityConfig;
import org.cbioportal.web.config.PublicApiTags;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PublicApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = PublicApiTags.STUDIES, description = " ")
public class StudyController {

    @Value("${app.name:unknown}")
    private String appName;

    @Value("${authenticate}")
    private String authenticate;

    @Value("${skin.home_page.show_unauthorized_studies:false}")
    private boolean showUnauthorizedStudiesOnHomePage;

    @Autowired
    private StudyService studyService;
    
    // This is a stop-gap solution because this endpoint needs caching
    // Right now this method has spontaneous performance problems
    // for the default query. We felt the best stop gap would be
    // to just manually cache that one response.
    private static List<CancerStudy> defaultResponse;
    
    @PostConstruct
    private void warmDefaultResponseCache() {
        if (!PortalSecurityConfig.userAuthorizationEnabled(authenticate)) {
            defaultResponse = studyService.getAllStudies(
                null, Projection.SUMMARY.name(),
                10000000, 0,
                null, Direction.ASC.name(), null,AccessLevel.READ);
        }
    }

    @RequestMapping(value = "/studies", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all studies")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = CancerStudy.class))))
    public ResponseEntity<List<CancerStudy>> getAllStudies(
        @Parameter(description = "Search keyword that applies to name and cancer type of the studies")
        @RequestParam(required = false) String keyword,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @Parameter(description = "Page size of the result list")
        @Max(PagingConstants.MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false) StudySortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction)
        {

        Authentication authentication = null;    
        // Only use this feature on the public portal and make sure it is never used
        // on portals using auth, as in auth setting, different users will have different
        // results.
        if (!PortalSecurityConfig.userAuthorizationEnabled(authenticate)
                && appName.equals("public-portal")
                && keyword == null
                && projection == Projection.SUMMARY
                && pageSize == 10000000
                && pageNumber == 0
                && sortBy == null
                && direction == Direction.ASC) {
            return new ResponseEntity<>(defaultResponse, HttpStatus.OK);
        }
        else
            authentication = SecurityContextHolder.getContext().getAuthentication();

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, studyService.getMetaStudies(keyword).getTotalCount()
                .toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                studyService.getAllStudies(keyword, projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name(), authentication, getAccessLevel()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/{studyId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content( schema = @Schema(implementation = CancerStudy.class)))
    public ResponseEntity<CancerStudy> getStudy(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable String studyId) throws StudyNotFoundException {

        return new ResponseEntity<>(studyService.getStudy(studyId), HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#studyIds, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch studies by IDs")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = CancerStudy.class))))
    public ResponseEntity<List<CancerStudy>> fetchStudies(
        @Parameter(required = true, description = "List of Study IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody List<String> studyIds,
        @Parameter(description = "Level of detail of the response")
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

    @RequestMapping(value = "/studies/{studyId}/tags", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get the tags of a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = Object.class)))
    public ResponseEntity<Object> getTags(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable String studyId) throws JsonParseException, JsonMappingException,
        IOException {

        Map<String,Object> map = new HashMap<String,Object>();
        ObjectMapper mapper = new ObjectMapper();
        CancerStudyTags cancerStudyTags = studyService.getTags(studyId, getAccessLevel());
        if (cancerStudyTags != null) { //If tags is null an empty map is returned
            map = mapper.readValue(cancerStudyTags.getTags(), Map.class);
        }

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#studyIds, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PreFilter("hasPermission(#studyIds, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/tags/fetch", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get the study tags by IDs")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = CancerStudyTags.class))))
    public ResponseEntity<List<CancerStudyTags>> getTagsForMultipleStudies(
        @Parameter(required = true, description = "List of Study IDs")
        @RequestBody List<String> studyIds
    ) {

        List<CancerStudyTags> cancerStudyTags = studyService.getTagsForMultipleStudies(studyIds);

        return new ResponseEntity<>(cancerStudyTags, HttpStatus.OK);
    }
    
    private AccessLevel getAccessLevel() {
        return PortalSecurityConfig.userAuthorizationEnabled(authenticate)
            && showUnauthorizedStudiesOnHomePage ? AccessLevel.LIST : AccessLevel.READ;
    }
}
