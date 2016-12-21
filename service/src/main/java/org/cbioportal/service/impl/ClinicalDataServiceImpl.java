package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.service.ClinicalDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ClinicalDataServiceImpl implements ClinicalDataService {

    @Autowired
    private ClinicalDataRepository clinicalDataRepository;

    @Override
    public List<ClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId,
                                                                String attributeId, String projection,
                                                                Integer pageSize, Integer pageNumber,
                                                                String sortBy, String direction) {

        return clinicalDataRepository.getAllClinicalDataOfSampleInStudy(studyId, sampleId, attributeId, projection,
                pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId) {

        return clinicalDataRepository.getMetaSampleClinicalData(studyId, sampleId, attributeId);
    }

    @Override
    public List<ClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId,
                                                                        String attributeId, String projection,
                                                                        Integer pageSize, Integer pageNumber,
                                                                        String sortBy, String direction) {

        return clinicalDataRepository.getAllClinicalDataOfPatientInStudy(studyId, patientId, attributeId, projection,
                pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId) {

        return clinicalDataRepository.getMetaPatientClinicalData(studyId, patientId, attributeId);
    }

    @Override
    public List<ClinicalData> getAllClinicalDataInStudy(String studyId, String attributeId,
                                                                         String clinicalDataType, String projection,
                                                                         Integer pageSize, Integer pageNumber,
                                                                         String sortBy, String direction) {

        return clinicalDataRepository.getAllClinicalDataInStudy(studyId, attributeId, clinicalDataType, projection,
                pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType) {

        return clinicalDataRepository.getMetaAllClinicalData(studyId, attributeId, clinicalDataType);
    }

    @Override
    public List<ClinicalData> fetchClinicalData(List<String> studyIds, List<String> ids,
                                                                 String attributeId, String clinicalDataType,
                                                                 String projection) {

        return clinicalDataRepository.fetchClinicalData(studyIds, ids, attributeId, clinicalDataType, projection);
    }

    @Override
    public BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, String attributeId,
                                          String clinicalDataType) {

        return clinicalDataRepository.fetchMetaClinicalData(studyIds, ids, attributeId, clinicalDataType);
    }
}
