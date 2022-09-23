package org.cbioportal.persistence.mybatis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.persistence.PatientRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ClinicalDataMyBatisRepository implements ClinicalDataRepository {

    @Autowired
    private ClinicalDataMapper clinicalDataMapper;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private OffsetCalculator offsetCalculator;

    private static final Log log = LogFactory.getLog(ClinicalDataMyBatisRepository.class);

    @Override
    public List<ClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId,
                                                                String attributeId, String projection,
                                                                Integer pageSize, Integer pageNumber,
                                                                String sortBy, String direction) {
        UUID uuid = UUID.randomUUID();
        log.warn("entry to getAllClinicalDataOfSampleInStudy() : " + uuid);

        List<ClinicalData> clinicalDataList = clinicalDataMapper.getSampleClinicalData(Arrays.asList(studyId), Arrays.asList(sampleId),
                attributeId != null ? Arrays.asList(attributeId) : null, projection, pageSize, 
                offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
        log.warn("exit from getAllClinicalDataOfSampleInStudy() : " + uuid);
        return clinicalDataList;
    }

    @Override
    public BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId) {
        UUID uuid = UUID.randomUUID();
        log.warn("entry to getMetaSampleClinicalData() : " + uuid);

        BaseMeta returnValue = clinicalDataMapper.getMetaSampleClinicalData(Arrays.asList(studyId), Arrays.asList(sampleId),
            attributeId != null ? Arrays.asList(attributeId) : null);
        log.warn("exit from getMetaSampleClinicalData() : " + uuid);
        return returnValue;
    }

    @Override
    public List<ClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId,
                                                                 String attributeId, String projection,
                                                                 Integer pageSize, Integer pageNumber,
                                                                 String sortBy, String direction) {

        UUID uuid = UUID.randomUUID();
        log.warn("entry to getAllClinicalDataOfPatientInStudy() : " + uuid);
        List<ClinicalData> returnValue = clinicalDataMapper.getPatientClinicalData(Arrays.asList(studyId), Arrays.asList(patientId),
                attributeId != null ? Arrays.asList(attributeId) : null, projection, pageSize, 
                offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
        log.warn("exit from getAllClinicalDataOfPatientInStudy() : " + uuid);
        return returnValue;
    }

    @Override
    public BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId) {
        UUID uuid = UUID.randomUUID();
        log.warn("entry to getMetaPatientClinicalData() : " + uuid);
        BaseMeta returnValue = clinicalDataMapper.getMetaPatientClinicalData(Arrays.asList(studyId), Arrays.asList(patientId),
                attributeId != null ? Arrays.asList(attributeId) : null);
        log.warn("exit from getMetaPatientClinicalData() : " + uuid);
        return returnValue;
    }

    @Override
    public List<ClinicalData> getAllClinicalDataInStudy(String studyId, String attributeId,
                                                        String clinicalDataType, String projection,
                                                        Integer pageSize, Integer pageNumber,
                                                        String sortBy, String direction) {
        UUID uuid = UUID.randomUUID();
        log.warn("entry to getAllClinicalDataInStudy() : " + uuid);
        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            List<ClinicalData> returnValue = clinicalDataMapper.getSampleClinicalData(Arrays.asList(studyId), null, 
                    attributeId != null ? Arrays.asList(attributeId) : null, projection, pageSize, 
                    offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
            log.warn("exit from getAllClinicalDataInStudy() : " + uuid);
            return returnValue;
        } else {
            List<ClinicalData> returnValue = clinicalDataMapper.getPatientClinicalData(Arrays.asList(studyId), null, 
                    attributeId != null ? Arrays.asList(attributeId) : null, projection, pageSize, 
                    offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
            log.warn("exit from getAllClinicalDataInStudy() : " + uuid);
            return returnValue;
        }
    }

    @Override
    public BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType) {
        BaseMeta baseMeta = new BaseMeta();
        UUID uuid = UUID.randomUUID();
        log.warn("entry to getMetaAllClinicalData() : " + uuid);
        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaSampleClinicalData(Arrays.asList(studyId), null,
                attributeId != null ? Arrays.asList(attributeId) : null).getTotalCount());
        } else {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaPatientClinicalData(Arrays.asList(studyId), null,
                attributeId != null ? Arrays.asList(attributeId) : null).getTotalCount());
        }
        log.warn("exit from getMetaAllClinicalData() : " + uuid);
        return baseMeta;
    }

    @Override
    public List<ClinicalData> fetchAllClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds,
                                                          String clinicalDataType, String projection) {
        UUID uuid = UUID.randomUUID();
        String attributeList = "";
        if (attributeIds.size() > 0) {
            attributeList = String.format(" attrs=('%s')", String.join("','", attributeIds)); 
        }
        log.warn("entry to fetchAllClinicalDataInStudy() : " + uuid + attributeList);
        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            List<ClinicalData> clinicalDataList = clinicalDataMapper.getSampleClinicalData(Arrays.asList(studyId), ids, attributeIds,
                projection, 0, 0, null, null);
            log.warn("exit from fetchAllClinicalDataInStudy() : " + uuid + attributeList);
            return clinicalDataList;
        } else {
            List<ClinicalData> clinicalDataList = clinicalDataMapper.getPatientClinicalData(Arrays.asList(studyId), ids, attributeIds,
                projection, 0, 0, null, null);
            log.warn("exit from fetchAllClinicalDataInStudy() : " + uuid + attributeList);
            return clinicalDataList;
        }
    }

    @Override
    public BaseMeta fetchMetaClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds,
                                                 String clinicalDataType) {

        BaseMeta baseMeta = new BaseMeta();
        UUID uuid = UUID.randomUUID();
        String attributeList = "";
        if (attributeIds.size() > 0) {
            attributeList = String.format(" attrs=('%s')", String.join("','", attributeIds)); 
        }
        log.warn("entry to fetchMetaClinicalDataInStudy() : " + uuid + attributeList);
        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaSampleClinicalData(Arrays.asList(studyId), ids,
                attributeIds).getTotalCount());
        } else {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaPatientClinicalData(Arrays.asList(studyId), ids,
                attributeIds).getTotalCount());
        }
        log.warn("exit from fetchMetaClinicalDataInStudy() : " + uuid + attributeList);
        return baseMeta;
    }

    @Override
    public List<ClinicalData> fetchClinicalData(List<String> studyIds, List<String> ids,
                                                List<String> attributeIds, String clinicalDataType,
                                                String projection) {
        UUID uuid = UUID.randomUUID();
        String attributeList = "";
        if (attributeIds.size() > 0) {
            attributeList = String.format(" attrs=('%s')", String.join("','", attributeIds)); 
        }
        log.warn("entry to fetchClinicalData() : " + uuid + attributeList);
        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            List<ClinicalData> returnValue = clinicalDataMapper.getSampleClinicalData(studyIds, ids, attributeIds, projection, 0, 0, null, null);
            log.warn("exit from fetchClinicalData() : " + uuid + attributeList);
            return returnValue;
        } else {
            List<ClinicalData> returnValue = clinicalDataMapper.getPatientClinicalData(studyIds, ids, attributeIds, projection, 0, 0, null, null);
            log.warn("exit from fetchClinicalData() : " + uuid + attributeList);
            return returnValue;
        }
    }

    @Override
    public BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds,
                                          String clinicalDataType) {

        BaseMeta baseMeta = new BaseMeta();
        UUID uuid = UUID.randomUUID();
        String attributeList = "";
        if (attributeIds.size() > 0) {
            attributeList = String.format(" attrs=('%s')", String.join("','", attributeIds)); 
        }
        log.warn("entry to fetchMetaClinicalData() : " + uuid + attributeList);
        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
                baseMeta.setTotalCount(clinicalDataMapper.getMetaSampleClinicalData(studyIds, ids, attributeIds)
                .getTotalCount());
        } else {
                baseMeta.setTotalCount(clinicalDataMapper.getMetaPatientClinicalData(studyIds, ids, attributeIds)
                .getTotalCount());
        }
        log.warn("exit from fetchMetaClinicalData() : " + uuid + attributeList);
        return baseMeta;
    }

    @Override
    public List<ClinicalDataCount> fetchClinicalDataCounts(List<String> studyIds, List<String> sampleIds,
            List<String> attributeIds, String clinicalDataType, String projection) {
        UUID uuid = UUID.randomUUID();
        String attributeList = "";
        if (attributeIds.size() > 0) {
            attributeList = String.format(" attrs=('%s')", String.join("','", attributeIds)); 
        }
        log.warn("entry to fetchClinicalDataCounts() : " + uuid + attributeList);
        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            List<ClinicalDataCount> returnValue = clinicalDataMapper.fetchSampleClinicalDataCounts(studyIds, sampleIds, attributeIds);
            log.warn("exit from fetchClinicalDataCounts() : " + uuid + attributeList);
            return returnValue;
        } else {
            List<Patient> patients = patientRepository.getPatientsOfSamples(studyIds, sampleIds);
            List<String> patientStudyIds = new ArrayList<>();
            patients.forEach(p -> patientStudyIds.add(p.getCancerStudyIdentifier()));
            List<ClinicalDataCount> returnValue = clinicalDataMapper.fetchPatientClinicalDataCounts(patientStudyIds, 
                patients.stream().map(Patient::getStableId).collect(Collectors.toList()), attributeIds, projection);
            log.warn("exit from fetchClinicalDataCounts() : " + uuid + attributeList);
            return returnValue;
        }
    }

    @Override
    public List<ClinicalData> getPatientClinicalDataDetailedToSample(List<String> studyIds, List<String> patientIds,
            List<String> attributeIds) {
        UUID uuid = UUID.randomUUID();
        String attributeList = "";
        if (attributeIds.size() > 0) {
            attributeList = String.format(" attrs=('%s')", String.join("','", attributeIds)); 
        }
        log.warn("entry to getPatientClinicalDataDetailedToSample() : " + uuid + attributeList);
        List<ClinicalData> returnValue = clinicalDataMapper.getPatientClinicalDataDetailedToSample(studyIds, patientIds, attributeIds, "SUMMARY",
                0, 0, null, null);
        log.warn("exit from getPatientClinicalDataDetailedToSample() : " + uuid + attributeList);
        return returnValue;
    }
}
