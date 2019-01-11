package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.cbioportal.model.*;
import org.cbioportal.model.ClinicalDataCountItem.ClinicalDataType;
import org.cbioportal.service.*;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.DataBinner;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.math.NumberUtils;

@InternalApi
@RestController
@Validated
@Api(tags = "Study View", description = " ")
public class StudyViewController {

    @Autowired
    private StudyViewFilterApplier studyViewFilterApplier;
    @Autowired
    private ClinicalDataService clinicalDataService;
    @Autowired
    private MutationService mutationService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private DiscreteCopyNumberService discreteCopyNumberService;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private GenePanelService genePanelService;
    @Autowired
    private SignificantlyMutatedGeneService significantlyMutatedGeneService;
    @Autowired
    private SignificantCopyNumberRegionService significantCopyNumberRegionService;
    @Autowired
    private DataBinner dataBinner;
    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;

    @RequestMapping(value = "/clinical-data-counts/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch clinical data counts by study view filter")
    public ResponseEntity<List<ClinicalDataCountItem>> fetchClinicalDataCounts(
        @ApiParam(required = true, value = "Clinical data count filter")
        @Valid @RequestBody ClinicalDataCountFilter clinicalDataCountFilter) {

        List<ClinicalDataFilter> attributes = clinicalDataCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = clinicalDataCountFilter.getStudyViewFilter();
        if (attributes.size() == 1) {
            studyViewFilterUtil.removeSelfFromFilter(attributes.get(0).getAttributeId(), studyViewFilter);
        }
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);

        List<ClinicalDataCountItem> combinedResult = new ArrayList<>();
        if (filteredSampleIdentifiers.isEmpty()) {
            return new ResponseEntity<>(combinedResult, HttpStatus.OK);
        }
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
        List<ClinicalDataCountItem> resultForSampleAttributes = clinicalDataService.fetchClinicalDataCounts(
            studyIds, sampleIds, attributes.stream().filter(a -> a.getClinicalDataType().equals(ClinicalDataType.SAMPLE))
            .map(a -> a.getAttributeId()).collect(Collectors.toList()), ClinicalDataType.SAMPLE);
        List<ClinicalDataCountItem> resultForPatientAttributes = clinicalDataService.fetchClinicalDataCounts(studyIds, sampleIds, 
        attributes.stream().filter(a -> a.getClinicalDataType().equals(ClinicalDataType.PATIENT))
            .map(a -> a.getAttributeId()).collect(Collectors.toList()), ClinicalDataType.PATIENT);
        combinedResult.addAll(resultForSampleAttributes);
        combinedResult.addAll(resultForPatientAttributes);
        return new ResponseEntity<>(combinedResult, HttpStatus.OK);
    }

    @RequestMapping(value = "/clinical-data-bin-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch clinical data bin counts by study view filter")
    public ResponseEntity<List<DataBin>> fetchClinicalDataBinCounts(
        @ApiParam("Method for data binning")
        @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
        @ApiParam(required = true, value = "Clinical data bin count filter")
        @Valid @RequestBody ClinicalDataBinCountFilter clinicalDataBinCountFilter) {

        List<ClinicalDataBinFilter> attributes = clinicalDataBinCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = clinicalDataBinCountFilter.getStudyViewFilter();
        
        if (attributes.size() == 1) {
            studyViewFilterUtil.removeSelfFromFilter(attributes.get(0).getAttributeId(), studyViewFilter);
        }
        
        List<DataBin> clinicalDataBins = null;
        List<String> filteredSampleIds = new ArrayList<>();
        List<String> filteredPatientIds = new ArrayList<>();
        List<ClinicalData> filteredClinicalData = fetchClinicalData(attributes, studyViewFilter, filteredSampleIds, filteredPatientIds);
        Map<String, List<ClinicalData>> filteredClinicalDataByAttributeId = 
            filteredClinicalData.stream().collect(Collectors.groupingBy(ClinicalData::getAttrId));
        
        if (dataBinMethod == DataBinMethod.STATIC) 
        {
            StudyViewFilter filter = studyViewFilter == null ? null : new StudyViewFilter();
            
            if (filter != null) {
                filter.setStudyIds(studyViewFilter.getStudyIds());
                filter.setSampleIdentifiers(studyViewFilter.getSampleIdentifiers());
            }
            
            List<String> unfilteredSampleIds = new ArrayList<>();
            List<String> unfilteredPatientIds = new ArrayList<>();
            List<ClinicalData> unfilteredClinicalData = fetchClinicalData(attributes, filter, unfilteredSampleIds, unfilteredPatientIds);
            Map<String, List<ClinicalData>> unfilteredClinicalDataByAttributeId =
                unfilteredClinicalData.stream().collect(Collectors.groupingBy(ClinicalData::getAttrId));

            if (!unfilteredClinicalData.isEmpty()) {
                clinicalDataBins = new ArrayList<>();
                for (ClinicalDataBinFilter attribute: attributes) {
                    List<String> filteredIds = attribute.getClinicalDataType() == ClinicalDataType.PATIENT ?
                        filteredPatientIds : filteredSampleIds;
                    List<String> unfilteredIds = attribute.getClinicalDataType() == ClinicalDataType.PATIENT ? 
                        unfilteredPatientIds : unfilteredSampleIds;
                    
                    List<DataBin> dataBins = dataBinner.calculateClinicalDataBins(
                        attribute,
                        filteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(), Collections.emptyList()),
                        unfilteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(), Collections.emptyList()),
                        filteredIds,
                        unfilteredIds);
                    dataBins.forEach(dataBin -> dataBin.setClinicalDataType(attribute.getClinicalDataType()));
                    clinicalDataBins.addAll(dataBins);
                }
            }
        }
        else { // dataBinMethod == DataBinMethod.DYNAMIC
            if (!filteredClinicalData.isEmpty()) {
                clinicalDataBins = new ArrayList<>();
                for (ClinicalDataBinFilter attribute: attributes) {
                    List<String> filteredIds = attribute.getClinicalDataType() == ClinicalDataType.PATIENT ?
                        filteredPatientIds : filteredSampleIds;
                    
                    List<DataBin> dataBins = dataBinner.calculateClinicalDataBins(
                        attribute,
                        filteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(), Collections.emptyList()),
                        filteredIds);
                    dataBins.forEach(dataBin -> dataBin.setClinicalDataType(attribute.getClinicalDataType()));
                    clinicalDataBins.addAll(dataBins);
                }
            }
        }
        
        return new ResponseEntity<>(clinicalDataBins, HttpStatus.OK);
    }

    @RequestMapping(value = "/mutated-genes/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutated genes by study view filter")
    public ResponseEntity<List<MutationCountByGene>> fetchMutatedGenes(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) throws StudyNotFoundException {

        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);
        List<MutationCountByGene> result = new ArrayList<>();
        if (!filteredSampleIdentifiers.isEmpty()) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
            result = mutationService.getSampleCountInMultipleMolecularProfiles(molecularProfileService
                .getFirstMutationProfileIds(studyIds, sampleIds), sampleIds, null, true);
            result.sort((a, b) -> b.getCountByEntity() - a.getCountByEntity());
            List<String> distinctStudyIds = studyIds.stream().distinct().collect(Collectors.toList());
            if (distinctStudyIds.size() == 1) {
                Map<Integer, MutSig> mutSigMap = significantlyMutatedGeneService.getSignificantlyMutatedGenes(
                    distinctStudyIds.get(0), Projection.SUMMARY.name(), null, null, null, null).stream().collect(
                        Collectors.toMap(MutSig::getEntrezGeneId, Function.identity()));
                result.forEach(r -> {
                    if (mutSigMap.containsKey(r.getEntrezGeneId())) {
                        r.setqValue(mutSigMap.get(r.getEntrezGeneId()).getqValue());
                    }
                });
            }
        }
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/cna-genes/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch CNA genes by study view filter")
    public ResponseEntity<List<CopyNumberCountByGene>> fetchCNAGenes(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) throws StudyNotFoundException {

        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);
        List<CopyNumberCountByGene> result = new ArrayList<>();
        if (!filteredSampleIdentifiers.isEmpty()) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
            result = discreteCopyNumberService.getSampleCountInMultipleMolecularProfiles(molecularProfileService
                .getFirstDiscreteCNAProfileIds(studyIds, sampleIds), sampleIds, null, Arrays.asList(-2, 2), true);
            result.sort((a, b) -> b.getCountByEntity() - a.getCountByEntity());
            List<String> distinctStudyIds = studyIds.stream().distinct().collect(Collectors.toList());
            if (distinctStudyIds.size() == 1) {
                List<Gistic> gisticList = significantCopyNumberRegionService.getSignificantCopyNumberRegions(
                    distinctStudyIds.get(0), Projection.SUMMARY.name(), null, null, null, null);
                MultiKeyMap gisticMap = new MultiKeyMap();
                gisticList.forEach(g -> g.getGenes().forEach(gene -> {
                    Gistic gistic = (Gistic) gisticMap.get(gene.getEntrezGeneId(), g.getAmp());
                    if (gistic == null || g.getqValue().compareTo(gistic.getqValue()) < 0) {
                        gisticMap.put(gene.getEntrezGeneId(), g.getAmp(), g);
                    }
                }));
                result.forEach(r -> {
                    if (gisticMap.containsKey(r.getEntrezGeneId(), r.getAlteration().equals(2))) {
                        r.setqValue(((Gistic) gisticMap.get(r.getEntrezGeneId(), r.getAlteration().equals(2))).getqValue());
                    }
                });
            }
        }
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/filtered-samples/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch sample IDs by study view filter")
    public ResponseEntity<List<Sample>> fetchFilteredSamples(
        @ApiParam("Whether to negate the study view filters")
        @RequestParam(defaultValue = "false") Boolean negateFilters,
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) {
        
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        
        studyViewFilterUtil.extractStudyAndSampleIds(
            studyViewFilterApplier.apply(studyViewFilter, negateFilters), studyIds, sampleIds);
        
        List<Sample> result = new ArrayList<>();
        if (!sampleIds.isEmpty()) {
            result = sampleService.fetchSamples(studyIds, sampleIds, Projection.ID.name());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/sample-counts/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch sample counts by study view filter")
    public ResponseEntity<MolecularProfileSampleCount> fetchMolecularProfileSampleCounts(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) {
        
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(studyViewFilterApplier.apply(studyViewFilter), studyIds, sampleIds);
        MolecularProfileSampleCount molecularProfileSampleCount = new MolecularProfileSampleCount();
        if (sampleIds.isEmpty()) {
            molecularProfileSampleCount.setNumberOfMutationProfiledSamples(0);
            molecularProfileSampleCount.setNumberOfMutationUnprofiledSamples(0);
            molecularProfileSampleCount.setNumberOfCNAProfiledSamples(0);
            molecularProfileSampleCount.setNumberOfCNAUnprofiledSamples(0);
        } else {
            int sampleCount = sampleIds.size();
            List<String> mutationSampleIds = new ArrayList<>(sampleIds);
            List<String> firstMutationProfileIds = molecularProfileService.getFirstMutationProfileIds(studyIds, mutationSampleIds);
            if (!firstMutationProfileIds.isEmpty()) {
                molecularProfileSampleCount.setNumberOfMutationProfiledSamples(Math.toIntExact(genePanelService
                    .fetchGenePanelDataInMultipleMolecularProfiles(firstMutationProfileIds, mutationSampleIds).stream().filter(
                        g -> g.getProfiled()).count()));
                molecularProfileSampleCount.setNumberOfMutationUnprofiledSamples(sampleCount - 
                    molecularProfileSampleCount.getNumberOfMutationProfiledSamples());
            }
            List<String> cnaSampleIds = new ArrayList<>(sampleIds);
            List<String> firstDiscreteCNAProfileIds = molecularProfileService.getFirstDiscreteCNAProfileIds(studyIds, cnaSampleIds);
            if (!firstDiscreteCNAProfileIds.isEmpty()) {
                molecularProfileSampleCount.setNumberOfCNAProfiledSamples(Math.toIntExact(genePanelService
                    .fetchGenePanelDataInMultipleMolecularProfiles(firstDiscreteCNAProfileIds, cnaSampleIds).stream().filter(
                        g -> g.getProfiled()).count()));
                molecularProfileSampleCount.setNumberOfCNAUnprofiledSamples(sampleCount - 
                    molecularProfileSampleCount.getNumberOfCNAProfiledSamples());
            }
        }
        return new ResponseEntity<>(molecularProfileSampleCount, HttpStatus.OK);
    }

    @RequestMapping(value = "/clinical-data-density-plot/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch clinical data density plot bins by study view filter")
    public ResponseEntity<List<DensityPlotBin>> fetchClinicalDataDensityPlot(
        @ApiParam(required = true, value = "Clinical Attribute ID of the X axis")
        @RequestParam String xAxisAttributeId,
        @ApiParam("Number of the bins in X axis")
        @RequestParam(defaultValue = "50") Integer xAxisBinCount,
        @ApiParam("Starting point of the X axis, if different than smallest value")
        @RequestParam(required = false) BigDecimal xAxisStart,
        @ApiParam("Starting point of the X axis, if different than largest value")
        @RequestParam(required = false) BigDecimal xAxisEnd,
        @ApiParam(required = true, value = "Clinical Attribute ID of the Y axis")
        @RequestParam String yAxisAttributeId,
        @ApiParam("Number of the bins in Y axis")
        @RequestParam(defaultValue = "50") Integer yAxisBinCount,
        @ApiParam("Starting point of the Y axis, if different than smallest value")
        @RequestParam(required = false) BigDecimal yAxisStart,
        @ApiParam("Starting point of the Y axis, if different than largest value")
        @RequestParam(required = false) BigDecimal yAxisEnd,
        @ApiParam(required = true, value = "Clinical data type of both attributes")
        @RequestParam ClinicalDataType clinicalDataType,
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) {
        
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(studyViewFilterApplier.apply(studyViewFilter), studyIds, sampleIds);
        List<DensityPlotBin> result = new ArrayList<>();
        if (sampleIds.isEmpty()) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        
        List<ClinicalData> clinicalDataList = clinicalDataService.fetchClinicalData(studyIds, sampleIds, 
            Arrays.asList(xAxisAttributeId, yAxisAttributeId), clinicalDataType.name(), Projection.SUMMARY.name());

        Map<String, Map<String, List<ClinicalData>>> clinicalDataMap;
        if (clinicalDataType == ClinicalDataType.SAMPLE) {
            clinicalDataMap = clinicalDataList.stream().collect(Collectors.groupingBy(ClinicalData::getSampleId, 
                Collectors.groupingBy(ClinicalData::getStudyId)));
        } else {
            clinicalDataMap = clinicalDataList.stream().collect(Collectors.groupingBy(ClinicalData::getPatientId, 
                Collectors.groupingBy(ClinicalData::getStudyId)));
        }
        List<ClinicalData> filteredClinicalDataList = new ArrayList<>();
        clinicalDataMap.forEach((k, v) -> v.forEach((m, n) -> {
            if (n.size() == 2 && NumberUtils.isNumber(n.get(0).getAttrValue()) && NumberUtils.isNumber(n.get(1).getAttrValue())) {
                filteredClinicalDataList.addAll(n);
            }
        }));
        if (filteredClinicalDataList.isEmpty()) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        Map<Boolean, List<ClinicalData>> partition = filteredClinicalDataList.stream().collect(
            Collectors.partitioningBy(c -> c.getAttrId().equals(xAxisAttributeId)));
        double[] xValues = partition.get(true).stream().mapToDouble(c -> Double.parseDouble(c.getAttrValue())).toArray();
        double[] yValues = partition.get(false).stream().mapToDouble(c -> Double.parseDouble(c.getAttrValue())).toArray();
        double[] xValuesCopy = Arrays.copyOf(xValues, xValues.length);
        double[] yValuesCopy = Arrays.copyOf(yValues, yValues.length);
        Arrays.sort(xValuesCopy);
        Arrays.sort(yValuesCopy);

        double xAxisStartValue = xAxisStart == null ? xValuesCopy[0] : xAxisStart.doubleValue();
        double xAxisEndValue = xAxisEnd == null ? xValuesCopy[xValuesCopy.length - 1] : xAxisEnd.doubleValue();
        double yAxisStartValue = yAxisStart == null ? yValuesCopy[0] : yAxisStart.doubleValue();
        double yAxisEndValue = yAxisEnd == null ? yValuesCopy[yValuesCopy.length - 1] : yAxisEnd.doubleValue();
        double xAxisBinInterval = (xAxisEndValue - xAxisStartValue) / xAxisBinCount;
        double yAxisBinInterval = (yAxisEndValue - yAxisStartValue) / yAxisBinCount;
        for (int i = 0; i < xAxisBinCount; i++) {
            for (int j = 0; j < yAxisBinCount; j++) {
                DensityPlotBin densityPlotBin = new DensityPlotBin();
                densityPlotBin.setBinX(new BigDecimal(xAxisStartValue + (i * xAxisBinInterval)));
                densityPlotBin.setBinY(new BigDecimal(yAxisStartValue + (j * yAxisBinInterval)));
                densityPlotBin.setCount(0);
                result.add(densityPlotBin);
            }
        }

        for (int i = 0; i < xValues.length; i++) {
            double xValue = xValues[i];
            double yValue = yValues[i];
            int xBinIndex = (int) ((xValue - xAxisStartValue) / xAxisBinInterval);
            int yBinIndex = (int) ((yValue - yAxisStartValue) / yAxisBinInterval);
            int index = (int) (((xBinIndex - (xBinIndex == xAxisBinCount ? 1 : 0)) * yAxisBinCount) +
                (yBinIndex - (yBinIndex == yAxisBinCount ? 1 : 0)));
            DensityPlotBin densityPlotBin = result.get(index);
            densityPlotBin.setCount(densityPlotBin.getCount() + 1);
            BigDecimal xValueBigDecimal = new BigDecimal(xValue);
            BigDecimal yValueBigDecimal = new BigDecimal(yValue);
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

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    private List<ClinicalData> fetchClinicalData(List<String> attributeIds, 
                                                 ClinicalDataType clinicalDataType, 
                                                 StudyViewFilter studyViewFilter,
                                                 List<String> ids)
    {
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);

        if (filteredSampleIdentifiers.isEmpty()) {
            return null;
        }

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        
        ids.clear();

        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);

        if (clinicalDataType == ClinicalDataType.SAMPLE) {
            ids.addAll(sampleIds);
        }
        else {
            List<Patient> patients = patientService.getPatientsOfSamples(studyIds, sampleIds);
            ids.addAll(patients.stream().map(Patient::getStableId).collect(Collectors.toList()));

            // we need to regenerate study ids for the patients	
            studyIds.clear();
            patients.forEach(p -> studyIds.add(p.getCancerStudyIdentifier()));
        }
        
        return clinicalDataService.fetchClinicalData(
            studyIds, ids, attributeIds, clinicalDataType.name(), Projection.SUMMARY.name());
    }
    
    private List<ClinicalData> fetchClinicalData(List<ClinicalDataBinFilter> attributes, 
                                                 StudyViewFilter studyViewFilter,
                                                 List<String> sampleIds,
                                                 List<String> patientIds)
    {
        List<String> filteredIds = new ArrayList<>();
        
        List<String> sampleAttributes = attributes.stream()
            .filter(a -> a.getClinicalDataType().equals(ClinicalDataType.SAMPLE))
            .map(ClinicalDataBinFilter::getAttributeId)
            .collect(Collectors.toList());
        List<ClinicalData> filteredClinicalDataForSamples = null;
        
        if (sampleAttributes.size() > 0) {
            filteredClinicalDataForSamples = fetchClinicalData(sampleAttributes, ClinicalDataType.SAMPLE, studyViewFilter, filteredIds);
            sampleIds.addAll(filteredIds);
        }
        
        List<String> patientAttributes = attributes.stream()
            .filter(a -> a.getClinicalDataType().equals(ClinicalDataType.PATIENT))
            .map(ClinicalDataBinFilter::getAttributeId)
            .collect(Collectors.toList());
        List<ClinicalData> filteredClinicalDataForPatients = null;

        if (patientAttributes.size() > 0) {
            filteredClinicalDataForPatients = fetchClinicalData(patientAttributes, ClinicalDataType.PATIENT, studyViewFilter, filteredIds);
            patientIds.addAll(filteredIds);
        }
        
        List<ClinicalData> combinedResult = new ArrayList<>();
        
        if (filteredClinicalDataForSamples != null) {
            combinedResult.addAll(filteredClinicalDataForSamples);
        }
        
        if (filteredClinicalDataForPatients != null) {
            combinedResult.addAll(filteredClinicalDataForPatients);
        }
        
        return combinedResult;
    }
}
