package org.cbioportal.legacy.persistence;

import org.cbioportal.legacy.model.AlterationDriverAnnotation;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface AlterationDriverAnnotationRepository {

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<AlterationDriverAnnotation> getAlterationDriverAnnotations(List<String> molecularProfileCaseIdentifiers);

}