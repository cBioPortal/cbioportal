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
import org.cbioportal.persistence.mybatis.client.AdhocFlightClient;
import org.cbioportal.persistence.mybatis.client.ArrowFlightClient;
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
        try {
            AdhocFlightClient client = ArrowFlightClient.getClient();
            // initial query           
            StringBuilder sb = new StringBuilder("select * from \"cbioportal_prototype\".\"database_2022_06\".\"clinical_data_sample_select_from_set\"");
            if (sampleId == null) {
                sb.append(String.format(" where studyId = '%s'", studyId));
            } else {
                sb.append(String.format(" where studyId = '%s' and sampleId in ('%s')", studyId, sampleId));
            }
            if (attributeId != null) {
                sb.append(String.format(" and attrId in ('%s')", attributeId));
            }
            List<ClinicalData> clinicalDataList = client.runQuery(sb.toString(), null, ClinicalData.class);
            log.warn("exit from getAllClinicalDataOfSampleInStudy() : " + uuid);
            return clinicalDataList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            log.warn("exit from getAllClinicalDataOfSampleInStudy() : " + uuid);
        }
        return null;
    }

    @Override
    public BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId) {

        UUID uuid = UUID.randomUUID();
        log.warn("entry to getMetaSampleClinicalData() : " + uuid + "\n");
        BaseMeta returnValue = clinicalDataMapper.getMetaSampleClinicalData(Arrays.asList(studyId), Arrays.asList(sampleId),
                attributeId != null ? Arrays.asList(attributeId) : null);
        log.warn("exit from getMetaSampleClinicalData() : " + uuid + "\n");
        return returnValue;
    }

    @Override
    public List<ClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId,
                                                                 String attributeId, String projection,
                                                                 Integer pageSize, Integer pageNumber,
                                                                 String sortBy, String direction) {

        UUID uuid = UUID.randomUUID();
        log.warn("entry to getAllClinicalDataOfPatientInStudy() : " + uuid + "\n");
        List<ClinicalData> returnValue = clinicalDataMapper.getPatientClinicalData(Arrays.asList(studyId), Arrays.asList(patientId),
                attributeId != null ? Arrays.asList(attributeId) : null, projection, pageSize,
                offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
        log.warn("exit from getAllClinicalDataOfPatientInStudy() : " + uuid + "\n");
        return returnValue;
    }

    @Override
    public BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId) {

        UUID uuid = UUID.randomUUID();
        log.warn("entry to getMetaPatientClinicalData() : " + uuid + "\n");
        BaseMeta returnValue = clinicalDataMapper.getMetaPatientClinicalData(Arrays.asList(studyId), Arrays.asList(patientId),
                attributeId != null ? Arrays.asList(attributeId) : null);
        log.warn("exit from getMetaPatientClinicalData() : " + uuid + "\n");
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
            try {
                AdhocFlightClient client = ArrowFlightClient.getClient();
                StringBuilder sb = new StringBuilder("select * from \"cbioportal_prototype\".\"database_2022_06\".\"clinical_data_sample_select_from_set\"");
                sb.append(String.format(" where studyId = '%s'", studyId));
                if (attributeId != null) {
                    sb.append(String.format(" and attrId in ('%s')", attributeId));
                }
                List<ClinicalData> clinicalDataList = client.runQuery(sb.toString(), null, ClinicalData.class);
                return clinicalDataList;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                log.warn("exit from getAllClinicalDataInStudy() : " + uuid);
            }
            return null;
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
            try {
                AdhocFlightClient client = ArrowFlightClient.getClient();
                StringBuilder sb = new StringBuilder("select * from \"cbioportal_prototype\".\"database_2022_06\".\"clinical_data_sample_select_from_set\"");
                if (ids == null) {
                    sb.append(String.format(" where studyId = '%s'", studyId));
                } else {
                    sb.append(String.format(" where studyId = '%s' and sampleId in (", studyId));
                    for (int i = 0; i < ids.size(); i++) {
                        if (i == 0) {
                            sb.append(String.format("'%s'", ids.get(i)));
                        } else {
                            sb.append(String.format(",'%s'", ids.get(i)));
                        }
                    }
                    sb.append(")");
                }
                if (attributeIds != null) {
                    sb.append(" and attrId in (");
                    for (int i = 0; i < attributeIds.size(); i++) {
                        if (i == 0) {
                            sb.append(String.format("'%s'", attributeIds.get(i)));
                        } else {
                            sb.append(String.format(",'%s'", attributeIds.get(i)));
                        }
                    }
                    sb.append(")");
                }
                List<ClinicalData> clinicalDataList = client.runQuery(sb.toString(), null, ClinicalData.class);
                return clinicalDataList;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                log.warn("exit from fetchAllClinicalDataInStudy() : " + uuid + attributeList);
            }
            return null;
        } else {
            List<ClinicalData> returnValue = clinicalDataMapper.getPatientClinicalData(Arrays.asList(studyId), ids, attributeIds,
                    projection, 0, 0, null, null);
            log.warn("exit from fetchAllClinicalDataInStudy() : " + uuid + attributeList);
            return returnValue;
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
            try {
                AdhocFlightClient client = ArrowFlightClient.getClient();
                StringBuilder sb = new StringBuilder("select * from \"cbioportal_prototype\".\"database_2022_06\".\"clinical_data_sample_select_from_set\"");
                if (ids == null) {
                    sb.append(String.format(" where studyId = '%s'", studyIds.get(0)));
                } else {
                    if (studyIds.stream().distinct().count() == 1) {
                        sb.append(String.format(" where studyId = '%s'", studyIds.get(0)));
                    } else {
                        sb.append(" where studyId in (");
                        for (int i = 0; i < studyIds.size(); i++) {
                            if (i == 0) {
                                sb.append(String.format("'%s'", studyIds.get(i)));
                            } else {
                                sb.append(String.format(",'%s'", studyIds.get(i)));
                            }
                        }
                        sb.append(")");
                    }
                    sb.append(" and sampleId in (");
                    for (int i = 0; i < ids.size(); i++) {
                        if (i == 0) {
                            sb.append(String.format("'%s'", ids.get(i)));
                        } else {
                            sb.append(String.format(",'%s'", ids.get(i)));
                        }
                    }
                    sb.append(")");
                }
                if (attributeIds != null) {
                    sb.append(" and attrId in (");
                    for (int i = 0; i < attributeIds.size(); i++) {
                        if (i == 0) {
                            sb.append(String.format("'%s'", attributeIds.get(i)));
                        } else {
                            sb.append(String.format(",'%s'", attributeIds.get(i)));
                        }
                    }
                    sb.append(")");
                }
                List<ClinicalData> clinicalDataList = client.runQuery(sb.toString(), null, ClinicalData.class);
                return clinicalDataList;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                log.warn("exit from fetchClinicalData() : " + uuid + attributeList);
            }
            return null;
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
