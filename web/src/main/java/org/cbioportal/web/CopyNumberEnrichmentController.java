package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.Entity;
import org.cbioportal.service.CopyNumberEnrichmentService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.CopyNumberEnrichmentEventType;
import org.cbioportal.web.parameter.MultipleStudiesEnrichmentFilter;
import org.cbioportal.web.parameter.EnrichmentFilter;
import org.cbioportal.web.parameter.EnrichmentType;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.Valid;
import java.util.List;
import java.util.ArrayList;
@InternalApi
@RestController
@Validated
@Api(tags = "Copy Number Enrichments", description = " ")
public class CopyNumberEnrichmentController {

    @Autowired
    private CopyNumberEnrichmentService copyNumberEnrichmentService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', 'read')")
    @RequestMapping(value = "/copy-number-enrichments/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch copy number enrichments in a molecular profile")
    public ResponseEntity<List<AlterationEnrichment>> fetchCopyNumberEnrichments(
        @ApiParam("Type of the copy number event")
        @RequestParam(defaultValue = "HOMDEL") CopyNumberEnrichmentEventType copyNumberEventType,
        @ApiParam("Type of the enrichment e.g. SAMPLE or PATIENT")
        @RequestParam(defaultValue = "SAMPLE") EnrichmentType enrichmentType,
        @ApiParam(required = true, value = "List of entities")
        @Valid @RequestBody MultipleStudiesEnrichmentFilter multipleStudiesEnrichmentFilter) throws MolecularProfileNotFoundException {
        
        String set1MolecularProfileId = multipleStudiesEnrichmentFilter.getSet1().get(0).getMolecularProfileId();
        String set2MolecularProfileId = multipleStudiesEnrichmentFilter.getSet2().get(0).getMolecularProfileId();
        List<String> set1Ids = new ArrayList<String>();
        List<String> set2Ids = new ArrayList<String>();
        for (Entity entity : multipleStudiesEnrichmentFilter.getSet1()) {
            set1Ids.add(entity.getEntityId());
        }
        for (Entity entity : multipleStudiesEnrichmentFilter.getSet2()) {
            set2Ids.add(entity.getEntityId());
        }
        List<AlterationEnrichment> alterationEnrichments = new ArrayList<AlterationEnrichment>();
        alterationEnrichments.addAll(copyNumberEnrichmentService.getCopyNumberEnrichments(set1MolecularProfileId,
            set1Ids, set2Ids, copyNumberEventType.getAlterationTypes(), enrichmentType.name()));
        return new ResponseEntity<>(alterationEnrichments, HttpStatus.OK);
    }
}
