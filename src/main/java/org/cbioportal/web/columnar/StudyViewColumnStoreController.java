package org.cbioportal.web.columnar;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.DensityPlotBin;
import org.cbioportal.model.DensityPlotData;
import org.cbioportal.model.Patient;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.mybatisclickhouse.StudyViewMapper;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.service.StudyViewService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.impl.ClinicalAttributeServiceImpl;
import org.cbioportal.service.impl.PatientServiceImpl;
import org.cbioportal.service.impl.SampleServiceImpl;
import org.cbioportal.service.util.ClinicalAttributeUtil;
import org.cbioportal.web.StudyViewController;
import org.cbioportal.web.columnar.util.NewStudyViewFilterUtil;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.ClinicalDataBinCountFilter;
import org.cbioportal.web.parameter.ClinicalDataCountFilter;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.DataBinMethod;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.ClinicalDataBinUtil;
import org.cbioportal.web.util.ClinicalDataFetcher;
import org.cbioportal.web.util.StudyViewFilterApplier;
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
import java.util.Map;
import java.util.stream.Collectors;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
public class StudyViewColumnStoreController {
    
    private final StudyViewColumnarService studyViewColumnarService;
    private final ClinicalDataBinner clinicalDataBinner;
    private final StudyViewService studyViewService;
    @Autowired
    private final ClinicalAttributeService clinicalAttributeService;
    @Autowired
    private ClinicalAttributeUtil clinicalAttributeUtil;
    @Autowired
    private PatientServiceImpl patientService;
    @Autowired
    private StudyViewFilterApplier studyViewFilterApplier;
    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;
    @Autowired
    private SampleServiceImpl sampleService;
    @Autowired
    private ClinicalDataFetcher clinicalDataFetcher;
    
    @Autowired
    public StudyViewColumnStoreController(StudyViewColumnarService studyViewColumnarService, StudyViewService studyViewService, ClinicalDataBinner clinicalDataBinner, ClinicalAttributeService clinicalAttributeService) {
        this.studyViewColumnarService = studyViewColumnarService;
        this.studyViewService = studyViewService;
        this.clinicalDataBinner = clinicalDataBinner;
        this.clinicalAttributeService = clinicalAttributeService;
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

        DensityPlotData result = new DensityPlotData();
        result.setBins(new ArrayList<>());
        
        List<String> xyAttributeId = new ArrayList<>(Arrays.asList(xAxisAttributeId, yAxisAttributeId));
        List<ClinicalData> sampleClinicalDataList = studyViewColumnarService.getSampleClinicalData(interceptedStudyViewFilter, xyAttributeId);

        Map<String, List<ClinicalData>> smallerClinicalDataMap = sampleClinicalDataList.stream().collect(Collectors.groupingBy(ClinicalData::getSampleId));
        List<ClinicalData> filteredClinicalDataList = new ArrayList<>();
        smallerClinicalDataMap.forEach((sampleId, clinDataList) -> {
            if (clinDataList.size() == 2 &&
                NumberUtils.isCreatable(clinDataList.get(0).getAttrValue()) &&
                NumberUtils.isCreatable(clinDataList.get(1).getAttrValue())) {
                filteredClinicalDataList.addAll(clinDataList);
            }
        });
        
        if (filteredClinicalDataList.isEmpty()) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        Map<Boolean, List<ClinicalData>> partition = filteredClinicalDataList.stream().collect(
            Collectors.partitioningBy(c -> c.getAttrId().equals(xAxisAttributeId)));

        boolean useXLogScale = xAxisLogScale && StudyViewColumnStoreController.isLogScalePossibleForAttribute(xAxisAttributeId);
        boolean useYLogScale = yAxisLogScale && StudyViewColumnStoreController.isLogScalePossibleForAttribute(yAxisAttributeId);

        double[] xValues = partition.get(true).stream().mapToDouble(
            useXLogScale ? StudyViewColumnStoreController::parseValueLog : StudyViewColumnStoreController::parseValueLinear
        ).toArray();
        double[] yValues = partition.get(false).stream().mapToDouble(
            useYLogScale ? StudyViewColumnStoreController::parseValueLog : StudyViewColumnStoreController::parseValueLinear
        ).toArray();
        double[] xValuesCopy = Arrays.copyOf(xValues, xValues.length);
        double[] yValuesCopy = Arrays.copyOf(yValues, yValues.length); // Why copy these?
        Arrays.sort(xValuesCopy);
        Arrays.sort(yValuesCopy);

        double xAxisStartValue = xAxisStart == null ? xValuesCopy[0] :
            (useXLogScale ? StudyViewColumnStoreController.logScale(xAxisStart.doubleValue()) : xAxisStart.doubleValue());
        double xAxisEndValue = xAxisEnd == null ? xValuesCopy[xValuesCopy.length - 1] :
            (useXLogScale ? StudyViewColumnStoreController.logScale(xAxisEnd.doubleValue()) : xAxisEnd.doubleValue());
        double yAxisStartValue = yAxisStart == null ? yValuesCopy[0] :
            (useYLogScale ? StudyViewColumnStoreController.logScale(yAxisStart.doubleValue()) : yAxisStart.doubleValue());
        double yAxisEndValue = yAxisEnd == null ? yValuesCopy[yValuesCopy.length - 1] :
            (useYLogScale ? StudyViewColumnStoreController.logScale(yAxisEnd.doubleValue()) : yAxisEnd.doubleValue());
        double xAxisBinInterval = (xAxisEndValue - xAxisStartValue) / xAxisBinCount;
        double yAxisBinInterval = (yAxisEndValue - yAxisStartValue) / yAxisBinCount;
        List<DensityPlotBin> bins = result.getBins();
        for (int i = 0; i < xAxisBinCount; i++) {
            for (int j = 0; j < yAxisBinCount; j++) {
                DensityPlotBin densityPlotBin = new DensityPlotBin();
                densityPlotBin.setBinX(BigDecimal.valueOf(xAxisStartValue + (i * xAxisBinInterval)));
                densityPlotBin.setBinY(BigDecimal.valueOf(yAxisStartValue + (j * yAxisBinInterval)));
                densityPlotBin.setCount(0);
                bins.add(densityPlotBin);
            }
        }

        for (int i = 0; i < xValues.length; i++) {
            double xValue = xValues[i];
            double yValue = yValues[i];
            int xBinIndex = (int) ((xValue - xAxisStartValue) / xAxisBinInterval);
            int yBinIndex = (int) ((yValue - yAxisStartValue) / yAxisBinInterval);
            int index = (int) (((xBinIndex - (xBinIndex == xAxisBinCount ? 1 : 0)) * yAxisBinCount) +
                (yBinIndex - (yBinIndex == yAxisBinCount ? 1 : 0)));
            DensityPlotBin densityPlotBin = bins.get(index);
            densityPlotBin.setCount(densityPlotBin.getCount() + 1);
            BigDecimal xValueBigDecimal = BigDecimal.valueOf(xValue);
            BigDecimal yValueBigDecimal = BigDecimal.valueOf(yValue);
            if (densityPlotBin.getMinX() != null) {
                if (densityPlotBin.getMinX().compareTo(xValueBigDecimal) > 0) {
                    densityPlotBin.setMinX(xValueBigDecimal);
                }
            } else {
                densityPlotBin.setMinX(xValueBigDecimal);
            }
            if (densityPlotBin.getMaxX() != null) {
                if (densityPlotBin.getMaxX().compareTo(xValueBigDecimal) < 0) {
                    densityPlotBin.setMaxX(xValueBigDecimal);
                }
            } else {
                densityPlotBin.setMaxX(xValueBigDecimal);
            }
            if (densityPlotBin.getMinY() != null) {
                if (densityPlotBin.getMinY().compareTo(yValueBigDecimal) > 0) {
                    densityPlotBin.setMinY(yValueBigDecimal);
                }
            } else {
                densityPlotBin.setMinY(yValueBigDecimal);
            }
            if (densityPlotBin.getMaxY() != null) {
                if (densityPlotBin.getMaxY().compareTo(yValueBigDecimal) < 0) {
                    densityPlotBin.setMaxY(yValueBigDecimal);
                }
            } else {
                densityPlotBin.setMaxY(yValueBigDecimal);
            }
        }

        if (xValues.length > 1) {
            // need at least 2 entries in each to compute correlation
            result.setPearsonCorr(new PearsonsCorrelation().correlation(xValues, yValues));
            result.setSpearmanCorr(new SpearmansCorrelation().correlation(xValues, yValues));
        } else {
            // if less than 1 entry, just set 0 correlation
            result.setSpearmanCorr(0.0);
            result.setPearsonCorr(0.0);
        }

        // filter out empty bins
        result.setBins(result.getBins().stream().filter((bin)->(bin.getCount() > 0)).collect(Collectors.toList()));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private static boolean isLogScalePossibleForAttribute(String clinicalAttributeId) {
        return clinicalAttributeId.equals("MUTATION_COUNT");
    }

    private static double logScale(double val) {
        return Math.log(1+val);
    }

    private static double parseValueLog(ClinicalData c) {
        return StudyViewColumnStoreController.logScale(Double.parseDouble(c.getAttrValue()));
    }

    private static double parseValueLinear(ClinicalData c) {
        return Double.parseDouble(c.getAttrValue());
    }
}
