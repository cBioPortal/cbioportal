package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.springframework.web.bind.annotation.*;

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
    private MolecularDataService molecularDataService;
    @Autowired
    private GeneService geneService;
    @Autowired
    private SampleListService sampleListService;

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
    public ResponseEntity<List<ClinicalDataBin>> fetchClinicalDataBinCounts(
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
        List<ClinicalDataBin> clinicalDataBins = new ArrayList<>();
        List<String> filteredSampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, filteredStudyIds, filteredSampleIds);
        
        List<String> attributeIds = attributes.stream().map(ClinicalDataBinFilter::getAttributeId).collect(Collectors.toList());
        
        List<String> filteredPatientIds = new ArrayList<>();
        List<String> studyIdsOfFilteredPatients = new ArrayList<>();
        patientService.getPatientsOfSamples(filteredStudyIds, filteredSampleIds).stream().forEach(patient -> {
            filteredPatientIds.add(patient.getStableId());
            studyIdsOfFilteredPatients.add(patient.getCancerStudyIdentifier());
        });
        
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

                if (!unfilteredClinicalData.isEmpty()) {
                    List<String> unfilteredUniqueSampleKeys =  getUniqkeyKeys(unfilteredStudyIds, unfilteredSampleIds);
                    List<String> unfilteredUniquePatientKeys =  getUniqkeyKeys(unfilteredStudyIdsOfPatients, unfilteredPatientIds);

                    Map<String, List<ClinicalData>> unfilteredClinicalDataByAttributeId = unfilteredClinicalData.stream()
                            .collect(Collectors.groupingBy(ClinicalData::getAttrId));

                    clinicalDataBins = new ArrayList<>();
                    for (ClinicalDataBinFilter attribute : attributes) {
                        if (attributeDatatypeMap.containsKey(attribute.getAttributeId())) {
                            ClinicalDataType clinicalDataType = attributeDatatypeMap.get(attribute.getAttributeId());
                            List<String> filteredIds = clinicalDataType == ClinicalDataType.PATIENT ? filteredUniquePatientKeys
                                    : filteredUniqueSampleKeys;
                            List<String> unfilteredIds = clinicalDataType == ClinicalDataType.PATIENT
                                    ? unfilteredUniquePatientKeys
                                    : unfilteredUniqueSampleKeys;

                            List<ClinicalDataBin> dataBins = dataBinner
                                    .calculateClinicalDataBins(attribute, clinicalDataType,
                                            filteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(),
                                                    Collections.emptyList()),
                                            unfilteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(),
                                                    Collections.emptyList()),
                                            filteredIds, unfilteredIds)
                                    .stream()
                                    .map(dataBin -> studyViewFilterUtil.dataBinToClinicalDataBin(attribute, dataBin))
                                    .collect(Collectors.toList());

                            clinicalDataBins.addAll(dataBins);
                        }
                    }
                }

            }
        }
        else { // dataBinMethod == DataBinMethod.DYNAMIC
            if (!filteredClinicalData.isEmpty()) {
                for (ClinicalDataBinFilter attribute : attributes) {

                    if (attributeDatatypeMap.containsKey(attribute.getAttributeId())) {
                        ClinicalDataType clinicalDataType = attributeDatatypeMap.get(attribute.getAttributeId());
                        List<String> filteredIds = clinicalDataType == ClinicalDataType.PATIENT
                                ? filteredUniquePatientKeys
                                : filteredUniqueSampleKeys;

                        List<ClinicalDataBin> dataBins = dataBinner
                                .calculateDataBins(attribute, clinicalDataType,
                                        filteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(),
                                                Collections.emptyList()),
                                        filteredIds)
                                .stream()
                                .map(dataBin -> studyViewFilterUtil.dataBinToClinicalDataBin(attribute, dataBin))
                                .collect(Collectors.toList());
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
    @RequestMapping(value = "/molecular-profile-sample-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch sample counts by study view filter")
    public List<GenomicDataCount> fetchMolecularProfileSampleCounts(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(studyViewFilterApplier.apply(interceptedStudyViewFilter), studyIds,
                sampleIds);
        
        List<MolecularProfile> molecularProfiles = molecularProfileService.getMolecularProfilesInStudies(studyIds,
                "SUMMARY");

        Map<String, List<MolecularProfile>> studyMolecularProfilesSet = molecularProfiles.stream()
                .collect(Collectors.groupingBy(MolecularProfile::getCancerStudyIdentifier));

        List<String> queryMolecularProfileIds = new ArrayList<>();
        List<String> querySampleIds = new ArrayList<>();
        for (int i = 0; i < studyIds.size(); i++) {
            String studyId = studyIds.get(i);
            String sampleId = sampleIds.get(i);
            if (studyMolecularProfilesSet.containsKey(studyId)) {
                studyMolecularProfilesSet.get(studyId).stream().forEach(molecularProfile -> {
                    queryMolecularProfileIds.add(molecularProfile.getStableId());
                    querySampleIds.add(sampleId);
                });
            }
        }

        List<GenePanelData> genePanelData = genePanelService
                .fetchGenePanelDataInMultipleMolecularProfiles(queryMolecularProfileIds, querySampleIds);
        HashMap<String, Integer> molecularPorfileSampleCountSet = new HashMap<String, Integer>();

        for (GenePanelData datum : genePanelData) {
            if (datum.getProfiled()) {
                Integer count = molecularPorfileSampleCountSet.getOrDefault(datum.getMolecularProfileId(), 0);
                molecularPorfileSampleCountSet.put(datum.getMolecularProfileId(), count + 1);
            }
        }

        Map<String, List<MolecularProfile>> molecularProfileSet = studyViewFilterUtil
                .categorizeMolecularPorfiles(molecularProfiles);

        return molecularProfileSet
                .entrySet()
                .stream()
                .map(entry -> {
                    GenomicDataCount dataCount = new GenomicDataCount();
                    dataCount.setValue(entry.getKey());

                    Integer count = entry.getValue().stream().mapToInt(molecularProfile -> {
                        return molecularPorfileSampleCountSet.getOrDefault(molecularProfile.getStableId(), 0);
                    }).sum();
        
                    dataCount.setCount(count);
                    dataCount.setLabel(entry.getValue().get(0).getName());
        
                    return dataCount;
                })
                .filter(dataCount -> dataCount.getCount() > 0)
                .collect(Collectors.toList());

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

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/sample-lists-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch case list sample counts by study view filter")
    public List<CaseListDataCount> fetchCaseListCounts(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
        List<SampleList> sampleLists = sampleListService.getAllSampleListsInStudies(studyIds,
                Projection.DETAILED.name());

        HashMap<String, Integer> sampleCountBySampleListId = new HashMap<String, Integer>();

        Map<String, SampleIdentifier> filteredSampleSet = filteredSampleIdentifiers.stream()
                .collect(Collectors.toMap(sampleidentifier -> studyViewFilterUtil
                        .getCaseUniqueKey(sampleidentifier.getStudyId(), sampleidentifier.getSampleId()),
                        Function.identity()));

        for (SampleList sampleList : sampleLists) {
            for (String sampleId : sampleList.getSampleIds()) {
                if (filteredSampleSet.containsKey(
                        studyViewFilterUtil.getCaseUniqueKey(sampleList.getCancerStudyIdentifier(), sampleId))) {
                    Integer count = sampleCountBySampleListId.getOrDefault(sampleList.getStableId(), 0);
                    sampleCountBySampleListId.put(sampleList.getStableId(), count + 1);
                }
            }
        }

        return studyViewFilterUtil
                .categorizeSampleLists(sampleLists)
                .entrySet()
                .stream()
                .map(entry -> {
                    CaseListDataCount dataCount = new CaseListDataCount();
                    dataCount.setValue(entry.getKey());
        
                    Integer count = entry.getValue().stream().mapToInt(sampleList -> {
                        return sampleCountBySampleListId.getOrDefault(sampleList.getStableId(), 0);
                    }).sum();
        
                    dataCount.setCount(count);
                    dataCount.setLabel(entry.getValue().get(0).getName());
        
                    return dataCount;
                })
                .filter(dataCount -> dataCount.getCount() > 0)
                .collect(Collectors.toList());

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
    
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/genomic-data-bin-counts/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch genomic data bin counts by study view filter")
    public ResponseEntity<List<GenomicDataBin>> fetchGenomicDataBinCounts(
            @ApiParam("Method for data binning") @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
            @ApiParam(required = true, value = "Genomic data bin count filter") @Valid @RequestBody(required = false) GenomicDataBinCountFilter genomicDataBinCountFilter,
            @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
            @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
            @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this
                       // attribute is needed for the @PreAuthorize tag above.
            @Valid @RequestAttribute(required = false, value = "interceptedGenomicDataBinCountFilter") GenomicDataBinCountFilter interceptedGenomicDataBinCountFilter) {

        List<GenomicDataBinFilter> genomicDataBinFilters = interceptedGenomicDataBinCountFilter
                .getGenomicDataBinFilters();

        StudyViewFilter studyViewFilter = interceptedGenomicDataBinCountFilter.getStudyViewFilter();

        if (genomicDataBinFilters.size() == 1) {
            studyViewFilterUtil.removeSelfFromFilter(genomicDataBinFilters.get(0), studyViewFilter);
        }

        List<GenomicDataBin> genomicDataBins = new ArrayList<>();
        List<String> filteredSampleIds = new ArrayList<>();
        List<String> filteredStudyIds = new ArrayList<>();
        List<ClinicalData> filteredData = fetchGenomicData(genomicDataBinFilters, studyViewFilter, filteredSampleIds,
                filteredStudyIds);

        List<String> filteredUniqueSampleKeys = getUniqkeyKeys(filteredStudyIds, filteredSampleIds);

        Map<String, List<ClinicalData>> filteredClinicalDataByAttributeId = filteredData.stream()
                .collect(Collectors.groupingBy(ClinicalData::getAttrId));

        if (dataBinMethod == DataBinMethod.STATIC) {

            StudyViewFilter filter = studyViewFilter == null ? null : new StudyViewFilter();
            if (filter != null) {
                filter.setStudyIds(studyViewFilter.getStudyIds());
                filter.setSampleIdentifiers(studyViewFilter.getSampleIdentifiers());
            }

            List<String> unfilteredSampleIds = new ArrayList<>();
            List<String> unfilteredStudyIds = new ArrayList<>();
            List<ClinicalData> unfilteredData = fetchGenomicData(genomicDataBinFilters, filter, unfilteredSampleIds,
                    unfilteredStudyIds);
            List<String> unFilteredUniqueSampleKeys = getUniqkeyKeys(unfilteredSampleIds, unfilteredStudyIds);

            Map<String, List<ClinicalData>> unfilteredDataByAttributeId = unfilteredData.stream()
                    .collect(Collectors.groupingBy(ClinicalData::getAttrId));

            genomicDataBins = genomicDataBinFilters.stream().flatMap(genomicDataBinFilter -> {
                String attributeId = studyViewFilterUtil.getGenomicDataFilterUniqueKey(
                        genomicDataBinFilter.getHugoGeneSymbol(), genomicDataBinFilter.getProfileType());

                List<DataBin> dataBins = dataBinner.calculateClinicalDataBins(genomicDataBinFilter,
                        ClinicalDataType.SAMPLE,
                        filteredClinicalDataByAttributeId.getOrDefault(attributeId, Collections.emptyList()),
                        unfilteredDataByAttributeId.getOrDefault(attributeId, Collections.emptyList()),
                        filteredUniqueSampleKeys, unFilteredUniqueSampleKeys);

                return dataBins.stream()
                        .map(dataBin -> studyViewFilterUtil.dataBintoGenomicDataBin(genomicDataBinFilter, dataBin));
            }).collect(Collectors.toList());
        } else { // dataBinMethod == DataBinMethod.DYNAMIC
            genomicDataBins = genomicDataBinFilters.stream().flatMap(genomicDataBinFilter -> {
                String attributeId = studyViewFilterUtil.getGenomicDataFilterUniqueKey(
                        genomicDataBinFilter.getHugoGeneSymbol(), genomicDataBinFilter.getProfileType());

                List<DataBin> dataBins = dataBinner.calculateDataBins(genomicDataBinFilter,
                        ClinicalDataType.SAMPLE,
                        filteredClinicalDataByAttributeId.getOrDefault(attributeId, Collections.emptyList()),
                        filteredUniqueSampleKeys);

                return dataBins.stream()
                        .map(dataBin -> studyViewFilterUtil.dataBintoGenomicDataBin(genomicDataBinFilter, dataBin));
            }).collect(Collectors.toList());
        }

        return new ResponseEntity<>(genomicDataBins, HttpStatus.OK);
    }

    private List<ClinicalData> fetchGenomicData(List<GenomicDataBinFilter> genomicDataFilters,
            StudyViewFilter studyViewFilter, List<String> sampleIds, List<String> studyIds) {

        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);

        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
        
        List<MolecularProfile> molecularProfiles = molecularProfileService.getMolecularProfilesInStudies(studyIds,"SUMMARY");

        Set<String> hugoGeneSymbols = genomicDataFilters.stream().map(GenomicDataBinFilter::getHugoGeneSymbol)
                .collect(Collectors.toSet());

        Map<String, Integer> geneSymbolIdMap = geneService
                .fetchGenes(new ArrayList<>(hugoGeneSymbols), GeneIdType.HUGO_GENE_SYMBOL.name(),
                        Projection.SUMMARY.name())
                .stream().collect(Collectors.toMap(Gene::getHugoGeneSymbol, Gene::getEntrezGeneId));

        Map<String, List<MolecularProfile>> molecularProfileMap = studyViewFilterUtil
                .categorizeMolecularPorfiles(molecularProfiles);

        return genomicDataFilters.stream().flatMap(genomicDataFilter -> {

            Map<String, String> studyIdToMolecularProfileIdMap = molecularProfileMap.getOrDefault(genomicDataFilter
                    .getProfileType(), new ArrayList<MolecularProfile>())
                    .stream()
                    .collect(Collectors.toMap(MolecularProfile::getCancerStudyIdentifier,
                            MolecularProfile::getStableId));

            List<String> mappedSampleIds = new ArrayList<>();
            List<String> mappedProfileIds = new ArrayList<>();

            for (int i = 0; i < sampleIds.size(); i++) {
                String studyId = studyIds.get(i);
                if (studyIdToMolecularProfileIdMap.containsKey(studyId)) {
                    mappedSampleIds.add(sampleIds.get(i));
                    mappedProfileIds.add(studyIdToMolecularProfileIdMap.get(studyId));
                }
            }

            if (mappedSampleIds.isEmpty()) {
                return Stream.of();
            }

            String attributeId = studyViewFilterUtil.getGenomicDataFilterUniqueKey(
                    genomicDataFilter.getHugoGeneSymbol(), genomicDataFilter.getProfileType());

            return molecularDataService.getMolecularDataInMultipleMolecularProfiles(mappedProfileIds, mappedSampleIds,
                    Arrays.asList(geneSymbolIdMap.get(genomicDataFilter.getHugoGeneSymbol())), Projection.SUMMARY.name())
                    .stream().map(geneMolecularData -> {
                        ClinicalData clinicalData = new ClinicalData();
                        clinicalData.setAttrId(attributeId);
                        clinicalData.setAttrValue(geneMolecularData.getValue());
                        clinicalData.setPatientId(geneMolecularData.getPatientId());
                        clinicalData.setSampleId(geneMolecularData.getSampleId());
                        clinicalData.setStudyId(geneMolecularData.getStudyId());
                        return clinicalData;
                    });

        }).collect(Collectors.toList());
    }
}
