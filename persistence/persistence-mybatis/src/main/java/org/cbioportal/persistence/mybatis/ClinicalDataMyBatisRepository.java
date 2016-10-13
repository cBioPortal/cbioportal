package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.PatientClinicalData;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleClinicalData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ClinicalDataMyBatisRepository implements ClinicalDataRepository {

    @Autowired
    private ClinicalDataMapper clinicalDataMapper;

    @Override
    public List<SampleClinicalData> getAllClinicalDataOfSampleInStudy(List<String> studyIds, List<String> sampleIds,
                                                                      String attributeId, String projection,
                                                                      Integer pageSize, Integer pageNumber,
                                                                      String sortBy, String direction) {

        return clinicalDataMapper.getSampleClinicalData(studyIds, sampleIds, attributeId, projection, pageSize,
                pageSize == null || pageNumber == null ? null : pageSize * pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSampleClinicalData(List<String> studyIds, List<String> sampleIds, String attributeId) {
        return clinicalDataMapper.getMetaSampleClinicalData(studyIds, sampleIds, attributeId);
    }

    @Override
    public List<PatientClinicalData> getAllClinicalDataOfPatientInStudy(List<String> studyIds, List<String> patientIds,
                                                                        String attributeId, String projection,
                                                                        Integer pageSize, Integer pageNumber,
                                                                        String sortBy, String direction) {

        return clinicalDataMapper.getPatientClinicalData(studyIds, patientIds, attributeId, projection, pageSize,
                pageSize == null || pageNumber == null ? null : pageSize * pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaPatientClinicalData(List<String> studyIds, List<String> patientIds, String attributeId) {
        return clinicalDataMapper.getMetaPatientClinicalData(studyIds, patientIds, attributeId);
    }
}
