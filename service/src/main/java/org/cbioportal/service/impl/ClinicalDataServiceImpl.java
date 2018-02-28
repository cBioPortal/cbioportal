package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

        return clinicalDataRepository.fetchClinicalData(studyIds, ids, attributeIds, clinicalDataType, projection);
    }

    @Override
    public BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds, 
                                          String clinicalDataType) {

        return clinicalDataRepository.fetchMetaClinicalData(studyIds, ids, attributeIds, clinicalDataType);
    }
}
