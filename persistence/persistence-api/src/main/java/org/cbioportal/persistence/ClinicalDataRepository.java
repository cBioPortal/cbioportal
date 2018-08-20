package org.cbioportal.persistence;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface ClinicalDataRepository {

    List<ClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId, String attributeId,
                                                         String projection, Integer pageSize, Integer pageNumber,
                                                         String sortBy, String direction);

    BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId);

    List<ClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId, String attributeId,
                                                          String projection, Integer pageSize,
                                                          Integer pageNumber, String sortBy, String direction);

    BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId);

    List<ClinicalData> getAllClinicalDataInStudy(String studyId, String attributeId,
                                                 String clinicalDataType, String projection,
                                                 Integer pageSize, Integer pageNumber, String sortBy,
                                                 String direction);

    BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType);

    List<ClinicalData> fetchAllClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds, 
                                                   String clinicalDataType, String projection);

    BaseMeta fetchMetaClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds, 
                                          String clinicalDataType);
    
    List<ClinicalData> fetchClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds,
                                         String clinicalDataType, String projection);

    BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds,
                                   String clinicalDataType);

    List<ClinicalDataCount> fetchClinicalDataCounts(List<String> studyIds, List<String> sampleIds, List<String> attributeIds, 
        String clinicalDataType);
}
