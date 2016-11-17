package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class ClinicalDataMyBatisRepository implements ClinicalDataRepository {

    @Autowired
    private ClinicalDataMapper clinicalDataMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<ClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId,
                                                                      String attributeId, String projection,
                                                                      Integer pageSize, Integer pageNumber,
                                                                      String sortBy, String direction) {

        return clinicalDataMapper.getSampleClinicalData(Arrays.asList(studyId), Arrays.asList(sampleId), attributeId,
                projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId) {
        return clinicalDataMapper.getMetaSampleClinicalData(Arrays.asList(studyId), Arrays.asList(sampleId),
                attributeId);
    }

    @Override
    public List<ClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId,
                                                                        String attributeId, String projection,
                                                                        Integer pageSize, Integer pageNumber,
                                                                        String sortBy, String direction) {

        return clinicalDataMapper.getPatientClinicalData(Arrays.asList(studyId), Arrays.asList(patientId), attributeId,
                projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId) {
        return clinicalDataMapper.getMetaPatientClinicalData(Arrays.asList(studyId), Arrays.asList(patientId),
                attributeId);
    }

    @Override
    public List<ClinicalData> getAllClinicalDataInStudy(String studyId, String attributeId,
                                                        String clinicalDataType, String projection,
                                                        Integer pageSize, Integer pageNumber,
                                                        String sortBy, String direction) {

        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            return clinicalDataMapper.getSampleClinicalData(Arrays.asList(studyId), null, attributeId,
                    projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
        } else {
            return clinicalDataMapper.getPatientClinicalData(Arrays.asList(studyId), null, attributeId,
                    projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
        }
    }

    @Override
    public BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType) {

        BaseMeta baseMeta = new BaseMeta();

        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaSampleClinicalData(Arrays.asList(studyId), null,
                    attributeId).getTotalCount());
        } else {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaPatientClinicalData(Arrays.asList(studyId), null,
                    attributeId).getTotalCount());
        }

        return baseMeta;
    }

    @Override
    public List<ClinicalData> fetchClinicalData(List<String> studyIds, List<String> ids,
                                                                 String attributeId, String clinicalDataType,
                                                                 String projection) {

        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            return clinicalDataMapper.getSampleClinicalData(studyIds, ids, attributeId, projection, 0, 0, null, null);
        } else {
            return clinicalDataMapper.getPatientClinicalData(studyIds, ids, attributeId, projection, 0, 0, null, null);
        }
    }

    @Override
    public BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, String attributeId,
                                          String clinicalDataType) {

        BaseMeta baseMeta = new BaseMeta();

        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaSampleClinicalData(studyIds, ids, attributeId)
                    .getTotalCount());
        } else {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaPatientClinicalData(studyIds, ids, attributeId)
                    .getTotalCount());
        }

        return baseMeta;
    }
}
