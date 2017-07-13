package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.GenePanelDataFilter;
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

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@PublicApi
@RestController
@Validated
@Api(tags = "Genes Panels", description = " ")
public class GenePanelController {

    @Autowired
    private GenePanelService genePanelService;

    @RequestMapping(value = "/gene-panels", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all gene panels")
    public ResponseEntity<List<GenePanel>> getAllGenePanels(
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
        @RequestParam(required = false) GenePanelSortBy sortBy,
        @ApiParam("Direction of the sort")
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
    @ApiOperation("Get gene panel")
    public ResponseEntity<GenePanel> getGenePanel(
        @ApiParam(required = true, value = "Gene Panel ID e.g. NSCLC_UNITO_2016_PANEL")
        @PathVariable String genePanelId) throws GenePanelNotFoundException {

        return new ResponseEntity<>(genePanelService.getGenePanel(genePanelId), HttpStatus.OK);
    }

    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/gene-panel-data/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get gene panel data")
    public ResponseEntity<List<GenePanelData>> getGenePanelData(
        @ApiParam(required = true, value = "Genetic Profile ID e.g. nsclc_unito_2016_mutations")
        @PathVariable String geneticProfileId,
        @ApiParam(required = true, value = "List of Sample IDs/Sample List ID and Entrez Gene IDs")
        @Valid @RequestBody GenePanelDataFilter genePanelDataFilter) throws GeneticProfileNotFoundException {

        List<GenePanelData> genePanelDataList;
        if (genePanelDataFilter.getSampleListId() != null) {
            genePanelDataList = genePanelService.getGenePanelData(geneticProfileId,
                genePanelDataFilter.getSampleListId(), genePanelDataFilter.getEntrezGeneIds());
        } else {
            genePanelDataList = genePanelService.fetchGenePanelData(geneticProfileId,
                genePanelDataFilter.getSampleIds(), genePanelDataFilter.getEntrezGeneIds());
        }

        return new ResponseEntity<>(genePanelDataList, HttpStatus.OK);
    }
}
