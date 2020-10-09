package org.cbioportal.persistence;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.Treatment;
import org.springframework.cache.annotation.Cacheable;

public interface TreatmentRepository {
    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    public Map<String, List<Treatment>> getTreatmentsByPatientId(List<String> sampleIds, List<String> studyIds);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    public Map<String, List<ClinicalEventSample>> getSamplesByPatientId(List<String> sampleIds, List<String> studyIds);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    public Set<String> getAllUniqueTreatments(List<String> sampleIds, List<String> studyIds);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    public Integer getTreatmentCount(List<String> studies);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    public Integer getSampleCount(List<String> studies);
}
