package org.cbioportal.web.columnar;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.cbioportal.model.*;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.service.StudyViewService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.columnar.util.NewStudyViewFilterUtil;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.ClinicalDataBinUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
public class StudyViewColumnStoreController {
    
    private final StudyViewColumnarService studyViewColumnarService;
    private final StudyViewService studyViewService;
    private final ClinicalDataBinUtil clinicalDataBinUtil;
    
    @Autowired
    public StudyViewColumnStoreController(StudyViewColumnarService studyViewColumnarService, StudyViewService studyViewService, ClinicalDataBinUtil clinicalDataBinUtil) {
        this.studyViewColumnarService = studyViewColumnarService;
        this.studyViewService = studyViewService;
        this.clinicalDataBinUtil = clinicalDataBinUtil;
    }
    
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/column-store/filtered-samples/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Sample>> fetchFilteredSamples(
        @RequestParam(defaultValue = "false") Boolean negateFilters,
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter,
        @RequestBody(required = false) StudyViewFilter studyViewFilter) {
        return new ResponseEntity<>(
            studyViewColumnarService.getFilteredSamples(interceptedStudyViewFilter),
            HttpStatus.OK
        );
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/column-store/mutated-genes/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AlterationCountByGene>> fetchMutatedGenes(
        @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter
    ) throws StudyNotFoundException {
        AlterationFilter annotationFilters = interceptedStudyViewFilter.getAlterationFilter();
        List<Sample> samples = studyViewColumnarService.getFilteredSamples(interceptedStudyViewFilter);
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        for(Sample sample : samples) {
            studyIds.add(sample.getCancerStudyIdentifier());
            sampleIds.add(sample.getStableId());
        }
        return new ResponseEntity<>(
            studyViewService.getMutationAlterationCountByGenes(studyIds, sampleIds, annotationFilters),
            HttpStatus.OK
        );
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/column-store/clinical-data-counts/fetch", 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClinicalDataCountItem>> fetchClinicalDataCounts(
        @RequestBody(required = false) ClinicalDataCountFilter clinicalDataCountFilter,
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @RequestAttribute(required = false, value = "interceptedClinicalDataCountFilter") ClinicalDataCountFilter interceptedClinicalDataCountFilter) {

        List<ClinicalDataFilter> attributes = interceptedClinicalDataCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = interceptedClinicalDataCountFilter.getStudyViewFilter();

        if (attributes.size() == 1) {
            NewStudyViewFilterUtil.removeSelfFromFilter(attributes.get(0).getAttributeId(), studyViewFilter);
       }
       // boolean singleStudyUnfiltered = studyViewFilterUtil.isSingleStudyUnfiltered(studyViewFilter);
        List<ClinicalDataCountItem> result = studyViewColumnarService.getClinicalDataCounts(studyViewFilter, 
            attributes.stream().map(ClinicalDataFilter::getAttributeId).collect(Collectors.toList()));
            //studyIds, sampleIds, attributes.stream().map(a -> a.getAttributeId()).collect(Collectors.toList())); 
        return new ResponseEntity<>(result, HttpStatus.OK);

    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/column-store/sample-lists-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch case list sample counts by study view filter")
    public List<CaseListDataCount> fetchCaseListCounts(
        @Parameter(required = true, description = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter) {

        return studyViewColumnarService.getCaseListDataCounts(interceptedStudyViewFilter);
        
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/column-store/clinical-data-bin-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClinicalDataBin>> fetchClinicalDataBinCounts(
        @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
        @RequestBody(required = false) ClinicalDataBinCountFilter clinicalDataBinCountFilter,
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @RequestAttribute(required = false, value = "interceptedClinicalDataBinCountFilter") ClinicalDataBinCountFilter interceptedClinicalDataBinCountFilter
    ) {
        List<ClinicalDataBin> clinicalDataBins = clinicalDataBinUtil.fetchClinicalDataBinCounts(
            dataBinMethod,
            interceptedClinicalDataBinCountFilter,
            true
        );
        return new ResponseEntity<>(clinicalDataBins, HttpStatus.OK);
    }
}
