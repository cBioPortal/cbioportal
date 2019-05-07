package org.cbioportal.persistence;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface ClinicalDataRepository {

    @Cacheable("GeneralRepositoryCache")
    List<ClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId, String attributeId,
                                                         String projection, Integer pageSize, Integer pageNumber,
                                                         String sortBy, String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId);

    @Cacheable("GeneralRepositoryCache")
    List<ClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId, String attributeId,
                                                          String projection, Integer pageSize,
                                                          Integer pageNumber, String sortBy, String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId);

    @Cacheable("GeneralRepositoryCache")
    List<ClinicalData> getAllClinicalDataInStudy(String studyId, String attributeId,
                                                 String clinicalDataType, String projection,
                                                 Integer pageSize, Integer pageNumber, String sortBy,
                                                 String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType);

    @Cacheable("GeneralRepositoryCache")
    List<ClinicalData> fetchAllClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds, 
                                                   String clinicalDataType, String projection);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta fetchMetaClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds, 
                                          String clinicalDataType);

    @Cacheable("GeneralRepositoryCache")
    List<ClinicalData> fetchClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds,
                                         String clinicalDataType, String projection);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds,
                                   String clinicalDataType);

    @Cacheable("GeneralRepositoryCache")
    List<ClinicalDataCount> fetchClinicalDataCounts(List<String> studyIds, List<String> sampleIds, List<String> attributeIds, 
        String clinicalDataType);
}
