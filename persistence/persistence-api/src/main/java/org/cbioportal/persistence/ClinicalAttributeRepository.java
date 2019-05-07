package org.cbioportal.persistence;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalAttributeCount;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface ClinicalAttributeRepository {

    @Cacheable("RepositoryCache")
    List<ClinicalAttribute> getAllClinicalAttributes(String projection, Integer pageSize, Integer pageNumber,
                                                     String sortBy, String direction);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaClinicalAttributes();

    @Cacheable("RepositoryCache")
    ClinicalAttribute getClinicalAttribute(String studyId, String clinicalAttributeId);

    @Cacheable("RepositoryCache")
    List<ClinicalAttribute> getAllClinicalAttributesInStudy(String studyId, String projection, Integer pageSize,
                                                            Integer pageNumber, String sortBy, String direction);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaClinicalAttributesInStudy(String studyId);

    @Cacheable("RepositoryCache")
    List<ClinicalAttribute> fetchClinicalAttributes(List<String> studyIds, String projection);

    @Cacheable("RepositoryCache")
    BaseMeta fetchMetaClinicalAttributes(List<String> studyIds);

    @Cacheable("RepositoryCache")
    List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleIds(List<String> studyIds, List<String> sampleIds);

    @Cacheable("RepositoryCache")
    List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleListId(String sampleListId);
}
