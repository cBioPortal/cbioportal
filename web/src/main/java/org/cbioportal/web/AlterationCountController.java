package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.*;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.AlterationCountFilter;
import org.cbioportal.web.util.AlterationCountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.*;

@InternalApi
@RestController
@Validated
@Api(tags = "Alteration Counts")
public class AlterationCountController {
    @Autowired
    private AlterationCountUtil alterationCountUtil;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/alteration-counts/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch alteration counts in molecular profiles")
    public ResponseEntity<AlterationCountDetailed> fetchAlterationCounts(
        @ApiParam(value = "List of entrez gene ids")
        @RequestParam(required = false)
        List<Integer> entrezGeneIds,
        @ApiParam("Type of the count e.g. SAMPLE or PATIENT")
        @RequestParam(defaultValue = "SAMPLE") AlterationCountType alterationCountType,
        @ApiParam(required = true, value = "Alteration Filter")
        @Valid @RequestBody(required = false)
        AlterationCountFilter alterationCountFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface.
        @Valid @RequestAttribute(required = false, value = "interceptedAlterationCountFilter")
        AlterationCountFilter interceptedAlterationCountFilter
    ) {
        Pair<List<AlterationCountByGene>, Long> alterationCount = this.alterationCountUtil.getAlterationCounts(
            interceptedAlterationCountFilter,
            alterationCountType,
            entrezGeneIds
        );
        List<AlterationCountByGene> alterationCountsByGene = alterationCount.getFirst();
        Long profiledCaseCount = alterationCount.getSecond();
        Set<GenePanelToGene> profiledGenes = this.alterationCountUtil.getProfiledGenes(
            interceptedAlterationCountFilter,
            alterationCountType,
            entrezGeneIds
        );
        boolean allGenesProfiled = profiledGenes == null;
        
        return new ResponseEntity<>(
            new AlterationCountDetailed(
                alterationCountsByGene,
                profiledCaseCount,
                profiledGenes,
                allGenesProfiled
            ),
            HttpStatus.OK
        );
    }
}
