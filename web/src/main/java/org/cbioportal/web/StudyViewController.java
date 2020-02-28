package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.cbioportal.model.*;
import org.cbioportal.service.*;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.util.ClinicalAttributeUtil;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.DataBinner;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.apache.commons.collections.CollectionUtils;
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
    @Autowired
    private ClinicalAttributeService clinicalAttributeService;
    @Autowired
    private ClinicalAttributeUtil clinicalAttributeUtil;
    @Autowired
    private TreatmentService treatmentService;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/clinical-data-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch clinical data counts by study view filter")
    public ResponseEntity<List<ClinicalDataCountItem>> fetchClinicalDataCounts(
        @ApiParam(required = true, value = "Clinical data count filter")
        @Valid @RequestBody(required = false)  ClinicalDataCountFilter clinicalDataCountFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedClinicalDataCountFilter") ClinicalDataCountFilter interceptedClinicalDataCountFilter) {

        List<ClinicalDataFilter> attributes = interceptedClinicalDataCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = interceptedClinicalDataCountFilter.getStudyViewFilter();
        if (attributes.size() == 1) {
            studyViewFilterUtil.removeSelfFromFilter(attributes.get(0).getAttributeId(), studyViewFilter);
        }
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);
        
        if (filteredSampleIdentifiers.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
        
        List<ClinicalDataCountItem> result = clinicalDataService.fetchClinicalDataCounts(
            studyIds, sampleIds, attributes.stream().map(a -> a.getAttributeId()).collect(Collectors.toList()));
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/clinical-data-bin-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch clinical data bin counts by study view filter")
    public ResponseEntity<List<DataBin>> fetchClinicalDataBinCounts(
        @ApiParam("Method for data binning")
        @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
        @ApiParam(required = true, value = "Clinical data bin count filter")
        @Valid @RequestBody(required = false) ClinicalDataBinCountFilter clinicalDataBinCountFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedClinicalDataBinCountFilter") ClinicalDataBinCountFilter interceptedClinicalDataBinCountFilter) {

        List<ClinicalDataBinFilter> attributes = interceptedClinicalDataBinCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = interceptedClinicalDataBinCountFilter.getStudyViewFilter();

        if (attributes.size() == 1) {
            studyViewFilterUtil.removeSelfFromFilter(attributes.get(0).getAttributeId(), studyViewFilter);
        }

        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);
        List<String> filteredStudyIds = new ArrayList<>();
        List<String> filteredSampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, filteredStudyIds, filteredSampleIds);
        
        List<String> attributeIds = attributes.stream().map(ClinicalDataBinFilter::getAttributeId).collect(Collectors.toList());
        
        List<String> filteredPatientIds = new ArrayList<>();
        List<String> studyIdsOfFilteredPatients = new ArrayList<>();
        patientService.getPatientsOfSamples(filteredStudyIds, filteredSampleIds).stream().forEach(patient -> {
            filteredPatientIds.add(patient.getStableId());
            studyIdsOfFilteredPatients.add(patient.getCancerStudyIdentifier());
        });
        
        List<DataBin> clinicalDataBins = null;
        
        List<String> sampleAttributeIds = new ArrayList<>();
        List<String> patientAttributeIds = new ArrayList<>();
        // patient attributes which are also sample attributes in other studies
        List<String> conflictingPatientAttributeIds = new ArrayList<>();

        List<ClinicalAttribute> clinicalAttributes = clinicalAttributeService
                .getClinicalAttributesByStudyIdsAndAttributeIds(filteredStudyIds, attributeIds);
        clinicalAttributeUtil.extractCategorizedClinicalAttributes(clinicalAttributes, sampleAttributeIds,
                patientAttributeIds, conflictingPatientAttributeIds);

        List<ClinicalData> filteredClinicalData = fetchClinicalData(filteredStudyIds,
                filteredSampleIds,
                filteredPatientIds,
                studyIdsOfFilteredPatients,
                sampleAttributeIds,
                patientAttributeIds,
                conflictingPatientAttributeIds);
        
        Map<String, ClinicalDataType> attributeDatatypeMap = new HashMap<>();
        
        sampleAttributeIds.forEach(attribute->{
            attributeDatatypeMap.put(attribute, ClinicalDataType.SAMPLE);
        });
        patientAttributeIds.forEach(attribute->{
            attributeDatatypeMap.put(attribute, ClinicalDataType.PATIENT);
        });
        conflictingPatientAttributeIds.forEach(attribute->{
            attributeDatatypeMap.put(attribute, ClinicalDataType.SAMPLE);
        });
        
        Map<String, List<ClinicalData>> filteredClinicalDataByAttributeId = 
            filteredClinicalData.stream().collect(Collectors.groupingBy(ClinicalData::getAttrId));

        List<String> filteredUniqueSampleKeys =  getUniqkeyKeys(filteredStudyIds, filteredSampleIds);
        List<String> filteredUniquePatientKeys =  getUniqkeyKeys(studyIdsOfFilteredPatients, filteredPatientIds);
        
        if (dataBinMethod == DataBinMethod.STATIC) {
            StudyViewFilter filter = studyViewFilter == null ? null : new StudyViewFilter();

            if (filter != null) {
                filter.setStudyIds(studyViewFilter.getStudyIds());
                filter.setSampleIdentifiers(studyViewFilter.getSampleIdentifiers());
            }
            List<String> unfilteredStudyIds = new ArrayList<>();
            List<String> unfilteredSampleIds = new ArrayList<>();

            List<SampleIdentifier> unFilteredSampleIdentifiers = studyViewFilterApplier.apply(filter);
            studyViewFilterUtil.extractStudyAndSampleIds(unFilteredSampleIdentifiers, unfilteredStudyIds,
                    unfilteredSampleIds);

            if (!unFilteredSampleIdentifiers.isEmpty()) {
                List<String> unfilteredPatientIds = new ArrayList<>();
                List<String> unfilteredStudyIdsOfPatients = new ArrayList<>();
                patientService.getPatientsOfSamples(unfilteredStudyIds, unfilteredSampleIds).stream()
                        .forEach(patient -> {
                            unfilteredPatientIds.add(patient.getStableId());
                            unfilteredStudyIdsOfPatients.add(patient.getCancerStudyIdentifier());
                        });
                
                List<ClinicalData> unfilteredClinicalData = fetchClinicalData(unfilteredStudyIds, unfilteredSampleIds,
                        unfilteredPatientIds, unfilteredStudyIdsOfPatients, new ArrayList<>(sampleAttributeIds),
                        new ArrayList<>(patientAttributeIds), new ArrayList<>(conflictingPatientAttributeIds));
                Map<String, List<ClinicalData>> unfilteredClinicalDataByAttributeId = unfilteredClinicalData.stream()
                        .collect(Collectors.groupingBy(ClinicalData::getAttrId));

                if (!unfilteredClinicalData.isEmpty()) {
                    List<String> unfilteredUniqueSampleKeys =  getUniqkeyKeys(unfilteredStudyIds, unfilteredSampleIds);
                    List<String> unfilteredUniquePatientKeys =  getUniqkeyKeys(unfilteredStudyIdsOfPatients, unfilteredPatientIds);

                    clinicalDataBins = new ArrayList<>();
                    for (ClinicalDataBinFilter attribute : attributes) {
                        if (attributeDatatypeMap.containsKey(attribute.getAttributeId())) {
                            ClinicalDataType clinicalDataType = attributeDatatypeMap.get(attribute.getAttributeId());
                            List<String> filteredIds = clinicalDataType == ClinicalDataType.PATIENT ? filteredUniquePatientKeys
                                    : filteredUniqueSampleKeys;
                            List<String> unfilteredIds = clinicalDataType == ClinicalDataType.PATIENT
                                    ? unfilteredUniquePatientKeys
                                    : unfilteredUniqueSampleKeys;

                            List<DataBin> dataBins = dataBinner.calculateClinicalDataBins(attribute, clinicalDataType,
                                    filteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(),
                                            Collections.emptyList()),
                                    unfilteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(),
                                            Collections.emptyList()),
                                    filteredIds, unfilteredIds);
                            clinicalDataBins.addAll(dataBins);
                        }
                    }
                }

            }
        }
        else { // dataBinMethod == DataBinMethod.DYNAMIC
            if (!filteredClinicalData.isEmpty()) {
                clinicalDataBins = new ArrayList<>();
                for (ClinicalDataBinFilter attribute : attributes) {

                    if (attributeDatatypeMap.containsKey(attribute.getAttributeId())) {
                        ClinicalDataType clinicalDataType = attributeDatatypeMap.get(attribute.getAttributeId());
                        List<String> filteredIds = clinicalDataType == ClinicalDataType.PATIENT ? filteredUniquePatientKeys
                                : filteredUniqueSampleKeys;

                        List<DataBin> dataBins = dataBinner.calculateClinicalDataBins(attribute, clinicalDataType,
                                filteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(),
                                        Collections.emptyList()),
                                filteredIds);
                        clinicalDataBins.addAll(dataBins);
                    }

                }
            }
        }

        return new ResponseEntity<>(clinicalDataBins, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/mutated-genes/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutated genes by study view filter")
    public ResponseEntity<List<MutationCountByGene>> fetchMutatedGenes(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter) throws StudyNotFoundException {

        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        List<MutationCountByGene> result = new ArrayList<>();
        if (!filteredSampleIdentifiers.isEmpty()) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
            result = mutationService.getSampleCountInMultipleMolecularProfiles(molecularProfileService
                .getFirstMutationProfileIds(studyIds, sampleIds), sampleIds, null, true, false);
            result.sort((a, b) -> b.getNumberOfAlteredCases() - a.getNumberOfAlteredCases());
            List<String> distinctStudyIds = studyIds.stream().distinct().collect(Collectors.toList());
            if (distinctStudyIds.size() == 1 && !result.isEmpty()) {
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

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/fusion-genes/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch fusion genes by study view filter")
    public ResponseEntity<List<MutationCountByGene>> fetchFusionGenes(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. This attribute is needed for the @PreAuthorize tag above.
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter) throws StudyNotFoundException {
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        List<MutationCountByGene> result = new ArrayList<>();
        if (!filteredSampleIdentifiers.isEmpty()) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
            result = mutationService.getSampleCountInMultipleMolecularProfilesForFusions(molecularProfileService
                .getFirstMutationProfileIds(studyIds, sampleIds), sampleIds, null, true, false);
            result.sort((a, b) -> b.getNumberOfAlteredCases() - a.getNumberOfAlteredCases());
            List<String> distinctStudyIds = studyIds.stream().distinct().collect(Collectors.toList());
            if (distinctStudyIds.size() == 1 && !result.isEmpty()) {
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

    
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/cna-genes/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch CNA genes by study view filter")
    public ResponseEntity<List<CopyNumberCountByGene>> fetchCNAGenes(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter) throws StudyNotFoundException {

        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        List<CopyNumberCountByGene> result = new ArrayList<>();
        if (!filteredSampleIdentifiers.isEmpty()) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
            result = discreteCopyNumberService.getSampleCountInMultipleMolecularProfiles(molecularProfileService
                .getFirstDiscreteCNAProfileIds(studyIds, sampleIds), sampleIds, null, Arrays.asList(-2, 2), true, false);
            result.sort((a, b) -> b.getNumberOfAlteredCases() - a.getNumberOfAlteredCases());
            List<String> distinctStudyIds = studyIds.stream().distinct().collect(Collectors.toList());
            if (distinctStudyIds.size() == 1 && !result.isEmpty()) {
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

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/filtered-samples/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch sample IDs by study view filter")
    public ResponseEntity<List<Sample>> fetchFilteredSamples(
        @ApiParam("Whether to negate the study view filters")
        @RequestParam(defaultValue = "false") Boolean negateFilters,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter,
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();

        studyViewFilterUtil.extractStudyAndSampleIds(
            studyViewFilterApplier.apply(interceptedStudyViewFilter, negateFilters), studyIds, sampleIds);

        List<Sample> result = new ArrayList<>();
        if (!sampleIds.isEmpty()) {
            result = sampleService.fetchSamples(studyIds, sampleIds, Projection.ID.name());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/sample-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch sample counts by study view filter")
    public ResponseEntity<MolecularProfileSampleCount> fetchMolecularProfileSampleCounts(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(studyViewFilterApplier.apply(interceptedStudyViewFilter), studyIds, sampleIds);
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
            molecularProfileSampleCount.setNumberOfCNSegmentSamples(Math.toIntExact(sampleService
                .fetchSamples(studyIds, sampleIds, Projection.DETAILED.name()).stream().filter(
                    s -> s.getCopyNumberSegmentPresent()).count()));
            molecularProfileSampleCount.setNumberOfFusionProfiledSamples(Math.toIntExact(sampleService
                .fetchSamples(studyIds, sampleIds, Projection.DETAILED.name()).stream().filter(
                    s -> s.getProfiledForFusions()).count()));
            molecularProfileSampleCount.setNumberOfFusionUnprofiledSamples(sampleCount -
                molecularProfileSampleCount.getNumberOfFusionProfiledSamples());
        }
        return new ResponseEntity<>(molecularProfileSampleCount, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
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
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter,
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(studyViewFilterApplier.apply(interceptedStudyViewFilter), studyIds, sampleIds);
        List<DensityPlotBin> result = new ArrayList<>();
        if (sampleIds.isEmpty()) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        List<String> sampleAttributeIds = new ArrayList<>();
        List<String> patientAttributeIds = new ArrayList<>();
        
        List<ClinicalAttribute> clinicalAttributes = clinicalAttributeService
                .getClinicalAttributesByStudyIdsAndAttributeIds(studyIds,
                        Arrays.asList(xAxisAttributeId, yAxisAttributeId));

        clinicalAttributeUtil.extractCategorizedClinicalAttributes(clinicalAttributes, sampleAttributeIds, patientAttributeIds, patientAttributeIds);

        List<Patient> patients = new ArrayList<>();
        List<String> patientIds = new ArrayList<>();
        List<String> studyIdsOfPatients = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(patientAttributeIds)) {
            patients = patientService.getPatientsOfSamples(studyIds, sampleIds).stream().collect(Collectors.toList());
            patientIds = patients.stream().map(Patient::getStableId).collect(Collectors.toList());
            studyIdsOfPatients = patients.stream().map(Patient::getCancerStudyIdentifier).collect(Collectors.toList());
        }

        List<ClinicalData> clinicalDataList = fetchClinicalData(studyIds, sampleIds, patientIds, studyIdsOfPatients,
                sampleAttributeIds, patientAttributeIds, null);

        Map<String, Map<String, List<ClinicalData>>> clinicalDataMap;
        if (!sampleAttributeIds.isEmpty()) {
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


    @RequestMapping(value = "/study-view-treatments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get study view treatment rows")
    public ResponseEntity<List<TreatmentRow>> getTreatmentRows() {
        List<String> samples = Arrays.asList("P01_Pri", "P01_Rec", "P04_Pri", "P04_Rec1", "P04_Rec2", "P04_Rec3", "P05_Pri", "P05_Rec", "P06_Pri", "P06_Rec", "P10_Pri", "P10_Rec", "P11_Pri", "P11_Rec", "P17_Pri_A", "P17_Pri_B", "P17_Pri_C", "P17_Rec1_A", "P17_Rec1_B", "P17_Rec1_C", "P17_Rec1_D", "P18_Pri_A", "P18_Pri_B", "P18_Pri_C", "P18_Pri_D", "P18_Rec", "P21_Pri", "P21_Rec", "P24_Pri", "P24_Rec1", "P24_Rec2", "MSK_LX13", "MSK_LX138", "MSK_LX148B", "MSK_LX151", "MSK_LX154", "MSK_LX174", "MSK_LX175", "MSK_LX181", "MSK_LX189", "MSK_LX19", "MSK_LX206", "MSK_LX206B", "MSK_LX213", "MSK_LX216", "MSK_LX224", "MSK_LX229", "MSK_LX234", "MSK_LX239", "MSK_LX242", "MSK_LX25", "MSK_LX263", "MSK_LX267B", "MSK_LX27", "MSK_LX276", "MSK_LX282", "MSK_LX285", "MSK_LX29", "MSK_LX292", "MSK_LX293B", "MSK_LX296", "MSK_LX298", "MSK_LX304", "MSK_LX304B", "MSK_LX307", "MSK_LX318", "MSK_LX326", "MSK_LX337", "MSK_LX349", "MSK_LX366", "MSK_LX369", "MSK_LX376", "MSK_LX382B", "MSK_LX393B", "MSK_LX40", "MSK_LX408", "MSK_LX413", "MSK_LX424", "MSK_LX424B", "MSK_LX462", "MSK_LX465", "MSK_LX479", "MSK_LX482", "MSK_LX489", "MSK_LX491", "MSK_LX512", "MSK_LX513", "MSK_LX524", "MSK_LX55", "MSK_LX585", "MSK_LX59", "MSK_LX631", "MSK_LX632", "MSK_LX640", "MSK_LX651", "MSK_LX666", "MSK_LX87", "MSK_LX95", "MSK_LX96", "TRF050916", "TRF053947", "TRF027871", "TRF043513", "TRF034438", "TRF030123", "TRF050483", "TRF064189", "TRF031593", "TRF030626", "TRF014012", "TRF021125", "TRF029672", "TRF044138", "TRF022408", "TRF014754", "TRF034570", "TRF054303", "TRF021879", "TRF053946", "TRF020252", "TRF019288", "TRF043500", "TRF047202", "TRF043644", "TRF048714", "TRF070135", "TRF024470", "TRF042339", "TRF036869", "TRF054406", "TRF102171", "TRF047623", "TRF050678", "TRF044571", "TRF011251", "TRF036890", "TRF050238", "TRF049276", "TRF042199", "TRF044293", "TRF050394", "TRF045887", "TRF040129", "TRF051443", "TRF036780", "TRF030611", "TRF064641", "TRF078041", "TRF061165", "TRF056390", "TRF034423", "TRF077655", "TRF041606", "TRF065229", "TRF029659", "TRF060980", "TRF077654", "TRF057063", "TRF049396", "TRF050931", "TRF020703", "TRF050912", "TRF016430", "TRF037647", "TRF030125", "TRF036863", "TRF071705", "TRF065699", "TRF042508", "TRF030605", "TRF067088", "TRF047834", "TRF017822", "TRF079056", "TRF034433", "TRF042358", "TRF028383", "TRF036748", "TRF043485", "TRF018658", "TRF050441", "TRF041560", "TRF047609", "TRF018670", "TRF046085", "TRF022348", "TRF035707", "TRF061214", "TRF066283", "TRF030607", "TRF033243", "TRF068126", "TRF060952", "TRF9155", "TRF072363", "TRF030628", "TRF079053", "TRF040046", "TRF033231", "TRF050430", "TRF040878", "TRF024471", "TRF043499", "TRF042507", "TRF043510", "TRF036897", "TRF040880", "TRF023776", "TRF040887", "TRF027848", "TRF036044", "TRF067080", "TRF027872", "TRF083668", "TRF043342", "TRF065134", "TRF042510", "TRF029667", "TRF035201", "TRF054314", "TRF039306", "TRF043651", "TRF051132", "TRF078227", "TRF071913", "TRF043353", "TRF068124", "TRF029674", "TRF027861", "TRF050416", "TRF051089", "TRF053605", "TRF051544", "TRF069124", "TRF027874", "TRF065100", "TRF080374", "TRF054735", "TRF029668", "TRF066660", "TRF043492", "TRF050776", "TRF022209", "TRF061174", "TRF035705", "TRF030609", "TRF022879", "TRF036750", "TRF059651", "TRF056398");
        List<TreatmentRow> result = treatmentService.getAllTreatmentRows(
            samples,
            Arrays.asList("nsclc_tcga_broad_2016")
        );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    private List<ClinicalData> fetchClinicalData(List<String> studyIds,
            List<String> sampleIds,
            List<String> patientIds,
            List<String> studyIdsOfPatients,
            List<String> sampleAttributeIds,
            List<String> patientAttributeIds,
            List<String> conflictingPatientAttributes) {

        List<ClinicalData> combinedResult = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(sampleAttributeIds)) {
            List<ClinicalData> filteredClinicalDataForSamples = clinicalDataService.fetchClinicalData(studyIds,
                    sampleIds, sampleAttributeIds, "SAMPLE", Projection.SUMMARY.name());
            combinedResult.addAll(filteredClinicalDataForSamples);
        }

        if (CollectionUtils.isNotEmpty(patientAttributeIds)) {
            List<ClinicalData> filteredClinicalDataForPatients = clinicalDataService.fetchClinicalData(
                    studyIdsOfPatients, patientIds, patientAttributeIds, "PATIENT", Projection.SUMMARY.name());
            combinedResult.addAll(filteredClinicalDataForPatients);
        }

        if (CollectionUtils.isNotEmpty(conflictingPatientAttributes)) {
            List<ClinicalData> filteredClinicalDataForPatients = clinicalDataService.getPatientClinicalDataDetailedToSample(
                    studyIdsOfPatients, patientIds, conflictingPatientAttributes);
            combinedResult.addAll(filteredClinicalDataForPatients);
        }

        return combinedResult;
    }

    private List<String> getUniqkeyKeys(List<String> studyIds, List<String> caseIds) {
        List<String> uniqkeyKeys = new ArrayList<String>();
        for (int i = 0; i < caseIds.size(); i++) {
            uniqkeyKeys.add(studyViewFilterUtil.getCaseUniqueKey(studyIds.get(i), caseIds.get(i)));
        }
        return uniqkeyKeys;
    }
    
}
