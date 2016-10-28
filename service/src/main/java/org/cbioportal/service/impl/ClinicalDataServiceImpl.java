package org.cbioportal.service.impl;

import org.cbioportal.model.PatientClinicalData;
import org.cbioportal.model.SampleClinicalData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.model.summary.ClinicalDataSummary;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.service.ClinicalDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ClinicalDataServiceImpl implements ClinicalDataService {

    public static final String SAMPLE_CLINICAL_DATA_TYPE = "SAMPLE";
    @Autowired
    private ClinicalDataRepository clinicalDataRepository;

    @Override
    public List<SampleClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId,
                                                                      String attributeId, String projection,
                                                                      Integer pageSize, Integer pageNumber,
                                                                      String sortBy, String direction) {

        return clinicalDataRepository.getAllClinicalDataOfSampleInStudy(Arrays.asList(studyId), Arrays.asList(sampleId),
                attributeId, projection, pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId) {

        return clinicalDataRepository.getMetaSampleClinicalData(Arrays.asList(studyId), Arrays.asList(sampleId),
                attributeId);
    }

    @Override
    public List<PatientClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId,
                                                                        String attributeId, String projection,
                                                                        Integer pageSize, Integer pageNumber,
                                                                        String sortBy, String direction) {

        return clinicalDataRepository.getAllClinicalDataOfPatientInStudy(Arrays.asList(studyId),
                Arrays.asList(patientId), attributeId, projection, pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId) {

        return clinicalDataRepository.getMetaPatientClinicalData(Arrays.asList(studyId), Arrays.asList(patientId),
                attributeId);
    }

    @Override
    public List<? extends ClinicalDataSummary> getAllClinicalDataInStudy(String studyId, String attributeId,
                                                                         String clinicalDataType, String projection,
                                                                         Integer pageSize, Integer pageNumber,
                                                                         String sortBy, String direction) {

        if (clinicalDataType.equals(SAMPLE_CLINICAL_DATA_TYPE)) {
            return clinicalDataRepository.getAllClinicalDataOfSampleInStudy(Arrays.asList(studyId), null, attributeId,
                    projection, pageSize, pageNumber, sortBy, direction);
        } else {
            return clinicalDataRepository.getAllClinicalDataOfPatientInStudy(Arrays.asList(studyId), null, attributeId,
                    projection, pageSize, pageNumber, sortBy, direction);
        }
    }

    @Override
    public BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType) {

        BaseMeta baseMeta = new BaseMeta();

        if (clinicalDataType.equals(SAMPLE_CLINICAL_DATA_TYPE)) {
            baseMeta.setTotalCount(clinicalDataRepository.getMetaSampleClinicalData(Arrays.asList(studyId), null,
                    attributeId).getTotalCount());
        } else {
            baseMeta.setTotalCount(clinicalDataRepository.getMetaPatientClinicalData(Arrays.asList(studyId), null,
                    attributeId).getTotalCount());
        }

        return baseMeta;
    }

    @Override
    public List<? extends ClinicalDataSummary> fetchClinicalData(List<String> studyIds, List<String> ids,
                                                                 String attributeId, String clinicalDataType,
                                                                 String projection) {

        if (clinicalDataType.equals(SAMPLE_CLINICAL_DATA_TYPE)) {
            return clinicalDataRepository.getAllClinicalDataOfSampleInStudy(studyIds, ids, attributeId, projection, 0,
                    0, null, null);
        } else {
            return clinicalDataRepository.getAllClinicalDataOfPatientInStudy(studyIds, ids, attributeId, projection, 0,
                    0, null, null);
        }
    }

    @Override
    public BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, String attributeId,
                                          String clinicalDataType) {

        BaseMeta baseMeta = new BaseMeta();

        if (clinicalDataType.equals(SAMPLE_CLINICAL_DATA_TYPE)) {
            baseMeta.setTotalCount(clinicalDataRepository.getMetaSampleClinicalData(studyIds, ids, attributeId)
                    .getTotalCount());
        } else {
            baseMeta.setTotalCount(clinicalDataRepository.getMetaPatientClinicalData(studyIds, ids, attributeId)
                    .getTotalCount());
        }

        return baseMeta;
    }
}
