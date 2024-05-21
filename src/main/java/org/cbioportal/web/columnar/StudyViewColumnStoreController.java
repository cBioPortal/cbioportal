package org.cbioportal.web.columnar;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DensityPlotData;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.Sample;
import org.cbioportal.service.ClinicalDataDensityPlotService;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.columnar.util.NewStudyViewFilterUtil;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.ClinicalDataBinCountFilter;
import org.cbioportal.web.parameter.ClinicalDataCountFilter;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.DataBinMethod;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.DensityPlotParameters;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
public class StudyViewColumnStoreController {
    
    private final StudyViewColumnarService studyViewColumnarService;
    private final ClinicalDataBinner clinicalDataBinner;
    private final ClinicalDataDensityPlotService clinicalDataDensityPlotService;
    
    @Autowired
    public StudyViewColumnStoreController(StudyViewColumnarService studyViewColumnarService, 
                                          ClinicalDataBinner clinicalDataBinner,
                                          ClinicalDataDensityPlotService clinicalDataDensityPlotService
                                          ) {
        this.studyViewColumnarService = studyViewColumnarService;
        this.clinicalDataBinner = clinicalDataBinner;
        this.clinicalDataDensityPlotService = clinicalDataDensityPlotService;
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
        return new ResponseEntity<>(
            studyViewColumnarService.getMutatedGenes(interceptedStudyViewFilter),
            HttpStatus.OK
        );
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/column-store/molecular-profile-sample-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch sample counts by study view filter")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenomicDataCount.class))))
    public ResponseEntity<List<GenomicDataCount>> fetchMolecularProfileSampleCounts(
        @Parameter(required = true, description = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter
    )
    {
        return new ResponseEntity<List<GenomicDataCount>>(
            studyViewColumnarService.getGenomicDataCounts(interceptedStudyViewFilter)
            , HttpStatus.OK);
    }
    
    @PostMapping(value = "/column-store/cna-genes/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CopyNumberCountByGene>> fetchCnaGenes(
        @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter
    ) {
        return new ResponseEntity<>(
            studyViewColumnarService.getCnaGenes(interceptedStudyViewFilter),
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
        List<ClinicalDataBin> clinicalDataBins = clinicalDataBinner.fetchClinicalDataBinCounts(
            dataBinMethod,
            interceptedClinicalDataBinCountFilter,
            true
        );
        return new ResponseEntity<>(clinicalDataBins, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/column-store/clinical-data-density-plot/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch clinical data density plot bins by study view filter")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = DensityPlotData.class)))
    @Validated
    public ResponseEntity<DensityPlotData> fetchClinicalDataDensityPlot(
        @Parameter(required = true, description = "Clinical Attribute ID of the X axis")
        @RequestParam String xAxisAttributeId,
        @Parameter(description = "Number of the bins in X axis")
        @RequestParam(defaultValue = "50") Integer xAxisBinCount,
        @Parameter(description = "Starting point of the X axis, if different than smallest value")
        @RequestParam(required = false) BigDecimal xAxisStart,
        @Parameter(description = "Starting point of the X axis, if different than largest value")
        @RequestParam(required = false) BigDecimal xAxisEnd,
        @Parameter(required = true, description = "Clinical Attribute ID of the Y axis")
        @RequestParam String yAxisAttributeId,
        @Parameter(description = "Number of the bins in Y axis")
        @RequestParam(defaultValue = "50") Integer yAxisBinCount,
        @Parameter(description = "Starting point of the Y axis, if different than smallest value")
        @RequestParam(required = false) BigDecimal yAxisStart,
        @Parameter(description = "Starting point of the Y axis, if different than largest value")
        @RequestParam(required = false) BigDecimal yAxisEnd,
        @Parameter(description="Use log scale for X axis")
        @RequestParam(required = false, defaultValue = "false") Boolean xAxisLogScale,
        @Schema(defaultValue = "false")
        @Parameter(description="Use log scale for Y axis")
        @RequestParam(required = false, defaultValue = "false") Boolean yAxisLogScale,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter,
        @Parameter(required = true, description = "Study view filter")
        @RequestBody(required = false) StudyViewFilter studyViewFilter) {
        
        List<String> xyAttributeId = new ArrayList<>(Arrays.asList(xAxisAttributeId, yAxisAttributeId));
        DensityPlotParameters densityPlotParameters = 
            new DensityPlotParameters.Builder()
            .xAxisAttributeId(xAxisAttributeId)
            .yAxisAttributeId(yAxisAttributeId)
            .xAxisBinCount(xAxisBinCount)
            .yAxisBinCount(yAxisBinCount)
            .xAxisStart(xAxisStart)
            .yAxisStart(yAxisStart)
            .xAxisEnd(xAxisEnd)
            .yAxisEnd(yAxisEnd)
            .xAxisLogScale(xAxisLogScale)
            .yAxisLogScale(yAxisLogScale)
            .build();
        
        List<ClinicalData> sampleClinicalDataList = studyViewColumnarService.getSampleClinicalData(interceptedStudyViewFilter, xyAttributeId);
        DensityPlotData result = clinicalDataDensityPlotService.getDensityPlotData(sampleClinicalDataList, densityPlotParameters);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
