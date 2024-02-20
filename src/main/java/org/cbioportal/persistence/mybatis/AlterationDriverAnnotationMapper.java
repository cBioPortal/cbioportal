package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.AlterationDriverAnnotation;

public interface AlterationDriverAnnotationMapper {

  List<AlterationDriverAnnotation> getAlterationDriverAnnotations(
      @Param("molecularProfileIds") List<String> molecularProfileIds);
}
