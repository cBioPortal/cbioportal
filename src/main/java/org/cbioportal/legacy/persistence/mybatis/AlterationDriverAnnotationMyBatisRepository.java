package org.cbioportal.legacy.persistence.mybatis;

import java.util.Collections;
import java.util.List;
import org.cbioportal.legacy.model.AlterationDriverAnnotation;
import org.cbioportal.legacy.persistence.AlterationDriverAnnotationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AlterationDriverAnnotationMyBatisRepository
    implements AlterationDriverAnnotationRepository {

  @Autowired private AlterationDriverAnnotationMapper alterationDriverAnnotationMapper;

  @Override
  public List<AlterationDriverAnnotation> getAlterationDriverAnnotations(
      List<String> molecularProfileIds) {

    if (molecularProfileIds == null || molecularProfileIds.isEmpty()) {
      return Collections.emptyList();
    }

    return alterationDriverAnnotationMapper.getAlterationDriverAnnotations(molecularProfileIds);
  }
}
