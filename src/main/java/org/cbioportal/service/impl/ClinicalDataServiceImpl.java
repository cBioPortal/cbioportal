package org.cbioportal.service.impl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.persistence.mybatis.util.PaginationCalculator;
import org.cbioportal.service.*;
import org.cbioportal.service.exception.*;
import org.cbioportal.service.util.ClinicalAttributeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cbioportal.utils.Encoder.calculateBase64;

@Service
public class ClinicalDataServiceImpl implements ClinicalDataService {

    @Autowired
    private ClinicalDataRepository clinicalDataRepository;
    @Autowired
    private StudyService studyService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private ClinicalAttributeService clinicalAttributeService;
    @Autowired
    private ClinicalAttributeUtil clinicalAttributeUtil;

    @Override
    public List<ClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId, String attributeId, 
                                                                String projection, Integer pageSize, Integer pageNumber,
                                                                String sortBy, String direction)
        throws SampleNotFoundException, StudyNotFoundException {

        sampleService.getSampleInStudy(studyId, sampleId);
        
        return clinicalDataRepository.getAllClinicalDataOfSampleInStudy(studyId, sampleId, attributeId, projection,
                pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId)
        throws SampleNotFoundException, StudyNotFoundException {

        sampleService.getSampleInStudy(studyId, sampleId);

        return clinicalDataRepository.getMetaSampleClinicalData(studyId, sampleId, attributeId);
    }

    @Override
    public List<ClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId, String attributeId, 
                                                                 String projection, Integer pageSize, 
                                                                 Integer pageNumber, String sortBy, String direction)
        throws PatientNotFoundException, StudyNotFoundException {
        
        patientService.getPatientInStudy(studyId, patientId);

        return clinicalDataRepository.getAllClinicalDataOfPatientInStudy(studyId, patientId, attributeId, projection,
                pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId)
        throws PatientNotFoundException, StudyNotFoundException {

        patientService.getPatientInStudy(studyId, patientId);

        return clinicalDataRepository.getMetaPatientClinicalData(studyId, patientId, attributeId);
    }

    @Override
    public List<ClinicalData> getAllClinicalDataInStudy(String studyId, String attributeId, String clinicalDataType, 
                                                        String projection, Integer pageSize, Integer pageNumber,
                                                        String sortBy, String direction) throws StudyNotFoundException {
        
        studyService.getStudy(studyId);

        return clinicalDataRepository.getAllClinicalDataInStudy(studyId, attributeId, clinicalDataType, projection,
                pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType) 
        throws StudyNotFoundException {

        studyService.getStudy(studyId);
        
        return clinicalDataRepository.getMetaAllClinicalData(studyId, attributeId, clinicalDataType);
    }

    @Override
    public List<ClinicalData> fetchAllClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds,
                                                          String clinicalDataType, String projection) 
        throws StudyNotFoundException {

        studyService.getStudy(studyId);
        
        return clinicalDataRepository.fetchAllClinicalDataInStudy(studyId, ids, attributeIds, clinicalDataType, 
            projection);
    }

    @Override
    public BaseMeta fetchMetaClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds,
                                                 String clinicalDataType) throws StudyNotFoundException {

        studyService.getStudy(studyId);
        
        return clinicalDataRepository.fetchMetaClinicalDataInStudy(studyId, ids, attributeIds, clinicalDataType);
    }

    @Override
    public List<ClinicalData> fetchClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds,
                                                String clinicalDataType, String projection) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return clinicalDataRepository.fetchClinicalData(studyIds, ids, attributeIds, clinicalDataType, projection);
    }

    @Override
    public BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds, 
                                          String clinicalDataType) {

        return clinicalDataRepository.fetchMetaClinicalData(studyIds, ids, attributeIds, clinicalDataType);
    }

	@Override
	public List<ClinicalDataCountItem> fetchClinicalDataCounts(List<String> studyIds, List<String> sampleIds,
            List<String> attributeIds) {

        if (attributeIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ClinicalAttribute> clinicalAttributes = clinicalAttributeService
                .getClinicalAttributesByStudyIdsAndAttributeIds(studyIds, attributeIds);

        List<String> sampleAttributeIds = new ArrayList<>();
        List<String> patientAttributeIds = new ArrayList<>();
        // patient attributes which are also sample attributes in other studies
        List<String> conflictingPatientAttributeIds = new ArrayList<>();

        clinicalAttributeUtil.extractCategorizedClinicalAttributes(clinicalAttributes, sampleAttributeIds,
                patientAttributeIds, conflictingPatientAttributeIds);

        List<ClinicalDataCount> clinicalDataCounts = new ArrayList<ClinicalDataCount>();
        if (!sampleAttributeIds.isEmpty()) {
            clinicalDataCounts.addAll(clinicalDataRepository.fetchClinicalDataCounts(studyIds, sampleIds,
                    sampleAttributeIds, "SAMPLE", "SUMMARY"));
        }

        if (!patientAttributeIds.isEmpty()) {
            clinicalDataCounts.addAll(clinicalDataRepository.fetchClinicalDataCounts(studyIds, sampleIds,
                    patientAttributeIds, "PATIENT", "SUMMARY"));
        }

        if (!conflictingPatientAttributeIds.isEmpty()) {
            clinicalDataCounts.addAll(clinicalDataRepository.fetchClinicalDataCounts(studyIds, sampleIds,
                    conflictingPatientAttributeIds, "PATIENT", "DETAILED"));
        }

        sampleAttributeIds.addAll(conflictingPatientAttributeIds);

        clinicalDataCounts = clinicalDataCounts
                .stream().filter(c -> !c.getValue().toUpperCase().equals("NA")
                        && !c.getValue().toUpperCase().equals("NAN") && !c.getValue().toUpperCase().equals("N/A"))
                .collect(Collectors.toList());

        Map<String, List<ClinicalDataCount>> clinicalDataCountMap = clinicalDataCounts.stream()
                .collect(Collectors.groupingBy(ClinicalDataCount::getAttributeId));

        List<Patient> patients = new ArrayList<Patient>();
        if (!patientAttributeIds.isEmpty()) {
            patients.addAll(patientService.getPatientsOfSamples(studyIds, sampleIds));
        }

        HashSet<String> uniqueAttributeIds = new HashSet<>(attributeIds);

        return uniqueAttributeIds.stream().map(attributeId -> {

            int naCount = 0;
            int totalCount = 0;
            List<ClinicalDataCount> counts = clinicalDataCountMap.getOrDefault(attributeId, new ArrayList<>());

            if (conflictingPatientAttributeIds.contains(attributeId)) {
                // if its a conflicting attribute then sum all counts
                counts = counts.stream().collect(Collectors.toMap(ClinicalDataCount::getValue, Function.identity(),
                        (clinicalDataCount1, clinicalDataCount2) -> {
                            clinicalDataCount1.setCount(clinicalDataCount1.getCount() + clinicalDataCount2.getCount());
                            return clinicalDataCount1;
                        })).values().stream().collect(Collectors.toList());
            }

            if (!counts.isEmpty()) {
                totalCount = counts.stream().mapToInt(ClinicalDataCount::getCount).sum();
            }

            if (sampleAttributeIds.contains(attributeId)) {
                naCount = sampleIds.size() - totalCount;
            } else {
                naCount = patients.size() - totalCount;
            }

            if (naCount > 0) {
                ClinicalDataCount clinicalDataCount = new ClinicalDataCount();
                clinicalDataCount.setAttributeId(attributeId);
                clinicalDataCount.setValue("NA");
                clinicalDataCount.setCount(naCount);
                counts.add(clinicalDataCount);
            }

            ClinicalDataCountItem clinicalDataCountItem = new ClinicalDataCountItem();
            clinicalDataCountItem.setAttributeId(attributeId);
            clinicalDataCountItem.setCounts(counts);
            return clinicalDataCountItem;

        }).collect(Collectors.toList());
    }

    @Override
    public List<ClinicalData> getPatientClinicalDataDetailedToSample(List<String> studyIds, List<String> patientIds,
            List<String> attributeIds) {
        return clinicalDataRepository.getPatientClinicalDataDetailedToSample(studyIds, patientIds, attributeIds);
    }

    @Override
    public ImmutablePair<SampleClinicalDataCollection, Integer> fetchSampleClinicalTable(List<String> studyIds, List<String> sampleIds, Integer pageSize, Integer pageNumber, String searchTerm, String sortBy, String direction) {
        if (studyIds == null || studyIds.isEmpty() || sampleIds == null || sampleIds.isEmpty()) {
            return new ImmutablePair<>(SampleClinicalDataCollection.builder().build(), 0);
        }

        // Request un-paginated data.
        List<Integer> allSampleInternalIds = clinicalDataRepository.getVisibleSampleInternalIdsForClinicalTable(
            studyIds, sampleIds,
            null, null,
            searchTerm, sortBy, direction
        );
        Integer offset = PaginationCalculator.offset(pageSize, pageNumber);

        if (allSampleInternalIds.isEmpty() || offset >= allSampleInternalIds.size()) {
            return new ImmutablePair<>(SampleClinicalDataCollection.builder().build(), 0);
        }

        return buildSampleClinicalDataCollection(allSampleInternalIds, offset, pageSize);
    }

    private ImmutablePair<SampleClinicalDataCollection, Integer> buildSampleClinicalDataCollection(List<Integer> allSampleInternalIds, Integer offset, Integer pageSize) {
        
        // Apply pagination to the sampleId list.
        Integer toIndex = PaginationCalculator.lastIndex(offset, pageSize, allSampleInternalIds.size());
        List<Integer> visibleSampleInternalIds = allSampleInternalIds.subList(offset, toIndex);

        List<ClinicalData> sampleClinicalData = clinicalDataRepository.getSampleClinicalDataBySampleInternalIds(visibleSampleInternalIds);
        List<ClinicalData> patientClinicalData = clinicalDataRepository.getPatientClinicalDataBySampleInternalIds(visibleSampleInternalIds);

        // Merge sample and patient level clinical data and key by unique sample-key.
        SampleClinicalDataCollection sampleClinicalDataCollection = SampleClinicalDataCollection.builder().withByUniqueSampleKey(
            Stream.concat(sampleClinicalData.stream(), patientClinicalData.stream())
                .collect(Collectors.groupingBy(clinicalDatum -> calculateBase64(clinicalDatum.getSampleId(), clinicalDatum.getStudyId())))
        ).build();

        return new ImmutablePair<>(sampleClinicalDataCollection, allSampleInternalIds.size());
    }
}
