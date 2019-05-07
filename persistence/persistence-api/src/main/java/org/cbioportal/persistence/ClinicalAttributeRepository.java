package org.cbioportal.persistence;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalAttributeCount;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface ClinicalAttributeRepository {

    @Cacheable("GeneralRepositoryCache")
    List<ClinicalAttribute> getAllClinicalAttributes(String projection, Integer pageSize, Integer pageNumber,
                                                     String sortBy, String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaClinicalAttributes();

    @Cacheable("GeneralRepositoryCache")
    ClinicalAttribute getClinicalAttribute(String studyId, String clinicalAttributeId);

    @Cacheable("GeneralRepositoryCache")
    List<ClinicalAttribute> getAllClinicalAttributesInStudy(String studyId, String projection, Integer pageSize,
                                                            Integer pageNumber, String sortBy, String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaClinicalAttributesInStudy(String studyId);

    @Cacheable("GeneralRepositoryCache")
    List<ClinicalAttribute> fetchClinicalAttributes(List<String> studyIds, String projection);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta fetchMetaClinicalAttributes(List<String> studyIds);

    @Cacheable("GeneralRepositoryCache")
    List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleIds(List<String> studyIds, List<String> sampleIds);

    @Cacheable("GeneralRepositoryCache")
    List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleListId(String sampleListId);
}
