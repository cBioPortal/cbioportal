package org.cbioportal.persistence;

import org.cbioportal.model.AlterationDriverAnnotation;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface AlterationDriverAnnotationRepository {

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<AlterationDriverAnnotation> getAlterationDriverAnnotations(List<String> molecularProfileCaseIdentifiers);

}