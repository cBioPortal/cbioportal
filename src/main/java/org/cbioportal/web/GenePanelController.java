package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.cbioportal.model.GenePanel;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.sort.GenePanelSortBy;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;

@PublicApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = PublicApiTags.GENE_PANELS, description = " ")
public class GenePanelController {

    @Autowired
    private GenePanelService genePanelService;
    
    @Hidden
    @RequestMapping(value = "/gene-panels/", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GenePanel>> getAllGenePanels_trailing(
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @Parameter(description = "Page size of the result list")
        @Max(PagingConstants.MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @RequestParam(required = false) GenePanelSortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) {
          return  getAllGenePanels(projection, pageSize, pageNumber, sortBy, direction); 
    }
    
    @RequestMapping(value = "/gene-panels", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all gene panels")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenePanel.class))))
    public ResponseEntity<List<GenePanel>> getAllGenePanels(
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
        @RequestParam(required = false) GenePanelSortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, genePanelService.getMetaGenePanels()
                .getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                genePanelService.getAllGenePanels(projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/gene-panels/{genePanelId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get gene panel")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = GenePanel.class)))
    public ResponseEntity<GenePanel> getGenePanel(
        @Parameter(required = true, description = "Gene Panel ID e.g. NSCLC_UNITO_2016_PANEL")
        @PathVariable String genePanelId) throws GenePanelNotFoundException {

        return new ResponseEntity<>(genePanelService.getGenePanel(genePanelId), HttpStatus.OK);
    }

    @RequestMapping(value = "/gene-panels/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get gene panel")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenePanel.class))))
    public ResponseEntity<List<GenePanel>> fetchGenePanels(
        @Parameter(required = true, description = "List of Gene Panel IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody List<String> genePanelIds,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) {

        return new ResponseEntity<>(genePanelService.fetchGenePanels(genePanelIds, projection.name()), HttpStatus.OK);
    }
}
