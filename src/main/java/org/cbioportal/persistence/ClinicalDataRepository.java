package org.cbioportal.persistence;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface ClinicalDataRepository {

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<ClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId, String attributeId,
                                                         String projection, Integer pageSize, Integer pageNumber,
                                                         String sortBy, String direction);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<ClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId, String attributeId,
                                                          String projection, Integer pageSize,
                                                          Integer pageNumber, String sortBy, String direction);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<ClinicalData> getAllClinicalDataInStudy(String studyId, String attributeId,
                                                 String clinicalDataType, String projection,
                                                 Integer pageSize, Integer pageNumber, String sortBy,
                                                 String direction);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<ClinicalData> fetchAllClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds, 
                                                   String clinicalDataType, String projection);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    BaseMeta fetchMetaClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds, 
                                          String clinicalDataType);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<ClinicalData> fetchClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds,
                                         String clinicalDataType, String projection);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds,
                                   String clinicalDataType);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<ClinicalDataCount> fetchClinicalDataCounts(List<String> studyIds, List<String> sampleIds, List<String> attributeIds, 
        String clinicalDataType, String projection);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<ClinicalData> getPatientClinicalDataDetailedToSample(List<String> studyIds, List<String> patientIds,
                                                              List<String> attributeIds);

    List<Integer> getVisibleSampleInternalIdsForClinicalTable(List<String> studyIds, List<String> sampleIds,
                                                              Integer pageSize, Integer pageNumber, String searchTerm,
                                                              String sortBy, String direction);

    List<ClinicalData> getSampleClinicalDataBySampleInternalIds(List<Integer> visibleSampleInternalIds);

    List<ClinicalData> getPatientClinicalDataBySampleInternalIds(List<Integer> visibleSampleInternalIds);
}
