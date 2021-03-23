package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.validation.Valid;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalAttributeCount;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.exception.ClinicalAttributeNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.InternalApiTags;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.ClinicalAttributeCountFilter;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.sort.ClinicalAttributeSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@InternalApi
@RestController
@Validated
@Api(tags = InternalApiTags.CLINICAL_ATTRIBUTES_COUNT, description = " ")
public class ClinicalAttributeCountController {

    @Autowired
    private ClinicalAttributeService clinicalAttributeService;
 
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/clinical-attributes/counts/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get counts for clinical attributes according to their data availability for selected samples/patients")
    public ResponseEntity<List<ClinicalAttributeCount>> getClinicalAttributeCounts(
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedClinicalAttributeCountFilter") ClinicalAttributeCountFilter interceptedClinicalAttributeCountFilter,
            @ApiParam(required = true, value = "List of SampleIdentifiers or Sample List ID")
            @Valid @RequestBody(required = false) ClinicalAttributeCountFilter clinicalAttributeCountFilter) {

        List<ClinicalAttributeCount> clinicalAttributeCountList;
        if (interceptedClinicalAttributeCountFilter.getSampleListId() != null) {
            clinicalAttributeCountList = clinicalAttributeService.getClinicalAttributeCountsBySampleListId(
                interceptedClinicalAttributeCountFilter.getSampleListId());
        } else {
            List<SampleIdentifier> sampleIdentifiers = interceptedClinicalAttributeCountFilter.getSampleIdentifiers();
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            for (SampleIdentifier sampleIdentifier : sampleIdentifiers) {
                studyIds.add(sampleIdentifier.getStudyId());
                sampleIds.add(sampleIdentifier.getSampleId());
            }
            clinicalAttributeCountList = clinicalAttributeService.getClinicalAttributeCountsBySampleIds(studyIds, sampleIds);
        }

        return new ResponseEntity<>(clinicalAttributeCountList, HttpStatus.OK);
    }
}
