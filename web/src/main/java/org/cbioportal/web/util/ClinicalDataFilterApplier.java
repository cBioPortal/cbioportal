package org.cbioportal.web.util;

import org.apache.commons.collections.map.MultiKeyMap;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.Patient;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.PatientService;
import org.cbioportal.web.parameter.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ClinicalDataFilterApplier {
    private PatientService patientService;
    private ClinicalDataService clinicalDataService;
    protected StudyViewFilterUtil studyViewFilterUtil;

    public ClinicalDataFilterApplier(PatientService patientService,
                                     ClinicalDataService clinicalDataService,
                                     StudyViewFilterUtil studyViewFilterUtil) {
        this.patientService = patientService;
        this.clinicalDataService = clinicalDataService;
        this.studyViewFilterUtil = studyViewFilterUtil;
    }

    public List<SampleIdentifier> apply(List<SampleIdentifier> sampleIdentifiers,
                                        List<ClinicalDataFilter> clinicalDataFilters,
                                        Boolean negateFilters) {
        if (!clinicalDataFilters.isEmpty() && !sampleIdentifiers.isEmpty()) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            
            studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);

            List<Patient> patients = patientService.getPatientsOfSamples(studyIds, sampleIds);
            List<String> patientIds = patients.stream().map(Patient::getStableId).collect(Collectors.toList());
            List<String> studyIdsOfPatients = patients.stream().map(Patient::getCancerStudyIdentifier).collect(Collectors.toList());

            List<String> attributeIds = clinicalDataFilters.stream().map(ClinicalDataFilter::getAttributeId)
                    .collect(Collectors.toList());

            List<ClinicalData> clinicalDataList = new ArrayList<ClinicalData>();

            List<ClinicalData> sampleClinicalDataList = clinicalDataService.fetchClinicalData(studyIds, sampleIds,
                    attributeIds, "SAMPLE", Projection.SUMMARY.name());
            clinicalDataList.addAll(sampleClinicalDataList);

            List<ClinicalData> patientClinicalDataList = clinicalDataService
                    .getPatientClinicalDataDetailedToSample(studyIdsOfPatients, patientIds, attributeIds);
            clinicalDataList.addAll(patientClinicalDataList);

            clinicalDataList.forEach(c -> {
                c.setAttrValue(c.getAttrValue().toUpperCase());
                if (c.getAttrValue().equals("NAN") || c.getAttrValue().equals("N/A")) {
                    c.setAttrValue("NA");
                }
            });

            List<SampleIdentifier> newSampleIdentifiers = new ArrayList<>();
            MultiKeyMap clinicalDataMap = new MultiKeyMap();
            for (ClinicalData clinicalData : clinicalDataList) {
                if (clinicalDataMap.containsKey(clinicalData.getSampleId(), clinicalData.getStudyId())) {
                    ((List<ClinicalData>)clinicalDataMap.get(clinicalData.getSampleId(), clinicalData.getStudyId())).add(clinicalData);
                } else {
                    List<ClinicalData> clinicalDatas = new ArrayList<>();
                    clinicalDatas.add(clinicalData);
                    clinicalDataMap.put(clinicalData.getSampleId(), clinicalData.getStudyId(), clinicalDatas);
                }
            }

            List<String> ids = new ArrayList<>();
            List<String> studyIdsOfIds = new ArrayList<>();
            int index = 0;
            for (String entityId : sampleIds) {
                String studyId = studyIds.get(index);

                int count = apply(clinicalDataFilters, clinicalDataMap, entityId, studyId, negateFilters);

                if (count == clinicalDataFilters.size()) {
                    ids.add(entityId);
                    studyIdsOfIds.add(studyId);
                }
                index++;
            }

            Set<String> idsSet = new HashSet<>(ids);
            idsSet.retainAll(new HashSet<>(sampleIds));
            for (int i = 0; i < ids.size(); i++) {
                SampleIdentifier sampleIdentifier = new SampleIdentifier();
                sampleIdentifier.setSampleId(ids.get(i));
                sampleIdentifier.setStudyId(studyIdsOfIds.get(i));
                newSampleIdentifiers.add(sampleIdentifier);
            }
            return newSampleIdentifiers;
        }
        return sampleIdentifiers;
    }

    // Must be overridden by child classes
    protected abstract Integer apply(List<ClinicalDataFilter> attributes, MultiKeyMap clinicalDataMap, String entityId, String studyId, Boolean negateFilters);
}
