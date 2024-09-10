package org.cbioportal.web.columnar;

import io.swagger.v3.oas.annotations.Hidden;
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
import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.ClinicalViolinPlotData;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DensityPlotData;
import org.cbioportal.model.GenericAssayDataBin;
import org.cbioportal.model.GenomicDataBin;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.PatientTreatmentReport;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleTreatmentReport;
import org.cbioportal.service.ClinicalDataDensityPlotService;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.service.ViolinPlotService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.columnar.util.NewStudyViewFilterUtil;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.ClinicalDataBinCountFilter;
import org.cbioportal.web.parameter.ClinicalDataCountFilter;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.DataBinMethod;
import org.cbioportal.web.parameter.GenericAssayDataBinCountFilter;
import org.cbioportal.web.parameter.GenomicDataBinCountFilter;
import org.cbioportal.web.parameter.GenomicDataCountFilter;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.cbioportal.web.parameter.MutationOption;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.DensityPlotParameters;
import org.cbioportal.web.util.StudyViewFilterUtil;
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

    private final BasicDataBinner basicDataBinner;
    private final ClinicalDataDensityPlotService clinicalDataDensityPlotService;
    private final ViolinPlotService violinPlotService;
    
    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;

    @Autowired
    public StudyViewColumnStoreController(StudyViewColumnarService studyViewColumnarService, 
                                          ClinicalDataBinner clinicalDataBinner,
                                          BasicDataBinner basicDataBinner,
                                          ClinicalDataDensityPlotService clinicalDataDensityPlotService,
                                          ViolinPlotService violinPlotService
                                          ) {
        this.studyViewColumnarService = studyViewColumnarService;
        this.clinicalDataBinner = clinicalDataBinner;
        this.basicDataBinner = basicDataBinner;
        this.clinicalDataDensityPlotService = clinicalDataDensityPlotService;
        this.violinPlotService = violinPlotService;
    }
    

    @Hidden
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

    @Hidden
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

    @Hidden
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
            studyViewColumnarService.getMolecularProfileSampleCounts(interceptedStudyViewFilter)
            , HttpStatus.OK);
    }

    @Hidden
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

    @Hidden
    @PostMapping(value = "/column-store/structuralvariant-genes/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch structural variant genes by study view filter")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlterationCountByGene.class))))
    public ResponseEntity<List<AlterationCountByGene>> fetchStructuralVariantGenes(
        @Parameter(required = true, description = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. This attribute is needed for the @PreAuthorize tag above.
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter
    ) {
        return new ResponseEntity<>(studyViewColumnarService.getStructuralVariantGenes(interceptedStudyViewFilter), HttpStatus.OK);
    }

    @Hidden
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

    @Hidden
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

    @Hidden
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

    @Hidden
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/column-store/clinical-data-density-plot/fetch", consumes = MediaType.APPLICATION_JSON_VALUE, 
        produces = MediaType.APPLICATION_JSON_VALUE)
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

    @Hidden
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/column-store/clinical-data-violin-plots/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch violin plot curves per categorical clinical data value, filtered by study view filter")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = ClinicalViolinPlotData.class)))
    public ResponseEntity<ClinicalViolinPlotData> fetchClinicalDataViolinPlots(
        @Parameter(required = true, description = "Clinical Attribute ID of the categorical attribute")
        @RequestParam String categoricalAttributeId,
        @Parameter(required = true, description = "Clinical Attribute ID of the numerical attribute")
        @RequestParam String numericalAttributeId,
        @Parameter(description = "Starting point of the violin plot axis, if different than smallest value")
        @RequestParam(required = false) BigDecimal axisStart,
        @Parameter(description = "Ending point  of the violin plot axis, if different than largest value")
        @RequestParam(required = false) BigDecimal axisEnd,
        @Parameter(description = "Number of points in the curve")
        @RequestParam(required = false, defaultValue = "100") BigDecimal numCurvePoints,
        @Parameter(description="Use log scale for the numerical attribute")
        @RequestParam(required = false, defaultValue = "false") Boolean logScale,
        @Parameter(description="Sigma stepsize multiplier")
        @RequestParam(required = false, defaultValue = "1") BigDecimal sigmaMultiplier,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter,
        @Parameter(required = true, description = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter) {
        
        List<Sample> filteredSamples = studyViewColumnarService.getFilteredSamples(interceptedStudyViewFilter);
        
        // get samples that are filtered without the numerical filter - this is violin plot data
        if (interceptedStudyViewFilter.getClinicalDataFilters() != null) {
            interceptedStudyViewFilter.getClinicalDataFilters().stream()
                .filter(f->f.getAttributeId().equals(numericalAttributeId))
                .findAny()
                .ifPresent(f->interceptedStudyViewFilter.getClinicalDataFilters().remove(f));
        }

        // Filter out clinical data with empty attribute values due to Clickhouse migration
        List<ClinicalData> sampleClinicalDataList = studyViewColumnarService.getSampleClinicalData(interceptedStudyViewFilter, List.of(numericalAttributeId, categoricalAttributeId))
            .stream()
            .filter(clinicalData -> !clinicalData.getAttrValue().isEmpty())
            .toList();
        
        // Only mutation count can use log scale
        boolean useLogScale = logScale && numericalAttributeId.equals("MUTATION_COUNT");
        
        ClinicalViolinPlotData result = violinPlotService.getClinicalViolinPlotData(
            sampleClinicalDataList,
            filteredSamples,
            axisStart,
            axisEnd,
            numCurvePoints,
            useLogScale,
            sigmaMultiplier
        );

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Hidden
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/column-store/genomic-data-counts/fetch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch genomic data counts by GenomicDataCountFilter")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenomicDataCountItem.class))))
    public ResponseEntity<List<GenomicDataCountItem>> fetchGenomicDataCounts(
            @Parameter(required = true, description = "Genomic data count filter") @Valid @RequestBody(required = false) GenomicDataCountFilter genomicDataCountFilter,
            @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
            @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
            @Parameter(required = true, description = "Intercepted Genomic Data Count Filter")
            @Valid @RequestAttribute(required = false, value = "interceptedGenomicDataCountFilter") GenomicDataCountFilter interceptedGenomicDataCountFilter
    ) throws StudyNotFoundException {
        List<GenomicDataFilter> genomicDataFilters = interceptedGenomicDataCountFilter.getGenomicDataFilters();
        StudyViewFilter studyViewFilter = interceptedGenomicDataCountFilter.getStudyViewFilter();
        // when there is only one filter, it means study view is doing a single chart filter operation
        // remove filter from studyViewFilter to return all data counts
        // the reason we do this is to make sure after chart get filtered, user can still see unselected portion of the chart
        if (genomicDataFilters.size() == 1) {
            studyViewFilterUtil.removeSelfFromGenomicDataFilter(
                    genomicDataFilters.get(0).getHugoGeneSymbol(),
                    genomicDataFilters.get(0).getProfileType(),
                    studyViewFilter);
        }

        // This endpoint is CNA specific. The name choice of "genomic data" does not imply it support other genomic data types
        List<GenomicDataCountItem> result = studyViewColumnarService.getCNACountsByGeneSpecific(studyViewFilter, genomicDataFilters);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/column-store/mutation-data-counts/fetch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch mutation data counts by GenomicDataCountFilter")
    public ResponseEntity<List<GenomicDataCountItem>> fetchMutationDataCounts(
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @Parameter(required = true, description = "Genomic data count filter")
        @Valid @RequestBody(required = false) GenomicDataCountFilter genomicDataCountFilter,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @Valid @RequestAttribute(required = false, value = "interceptedGenomicDataCountFilter") GenomicDataCountFilter interceptedGenomicDataCountFilter
    ) {
        List<GenomicDataFilter> genomicDataFilters = interceptedGenomicDataCountFilter.getGenomicDataFilters();
        StudyViewFilter studyViewFilter = interceptedGenomicDataCountFilter.getStudyViewFilter();
        // when there is only one filter, it means study view is doing a single chart filter operation
        // remove filter from studyViewFilter to return all data counts
        // the reason we do this is to make sure after chart get filtered, user can still see unselected portion of the chart
        if (genomicDataFilters.size() == 1 && projection == Projection.SUMMARY) {
            studyViewFilterUtil.removeSelfFromMutationDataFilter(
                genomicDataFilters.get(0).getHugoGeneSymbol(),
                genomicDataFilters.get(0).getProfileType(),
                MutationOption.MUTATED,
                studyViewFilter);
        }

        List<GenomicDataCountItem> result = projection == Projection.SUMMARY ?
            studyViewColumnarService.getMutationCountsByGeneSpecific(studyViewFilter, genomicDataFilters) :
            studyViewColumnarService.getMutationTypeCountsByGeneSpecific(studyViewFilter, genomicDataFilters);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/column-store/clinical-event-type-counts/fetch", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get Counts of Clinical Event Types by Study View Filter")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalEventTypeCount.class))))
    public ResponseEntity<List<ClinicalEventTypeCount>> getClinicalEventTypeCounts(
        @Parameter(required = true, description = "Study view filter")
        @Valid
        @RequestBody(required = false)
        StudyViewFilter studyViewFilter,

        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies")
        Collection<String> involvedCancerStudies,

        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid
        @RequestAttribute(required = false, value = "interceptedStudyViewFilter")
        StudyViewFilter interceptedStudyViewFilter
    ) {
        return new ResponseEntity<>(studyViewColumnarService.getClinicalEventTypeCounts(interceptedStudyViewFilter), HttpStatus.OK);
    }
    
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/column-store/treatments/patient-counts/fetch", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all patient level treatments")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = PatientTreatmentReport.class)))
    public ResponseEntity<PatientTreatmentReport> fetchPatientTreatmentCounts(
        @Parameter(required = false )
        @RequestParam(name = "tier", required = false, defaultValue = "Agent")
        ClinicalEventKeyCode tier,

        @Parameter(required = true, description = "Study view filter")
        @Valid
        @RequestBody(required = false)
        StudyViewFilter studyViewFilter,

        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies")
        Collection<String> involvedCancerStudies,

        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid
        @RequestAttribute(required = false, value = "interceptedStudyViewFilter")
        StudyViewFilter interceptedStudyViewFilter
    ) {
        return new ResponseEntity<>(studyViewColumnarService.getPatientTreatmentReport(interceptedStudyViewFilter),
            HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/column-store/treatments/sample-counts/fetch", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = SampleTreatmentReport.class)))
    public ResponseEntity<SampleTreatmentReport> fetchSampleTreatmentCounts(
        @Parameter(required = false )
        @RequestParam(name = "tier", required = false, defaultValue = "Agent")
        ClinicalEventKeyCode tier,

        @Parameter(required = true, description = "Study view filter")
        @Valid
        @RequestBody(required = false)
        StudyViewFilter studyViewFilter,

        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies")
        Collection<String> involvedCancerStudies,

        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid
        @RequestAttribute(required = false, value = "interceptedStudyViewFilter")
        StudyViewFilter interceptedStudyViewFilter
    ) {
        return new ResponseEntity<>(studyViewColumnarService.getSampleTreatmentReport(interceptedStudyViewFilter),
            HttpStatus.OK);
    }
    
//    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
//    @RequestMapping(value = "/column-store/custom-data-bin-counts/fetch", method = RequestMethod.POST,
//        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(description = "Fetch custom data bin counts by study view filter")
//    @ApiResponse(responseCode = "200", description = "OK",
//        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalDataBin.class))))
//    public ResponseEntity<List<ClinicalDataBin>> fetchCustomDataBinCounts(
//        @Parameter(description = "Method for data binning")
//        @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
//        @Parameter(required = true, description = "Clinical data bin count filter")
//        @Valid @RequestBody(required = false) ClinicalDataBinCountFilter clinicalDataBinCountFilter,
//        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
//        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
//        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
//        @Valid @RequestAttribute(required = false, value = "interceptedClinicalDataBinCountFilter") ClinicalDataBinCountFilter interceptedClinicalDataBinCountFilter
//    ) {
//        // TODO code shared with ClinicalDataController.fetchCustomDataCounts
//        List<ClinicalDataBinFilter> attributes = interceptedClinicalDataBinCountFilter.getAttributes();
//        StudyViewFilter studyViewFilter = interceptedClinicalDataBinCountFilter.getStudyViewFilter();
//        if (attributes.size() == 1) {
//            NewStudyViewFilterUtil.removeSelfCustomDataFromFilter(attributes.get(0).getAttributeId(), studyViewFilter);
//        }
//        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);
//
//        if (filteredSampleIdentifiers.isEmpty()) {
//            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
//        }
//
//        final List<ClinicalDataBin> clinicalDataBins = clinicalDataBinUtil.fetchCustomDataBinCounts(dataBinMethod, interceptedClinicalDataBinCountFilter, false);
//
//        return new ResponseEntity<>(clinicalDataBins, HttpStatus.OK);
//    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/column-store/genomic-data-bin-counts/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenomicDataBin.class))))
    public ResponseEntity<List<GenomicDataBin>> fetchGenomicDataBinCounts(
        @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
        @RequestBody(required = false) GenomicDataBinCountFilter genomicDataBinCountFilter,
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @RequestAttribute(required = false, value = "interceptedGenomicDataBinCountFilter") GenomicDataBinCountFilter interceptedGenomicDataBinCountFilter
    ) {
        List<GenomicDataBin> genomicDataBins = basicDataBinner.getDataBins(
            dataBinMethod,
            interceptedGenomicDataBinCountFilter,
            true
        );
        return new ResponseEntity<>(genomicDataBins, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/column-store/generic-assay-data-bin-counts/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenericAssayDataBin.class))))
    public ResponseEntity<List<GenericAssayDataBin>> fetchGenericAssayDataBinCounts(
        @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
        @RequestBody(required = false) GenericAssayDataBinCountFilter genericAssayDataBinCountFilter,
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @RequestAttribute(required = false, value = "interceptedGenericAssayDataBinCountFilter") GenericAssayDataBinCountFilter interceptedGenericAssayDataBinCountFilter
    ) {
        List<GenericAssayDataBin> genericAssayDataBins = basicDataBinner.getDataBins(
            dataBinMethod,
            interceptedGenericAssayDataBinCountFilter,
            true
        );
        return new ResponseEntity<>(genericAssayDataBins, HttpStatus.OK);
    }

}
