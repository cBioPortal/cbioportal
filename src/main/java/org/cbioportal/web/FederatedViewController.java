package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.service.exception.SummaryDataException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@PublicApi
@RestController()
@RequestMapping("/api-fed")
@Validated
@Tag(name = PublicApiTags.CLINICAL_ATTRIBUTES, description = " ")
public class FederatedViewController {
    @PreAuthorize("hasPermission(#studyIds, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/clinical-attributes/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch clinical attributes")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalAttribute.class))))
    public ResponseEntity<List<ClinicalAttribute>> fetchClinicalAttributes(
        @Parameter(required = true, description = "List of Study IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody List<String> studyIds,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws SummaryDataException {

        if (summaryDataService.supportsStudies(studyIds)) {
            return new ResponseEntity<>(summaryDataService.fetchClinicalAttributes(studyIds, projection), HttpStatus.OK);
        }

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, clinicalAttributeService.fetchMetaClinicalAttributes(
                studyIds).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(clinicalAttributeService.fetchClinicalAttributes(studyIds, projection.name()),
                HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/clinical-data-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch clinical data counts by study view filter")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalDataCountItem.class))))
    public ResponseEntity<List<ClinicalDataCountItem>> fetchClinicalDataCounts(
        @Parameter(required = true, description = "Clinical data count filter")
        @Valid @RequestBody(required = false)  ClinicalDataCountFilter clinicalDataCountFilter,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedClinicalDataCountFilter") ClinicalDataCountFilter interceptedClinicalDataCountFilter
    ) throws SummaryDataException {

        List<ClinicalDataFilter> attributes = interceptedClinicalDataCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = interceptedClinicalDataCountFilter.getStudyViewFilter();

        if (attributes.size() == 1) {
            studyViewFilterUtil.removeSelfFromFilter(attributes.get(0).getAttributeId(), studyViewFilter);
        }
        boolean unfilteredQuery = studyViewFilterUtil.isUnfilteredQuery(studyViewFilter);
        List<ClinicalDataCountItem> result =
            this.getInstance().cachedClinicalDataCounts(interceptedClinicalDataCountFilter,
                unfilteredQuery);
        return new ResponseEntity<>(result, HttpStatus.OK);

    }
    
    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabled() && #unfilteredQuery"
    )
    public List<ClinicalDataCountItem> cachedClinicalDataCounts(ClinicalDataCountFilter interceptedClinicalDataCountFilter,
                                                                boolean unfilteredQuery
    ) throws SummaryDataException {
        List<ClinicalDataFilter> attributes = interceptedClinicalDataCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = interceptedClinicalDataCountFilter.getStudyViewFilter();
        if (attributes.size() == 1) {
            studyViewFilterUtil.removeSelfFromFilter(attributes.get(0).getAttributeId(), studyViewFilter);
        }

        List<String> studyIds = studyViewFilter.getStudyIds();
        if (summaryDataService.supportsStudies(studyIds)) {
            return summaryDataService.fetchClinicalDataCounts(
                interceptedClinicalDataCountFilter
            );
        }

        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);

        if (filteredSampleIdentifiers.isEmpty()) {
            return new ArrayList<>();
        }
        studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);

        List<ClinicalDataCountItem> result = clinicalDataService.fetchClinicalDataCounts(
            studyIds, sampleIds, attributes.stream().map(a -> a.getAttributeId()).collect(Collectors.toList()));

        return result;
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/clinical-data-bin-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch clinical data bin counts by study view filter")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalDataBin.class))))
    public ResponseEntity<List<ClinicalDataBin>> fetchClinicalDataBinCounts(
        @Parameter(description = "Method for data binning")
        @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
        @Parameter(required = true, description = "Clinical data bin count filter")
        @Valid @RequestBody(required = false) ClinicalDataBinCountFilter clinicalDataBinCountFilter,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedClinicalDataBinCountFilter") ClinicalDataBinCountFilter interceptedClinicalDataBinCountFilter
    ) throws SummaryDataException {
        StudyViewFilter studyViewFilter = clinicalDataBinUtil.removeSelfFromFilter(interceptedClinicalDataBinCountFilter);
        boolean unfilteredQuery = studyViewFilterUtil.isUnfilteredQuery(studyViewFilter);
        List<ClinicalDataBin> clinicalDataBins =
            this.getInstance().cachableFetchClinicalDataBinCounts(dataBinMethod,
                interceptedClinicalDataBinCountFilter,
                unfilteredQuery);

        return new ResponseEntity<>(clinicalDataBins, HttpStatus.OK);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabled() && #unfilteredQuery"
    )
    public List<ClinicalDataBin> cachableFetchClinicalDataBinCounts(DataBinMethod dataBinMethod,
                                                                    ClinicalDataBinCountFilter interceptedClinicalDataBinCountFilter,
                                                                    boolean unfilteredQuery
    ) throws SummaryDataException {
        List<ClinicalDataBinFilter> attributes = interceptedClinicalDataBinCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = clinicalDataBinUtil.removeSelfFromFilter(interceptedClinicalDataBinCountFilter);

        List<String> studyIds = studyViewFilter.getStudyIds();
        if (summaryDataService.supportsStudies(studyIds)) {
            return summaryDataService.fetchClinicalDataBinCounts(
                interceptedClinicalDataBinCountFilter,
                dataBinMethod
            );
        }

        return clinicalDataBinUtil.fetchClinicalDataBinCounts(
            dataBinMethod,
            interceptedClinicalDataBinCountFilter,
            // we don't need to remove filter again since we already did it in the previous step 
            false
        );
    }

}
