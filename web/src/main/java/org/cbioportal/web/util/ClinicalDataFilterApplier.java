package org.cbioportal.web.util;

import org.apache.commons.collections.map.MultiKeyMap;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.Patient;
import org.cbioportal.model.Sample;
import org.cbioportal.model.ClinicalDataCountItem.ClinicalDataType;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.SampleService;
import org.cbioportal.web.parameter.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ClinicalDataFilterApplier<T extends ClinicalDataFilter>
{
    private PatientService patientService;
    private ClinicalDataService clinicalDataService;
    private SampleService sampleService;
    protected StudyViewFilterUtil studyViewFilterUtil;

    public ClinicalDataFilterApplier(PatientService patientService, 
                                     ClinicalDataService clinicalDataService, 
                                     SampleService sampleService,
                                     StudyViewFilterUtil studyViewFilterUtil) 
    {
        this.patientService = patientService;
        this.clinicalDataService = clinicalDataService;
        this.sampleService = sampleService;
        this.studyViewFilterUtil = studyViewFilterUtil;
    }

    public List<SampleIdentifier> apply(List<SampleIdentifier> sampleIdentifiers,
                                        List<T> attributes,
                                        ClinicalDataType filterClinicalDataType,
                                        Boolean negateFilters) {
        List<ClinicalData> clinicalDataList = new ArrayList<>();
        if (!attributes.isEmpty() && !sampleIdentifiers.isEmpty()) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);
            List<Patient> patients = patientService.getPatientsOfSamples(studyIds, sampleIds);
            List<String> patientIds = patients.stream().map(Patient::getStableId).collect(Collectors.toList());
            List<String> studyIdsOfPatients = patients.stream().map(Patient::getCancerStudyIdentifier).collect(Collectors.toList());
            clinicalDataList = clinicalDataService.fetchClinicalData(filterClinicalDataType.equals(ClinicalDataType.PATIENT) ?
                    studyIdsOfPatients : studyIds, filterClinicalDataType.equals(ClinicalDataType.PATIENT) ? patientIds : sampleIds,
                attributes.stream().map(ClinicalDataFilter::getAttributeId).collect(Collectors.toList()),
                filterClinicalDataType.name(), Projection.SUMMARY.name());

            clinicalDataList.forEach(c -> {
                if (c.getAttrValue().toUpperCase().equals("NAN") || c.getAttrValue().toUpperCase().equals("N/A")) {
                    c.setAttrValue("NA");
                }
            });

            MultiKeyMap clinicalDataMap = new MultiKeyMap();
            for (ClinicalData clinicalData : clinicalDataList) {
                if (filterClinicalDataType.equals(ClinicalDataType.PATIENT)) {
                    if (clinicalDataMap.containsKey(clinicalData.getPatientId(), clinicalData.getStudyId())) {
                        ((List<ClinicalData>)clinicalDataMap.get(clinicalData.getPatientId(), clinicalData.getStudyId())).add(clinicalData);
                    } else {
                        List<ClinicalData> clinicalDatas = new ArrayList<>();
                        clinicalDatas.add(clinicalData);
                        clinicalDataMap.put(clinicalData.getPatientId(), clinicalData.getStudyId(), clinicalDatas);
                    }
                } else {
                    if (clinicalDataMap.containsKey(clinicalData.getSampleId(), clinicalData.getStudyId())) {
                        ((List<ClinicalData>)clinicalDataMap.get(clinicalData.getSampleId(), clinicalData.getStudyId())).add(clinicalData);
                    } else {
                        List<ClinicalData> clinicalDatas = new ArrayList<>();
                        clinicalDatas.add(clinicalData);
                        clinicalDataMap.put(clinicalData.getSampleId(), clinicalData.getStudyId(), clinicalDatas);
                    }
                }
            }

            List<String> ids = new ArrayList<>();
            List<String> studyIdsOfIds = new ArrayList<>();
            int index = 0;
            for (String entityId : filterClinicalDataType.equals(ClinicalDataType.PATIENT) ? patientIds : sampleIds) {
                String studyId = filterClinicalDataType.equals(ClinicalDataType.PATIENT) ? studyIdsOfPatients.get(index) : studyIds.get(index);

                int count = apply(attributes, clinicalDataMap, entityId, studyId, negateFilters);

                if (count == attributes.size()) {
                    ids.add(entityId);
                    studyIdsOfIds.add(studyId);
                }
                index++;
            }

            if (filterClinicalDataType.equals(ClinicalDataType.PATIENT)) {
                if (!ids.isEmpty()) {
                    List<Sample> samples = sampleService.getSamplesOfPatientsInMultipleStudies(studyIdsOfIds, ids,
                        Projection.ID.name());
                    ids = samples.stream().map(Sample::getStableId).collect(Collectors.toList());
                    studyIdsOfIds = samples.stream().map(Sample::getCancerStudyIdentifier).collect(Collectors.toList());
                }
            }

            Set<String> idsSet = new HashSet<>(ids);
            idsSet.retainAll(new HashSet<>(sampleIds));
            List<SampleIdentifier> newSampleIdentifiers = new ArrayList<>();
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
    protected abstract Integer apply(List<T> attributes, MultiKeyMap clinicalDataMap, String entityId, String studyId, Boolean negateFilters);
}
