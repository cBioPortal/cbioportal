package org.cbioportal.legacy.persistence;

import java.util.List;
import org.cbioportal.legacy.model.AlterationDriverAnnotation;
import org.springframework.cache.annotation.Cacheable;

public interface AlterationDriverAnnotationRepository {

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<AlterationDriverAnnotation> getAlterationDriverAnnotations(
      List<String> molecularProfileCaseIdentifiers);
}
