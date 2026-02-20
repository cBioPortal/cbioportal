package org.cbioportal.application.rest.vcolumnstore;

import static java.util.stream.Collectors.toSet;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cbioportal.application.rest.mapper.SampleMapper;
import org.cbioportal.application.rest.response.SampleDTO;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.studyview.StudyViewService;
import org.cbioportal.infrastructure.service.BasicDataBinner;
import org.cbioportal.infrastructure.service.ClinicalDataBinner;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.CaseListDataCount;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataBin;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.legacy.model.ClinicalEventKeyCode;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.cbioportal.legacy.model.ClinicalViolinPlotData;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.DensityPlotData;
import org.cbioportal.legacy.model.GenericAssayDataBin;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.model.GenomicDataBin;
import org.cbioportal.legacy.model.GenomicDataCount;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.model.PatientTreatmentReport;
import org.cbioportal.legacy.model.SampleTreatmentReport;
import org.cbioportal.legacy.service.ClinicalDataDensityPlotService;
import org.cbioportal.legacy.service.CustomDataService;
import org.cbioportal.legacy.service.ViolinPlotService;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.service.util.ClinicalDataUtil;
import org.cbioportal.legacy.service.util.CustomDataSession;
import org.cbioportal.legacy.web.columnar.util.CustomDataFilterUtil;
import org.cbioportal.legacy.web.columnar.util.NewStudyViewFilterUtil;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.cbioportal.legacy.web.parameter.ClinicalDataBinCountFilter;
import org.cbioportal.legacy.web.parameter.ClinicalDataCountFilter;
import org.cbioportal.legacy.web.parameter.ClinicalDataFilter;
import org.cbioportal.legacy.web.parameter.DataBinMethod;
import org.cbioportal.legacy.web.parameter.GenericAssayDataBinCountFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataCountFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataBinCountFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataCountFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.cbioportal.legacy.web.parameter.MutationOption;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.cbioportal.legacy.web.util.DensityPlotParameters;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@InternalApi
@RestController
@RequestMapping("/api/column-store")
public class ColumnarStoreStudyViewController {

  private final StudyViewService studyViewService;
  private final BasicDataBinner basicDataBinner;
  private final ClinicalDataBinner clinicalDataBinner;
  private final ClinicalDataDensityPlotService clinicalDataDensityPlotService;
  private final ViolinPlotService violinPlotService;
  private final CustomDataService customDataService;
  private final CustomDataFilterUtil customDataFilterUtil;

  public ColumnarStoreStudyViewController(
      StudyViewService studyViewService,
      BasicDataBinner basicDataBinner,
      ClinicalDataBinner clinicalDataBinner,
      ClinicalDataDensityPlotService clinicalDataDensityPlotService,
      ViolinPlotService violinPlotService,
      CustomDataService customDataService,
      CustomDataFilterUtil customDataFilterUtil) {
    this.studyViewService = studyViewService;
    this.basicDataBinner = basicDataBinner;
    this.clinicalDataBinner = clinicalDataBinner;
    this.clinicalDataDensityPlotService = clinicalDataDensityPlotService;
    this.violinPlotService = violinPlotService;
    this.customDataService = customDataService;
    this.customDataFilterUtil = customDataFilterUtil;
  }

  @Hidden
  @RequestMapping(
      value = "/filtered-samples/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(
      "hasPermission(#studyViewFilter, 'StudyViewFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<List<SampleDTO>> fetchFilteredSamples(
      @RequestParam(defaultValue = "false") Boolean negateFilters,
      @RequestBody(required = false) StudyViewFilter studyViewFilter) {
    return ResponseEntity.ok(
        SampleMapper.INSTANCE.toDtos(studyViewService.getFilteredSamples(studyViewFilter)));
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/mutated-genes/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(
      "hasPermission(#studyViewFilter, 'StudyViewFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<List<AlterationCountByGene>> fetchMutatedGenes(
      @RequestBody(required = false) StudyViewFilter studyViewFilter)
      throws StudyNotFoundException {
    return ResponseEntity.ok(studyViewService.getMutatedGenes(studyViewFilter));
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/molecular-profile-sample-counts/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch sample counts by study view filter")
  @PreAuthorize(
      "hasPermission(#studyViewFilter, 'StudyViewFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = GenomicDataCount.class))))
  public ResponseEntity<List<GenomicDataCount>> fetchMolecularProfileSampleCounts(
      @Parameter(required = true, description = "Study view filter")
          @Valid
          @RequestBody(required = false)
          StudyViewFilter studyViewFilter)
      throws StudyNotFoundException {
    return ResponseEntity.ok(studyViewService.getMolecularProfileSampleCounts(studyViewFilter));
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/cna-genes/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(
      "hasPermission(#studyViewFilter, 'StudyViewFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<List<CopyNumberCountByGene>> fetchCnaGenes(
      @RequestBody(required = false) StudyViewFilter studyViewFilter)
      throws StudyNotFoundException {
    return ResponseEntity.ok(studyViewService.getCnaGenes(studyViewFilter));
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/structuralvariant-genes/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch structural variant genes by study view filter")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array = @ArraySchema(schema = @Schema(implementation = AlterationCountByGene.class))))
  @PreAuthorize(
      "hasPermission(#studyViewFilter, 'StudyViewFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<List<AlterationCountByGene>> fetchStructuralVariantGenes(
      @Parameter(required = true, description = "Study view filter")
          @Valid
          @RequestBody(required = false)
          StudyViewFilter studyViewFilter)
      throws StudyNotFoundException {
    return ResponseEntity.ok(studyViewService.getStructuralVariantGenes(studyViewFilter));
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/clinical-data-counts/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(
      "hasPermission(#clinicalDataCountFilter, 'ClinicalDataCountFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<List<ClinicalDataCountItem>> fetchClinicalDataCounts(
      @RequestBody(required = false) ClinicalDataCountFilter clinicalDataCountFilter) {

    List<ClinicalDataFilter> attributes = clinicalDataCountFilter.getAttributes();
    StudyViewFilter studyViewFilter = clinicalDataCountFilter.getStudyViewFilter();

    if (attributes.size() == 1) {
      NewStudyViewFilterUtil.removeClinicalDataFilter(
          attributes.getFirst().getAttributeId(), studyViewFilter.getClinicalDataFilters());
    }
    List<ClinicalDataCountItem> result =
        studyViewService.getClinicalDataCounts(
            studyViewFilter, attributes.stream().map(ClinicalDataFilter::getAttributeId).toList());
    return ResponseEntity.ok(result);
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/sample-lists-counts/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch case list sample counts by study view filter")
  @PreAuthorize(
      "hasPermission(#studyViewFilter, 'StudyViewFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public List<CaseListDataCount> fetchCaseListCounts(
      @Parameter(required = true, description = "Study view filter")
          @Valid
          @RequestBody(required = false)
          StudyViewFilter studyViewFilter) {

    return studyViewService.getCaseListDataCounts(studyViewFilter);
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/clinical-data-bin-counts/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(
      "hasPermission(#clinicalDataBinCountFilter, 'DataBinCountFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<List<ClinicalDataBin>> fetchClinicalDataBinCounts(
      @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
      @RequestBody(required = false) ClinicalDataBinCountFilter clinicalDataBinCountFilter) {
    List<ClinicalDataBin> clinicalDataBins =
        clinicalDataBinner.fetchClinicalDataBinCounts(
            dataBinMethod, clinicalDataBinCountFilter, true);
    return new ResponseEntity<>(clinicalDataBins, HttpStatus.OK);
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/clinical-data-density-plot/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch clinical data density plot bins by study view filter")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = DensityPlotData.class)))
  @Validated
  @PreAuthorize(
      "hasPermission(#studyViewFilter, 'StudyViewFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<DensityPlotData> fetchClinicalDataDensityPlot(
      @Parameter(required = true, description = "Clinical Attribute ID of the X axis") @RequestParam
          String xAxisAttributeId,
      @Parameter(description = "Number of the bins in X axis") @RequestParam(defaultValue = "50")
          Integer xAxisBinCount,
      @Parameter(description = "Starting point of the X axis, if different than smallest value")
          @RequestParam(required = false)
          BigDecimal xAxisStart,
      @Parameter(description = "Starting point of the X axis, if different than largest value")
          @RequestParam(required = false)
          BigDecimal xAxisEnd,
      @Parameter(required = true, description = "Clinical Attribute ID of the Y axis") @RequestParam
          String yAxisAttributeId,
      @Parameter(description = "Number of the bins in Y axis") @RequestParam(defaultValue = "50")
          Integer yAxisBinCount,
      @Parameter(description = "Starting point of the Y axis, if different than smallest value")
          @RequestParam(required = false)
          BigDecimal yAxisStart,
      @Parameter(description = "Starting point of the Y axis, if different than largest value")
          @RequestParam(required = false)
          BigDecimal yAxisEnd,
      @Parameter(description = "Use log scale for X axis")
          @RequestParam(required = false, defaultValue = "false")
          Boolean xAxisLogScale,
      @Schema(defaultValue = "false")
          @Parameter(description = "Use log scale for Y axis")
          @RequestParam(required = false, defaultValue = "false")
          Boolean yAxisLogScale,
      @Parameter(required = true, description = "Study view filter") @RequestBody(required = false)
          StudyViewFilter studyViewFilter) {
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

    List<org.cbioportal.legacy.model.ClinicalData> combinedClinicalDataList =
        ClinicalDataUtil.convertToLegacyClinicalDataList(
            studyViewService.getClinicalDataForXyPlot(
                studyViewFilter, List.of(xAxisAttributeId, yAxisAttributeId), false));

    DensityPlotData result =
        clinicalDataDensityPlotService.getDensityPlotData(
            combinedClinicalDataList, densityPlotParameters, studyViewFilter);

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Hidden // should unhide when we remove legacy controller
  @PreAuthorize(
      "hasPermission(#studyViewFilter, 'StudyViewFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/clinical-data-violin-plots/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description =
          "Fetch violin plot curves per categorical clinical data value, filtered by study view filter")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = ClinicalViolinPlotData.class)))
  public ResponseEntity<ClinicalViolinPlotData> fetchClinicalDataViolinPlots(
      @Parameter(
              required = true,
              description = "Clinical Attribute ID of the categorical attribute")
          @RequestParam
          String categoricalAttributeId,
      @Parameter(required = true, description = "Clinical Attribute ID of the numerical attribute")
          @RequestParam
          String numericalAttributeId,
      @Parameter(
              description =
                  "Starting point of the violin plot axis, if different than smallest value")
          @RequestParam(required = false)
          BigDecimal axisStart,
      @Parameter(
              description =
                  "Ending point  of the violin plot axis, if different than largest value")
          @RequestParam(required = false)
          BigDecimal axisEnd,
      @Parameter(description = "Number of points in the curve")
          @RequestParam(required = false, defaultValue = "100")
          BigDecimal numCurvePoints,
      @Parameter(description = "Use log scale for the numerical attribute")
          @RequestParam(required = false, defaultValue = "false")
          Boolean logScale,
      @Parameter(description = "Sigma stepsize multiplier")
          @RequestParam(required = false, defaultValue = "1")
          BigDecimal sigmaMultiplier,
      @Parameter(required = true, description = "Study view filter")
          @Valid
          @RequestBody(required = false)
          StudyViewFilter studyViewFilter) {
    // fetch the samples by using the provided study view filter
    List<Sample> filteredSamples = studyViewService.getFilteredSamples(studyViewFilter);

    // remove the numerical clinical data filter from the study view filter.
    // this new modified filter is used to fetch sample and patient clinical data.
    // this is required to get the complete violin plot data.
    // filteredSamples reflects only the original unmodified study view filter.
    // we will need to fetch samples again to get the samples corresponding to this modified filter,
    // otherwise patient to sample mapping may be incomplete.
    if (studyViewFilter.getClinicalDataFilters() != null) {
      studyViewFilter.getClinicalDataFilters().stream()
          .filter(f -> f.getAttributeId().equals(numericalAttributeId))
          .findAny()
          .ifPresent(f -> studyViewFilter.getClinicalDataFilters().remove(f));
    }

    List<ClinicalData> combinedClinicalDataList =
        ClinicalDataUtil.convertToLegacyClinicalDataList(
            studyViewService.getClinicalDataForXyPlot(
                studyViewFilter,
                List.of(numericalAttributeId, categoricalAttributeId),
                true // filter out clinical data with empty attribute values due to Clickhouse
                // migration
                ));

    // Only mutation count can use log scale
    boolean useLogScale = logScale && numericalAttributeId.equals("MUTATION_COUNT");

    Set<Integer> sampleIdsSet = filteredSamples.stream().map(Sample::internalId).collect(toSet());

    ClinicalViolinPlotData result =
        violinPlotService.getClinicalViolinPlotData(
            combinedClinicalDataList,
            sampleIdsSet,
            axisStart,
            axisEnd,
            numCurvePoints,
            useLogScale,
            sigmaMultiplier,
            studyViewFilter);

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/genomic-data-counts/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch genomic data counts by GenomicDataCountFilter")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array = @ArraySchema(schema = @Schema(implementation = GenomicDataCountItem.class))))
  @PreAuthorize(
      "hasPermission(#genomicDataCountFilter, 'GenomicDataCountFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<List<GenomicDataCountItem>> fetchGenomicDataCounts(
      @Parameter(required = true, description = "Genomic data count filter")
          @Valid
          @RequestBody(required = false)
          GenomicDataCountFilter genomicDataCountFilter)
      throws StudyNotFoundException {
    List<GenomicDataFilter> genomicDataFilters = genomicDataCountFilter.getGenomicDataFilters();
    StudyViewFilter studyViewFilter = genomicDataCountFilter.getStudyViewFilter();
    // when there is only one filter, it means study view is doing a single chart filter operation
    // remove filter from studyViewFilter to return all data counts
    // the reason we do this is to make sure after chart get filtered, user can still see unselected
    // portion of the chart
    if (genomicDataFilters.size() == 1) {
      NewStudyViewFilterUtil.removeSelfFromGenomicDataFilter(
          genomicDataFilters.get(0).getHugoGeneSymbol(),
          genomicDataFilters.get(0).getProfileType(),
          studyViewFilter);
    }

    // This endpoint is CNA specific. The name choice of "genomic data" does not imply it support
    // other genomic data types
    List<GenomicDataCountItem> result =
        studyViewService.getCNACountsByGeneSpecific(studyViewFilter, genomicDataFilters);

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/generic-assay-data-counts/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch generic assay data counts by study view filter")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array =
                  @ArraySchema(schema = @Schema(implementation = GenericAssayDataCountItem.class))))
  @PreAuthorize(
      "hasPermission(#genericAssayDataCountFilter, 'GenericAssayDataCountFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<List<GenericAssayDataCountItem>> fetchGenericAssayDataCounts(
      @Parameter(required = true, description = "Generic assay data count filter")
          @Valid
          @RequestBody(required = false)
          GenericAssayDataCountFilter genericAssayDataCountFilter) {

    List<GenericAssayDataFilter> gaFilters =
        genericAssayDataCountFilter.getGenericAssayDataFilters();
    StudyViewFilter studyViewFilter = genericAssayDataCountFilter.getStudyViewFilter();
    // when there is only one filter, it means study view is doing a single chart filter operation
    // remove filter from studyViewFilter to return all data counts
    // the reason we do this is to make sure after chart get filtered, user can still see unselected
    // portion of the chart

    if (gaFilters.size() == 1) {
      NewStudyViewFilterUtil.removeSelfFromGenericAssayFilter(
          gaFilters.getFirst().getStableId(), studyViewFilter);
    }

    return ResponseEntity.ok(
        studyViewService.getGenericAssayDataCounts(studyViewFilter, gaFilters));
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/mutation-data-counts/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch mutation data counts by GenomicDataCountFilter")
  @PreAuthorize(
      "hasPermission(#genomicDataCountFilter, 'GenomicDataCountFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<List<GenomicDataCountItem>> fetchMutationDataCounts(
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          Projection projection,
      @Parameter(required = true, description = "Genomic data count filter")
          @Valid
          @RequestBody(required = false)
          GenomicDataCountFilter genomicDataCountFilter) {
    List<GenomicDataFilter> genomicDataFilters = genomicDataCountFilter.getGenomicDataFilters();
    StudyViewFilter studyViewFilter = genomicDataCountFilter.getStudyViewFilter();
    // when there is only one filter, it means study view is doing a single chart filter operation
    // remove filter from studyViewFilter to return all data counts
    // the reason we do this is to make sure after chart get filtered, user can still see unselected
    // portion of the chart
    if (genomicDataFilters.size() == 1 && projection == Projection.SUMMARY) {
      NewStudyViewFilterUtil.removeSelfFromMutationDataFilter(
          genomicDataFilters.get(0).getHugoGeneSymbol(),
          genomicDataFilters.get(0).getProfileType(),
          MutationOption.MUTATED,
          studyViewFilter);
    }

    List<GenomicDataCountItem> result =
        projection == Projection.SUMMARY
            ? studyViewService.getMutationCountsByGeneSpecific(studyViewFilter, genomicDataFilters)
            : studyViewService.getMutationTypeCountsByGeneSpecific(
                studyViewFilter, genomicDataFilters);

    return ResponseEntity.ok(result);
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/clinical-event-type-counts/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get Counts of Clinical Event Types by Study View Filter")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array =
                  @ArraySchema(schema = @Schema(implementation = ClinicalEventTypeCount.class))))
  @PreAuthorize(
      "hasPermission(#studyViewFilter, 'StudyViewFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<List<ClinicalEventTypeCount>> getClinicalEventTypeCounts(
      @Parameter(required = true, description = "Study view filter")
          @Valid
          @RequestBody(required = false)
          StudyViewFilter studyViewFilter) {
    return ResponseEntity.ok(studyViewService.getClinicalEventTypeCounts(studyViewFilter));
  }

  @RequestMapping(
      value = "/treatments/patient-counts/fetch",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get all patient level treatments")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = PatientTreatmentReport.class)))
  @PreAuthorize(
      "hasPermission(#studyViewFilter, 'StudyViewFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<PatientTreatmentReport> fetchPatientTreatmentCounts(
      @Parameter(required = false)
          @RequestParam(name = "tier", required = false, defaultValue = "Agent")
          ClinicalEventKeyCode tier,
      @Parameter(required = true, description = "Study view filter")
          @Valid
          @RequestBody(required = false)
          StudyViewFilter studyViewFilter) {
    return ResponseEntity.ok(studyViewService.getPatientTreatmentReport(studyViewFilter));
  }

  @RequestMapping(
      value = "/treatments/sample-counts/fetch",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = SampleTreatmentReport.class)))
  @PreAuthorize(
      "hasPermission(#studyViewFilter, 'StudyViewFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<SampleTreatmentReport> fetchSampleTreatmentCounts(
      @Parameter(required = false)
          @RequestParam(name = "tier", required = false, defaultValue = "Agent")
          ClinicalEventKeyCode tier,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          ProjectionType projection,
      @Parameter(required = true, description = "Study view filter")
          @Valid
          @RequestBody(required = false)
          StudyViewFilter studyViewFilter) {
    return ResponseEntity.ok(
        studyViewService.getSampleTreatmentReport(studyViewFilter, projection));
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/custom-data-counts/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch custom data counts by study view filter")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array = @ArraySchema(schema = @Schema(implementation = ClinicalDataCountItem.class))))
  @PreAuthorize(
      "hasPermission(#clinicalDataCountFilter, 'DataCountFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<List<ClinicalDataCountItem>> fetchCustomDataCounts(
      @Parameter(required = true, description = "Custom data count filter")
          @Valid
          @RequestBody(required = false)
          ClinicalDataCountFilter clinicalDataCountFilter) {

    List<ClinicalDataFilter> attributes = clinicalDataCountFilter.getAttributes();
    StudyViewFilter studyViewFilter = clinicalDataCountFilter.getStudyViewFilter();
    if (attributes.size() == 1) {
      NewStudyViewFilterUtil.removeClinicalDataFilter(
          attributes.getFirst().getAttributeId(), studyViewFilter.getCustomDataFilters());
    }

    List<SampleIdentifier> filteredSampleIdentifiers =
        studyViewService.getFilteredSamples(studyViewFilter).stream()
            .map(
                sample ->
                    NewStudyViewFilterUtil.buildSampleIdentifier(
                        sample.cancerStudyIdentifier(), sample.stableId()))
            .toList();

    if (filteredSampleIdentifiers.isEmpty()) {
      return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
    }

    final List<String> attributeIds =
        attributes.stream().map(ClinicalDataFilter::getAttributeId).toList();
    Map<String, CustomDataSession> customDataSessionsMap =
        customDataService.getCustomDataSessions(attributeIds);

    List<ClinicalDataCountItem> result =
        customDataFilterUtil.getCustomDataCounts(filteredSampleIdentifiers, customDataSessionsMap);

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/custom-data-bin-counts/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch custom data bin counts by study view filter")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalDataBin.class))))
  @PreAuthorize(
      "hasPermission(#clinicalDataBinCountFilter, 'DataBinCountFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<List<ClinicalDataBin>> fetchCustomDataBinCounts(
      @Parameter(description = "Method for data binning") @RequestParam(defaultValue = "DYNAMIC")
          DataBinMethod dataBinMethod,
      @Parameter(required = true, description = "Clinical data bin count filter")
          @Valid
          @RequestBody(required = false)
          ClinicalDataBinCountFilter clinicalDataBinCountFilter) {
    List<ClinicalDataBin> customDataBins =
        basicDataBinner.getDataBins(dataBinMethod, clinicalDataBinCountFilter, true);
    return ResponseEntity.ok(customDataBins);
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/genomic-data-bin-counts/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = GenomicDataBin.class))))
  @PreAuthorize(
      "hasPermission(#genomicDataBinCountFilter, 'DataBinCountFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<List<GenomicDataBin>> fetchGenomicDataBinCounts(
      @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
      @RequestBody(required = false) GenomicDataBinCountFilter genomicDataBinCountFilter) {
    List<GenomicDataBin> genomicDataBins =
        basicDataBinner.getDataBins(dataBinMethod, genomicDataBinCountFilter, true);
    return ResponseEntity.ok(genomicDataBins);
  }

  @Hidden // should unhide when we remove legacy controller
  @RequestMapping(
      value = "/generic-assay-data-bin-counts/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array = @ArraySchema(schema = @Schema(implementation = GenericAssayDataBin.class))))
  @PreAuthorize(
      "hasPermission(#genericAssayDataBinCountFilter, 'DataBinCountFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<List<GenericAssayDataBin>> fetchGenericAssayDataBinCounts(
      @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
      @RequestBody(required = false)
          GenericAssayDataBinCountFilter genericAssayDataBinCountFilter) {
    List<GenericAssayDataBin> genericAssayDataBins =
        basicDataBinner.getDataBins(dataBinMethod, genericAssayDataBinCountFilter, true);
    return ResponseEntity.ok(genericAssayDataBins);
  }
}
