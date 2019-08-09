package org.cbioportal.persistence;

import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface ClinicalEventRepository {

    @Cacheable("GeneralRepositoryCache")
    List<ClinicalEvent> getAllClinicalEventsOfPatientInStudy(String studyId, String patientId, String projection,
                                                             Integer pageSize, Integer pageNumber, String sortBy,
                                                             String direction);
    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaPatientClinicalEvents(String studyId, String patientId);
    
    @Cacheable("GeneralRepositoryCache")
    List<ClinicalEventData> getDataOfClinicalEvents(List<Integer> clinicalEventIds);

    @Cacheable("GeneralRepositoryCache")
    List<ClinicalEvent> getAllClinicalEventsInStudy(String studyId, String projection, Integer pageSize,
                                                    Integer pageNumber, String sortBy, String direction);
    
    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaClinicalEvents(String studyId);
}
