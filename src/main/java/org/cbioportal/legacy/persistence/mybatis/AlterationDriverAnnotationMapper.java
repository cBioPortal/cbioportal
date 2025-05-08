package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.AlterationDriverAnnotation;

public interface AlterationDriverAnnotationMapper {

  List<AlterationDriverAnnotation> getAlterationDriverAnnotations(
      @Param("molecularProfileIds") List<String> molecularProfileIds);
}
