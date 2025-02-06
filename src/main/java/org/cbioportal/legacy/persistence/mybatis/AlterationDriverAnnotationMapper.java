package org.cbioportal.legacy.persistence.mybatis;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.AlterationDriverAnnotation;

import java.util.List;

public interface AlterationDriverAnnotationMapper {

    List<AlterationDriverAnnotation> getAlterationDriverAnnotations(@Param("molecularProfileIds") List<String> molecularProfileIds);
    
}