package org.cbioportal.persistence.clickhouse.mapper;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.AlterationDriverAnnotation;
import org.springframework.context.annotation.Profile;

import java.util.List;

public interface AlterationDriverAnnotationMapper {

    List<AlterationDriverAnnotation> getAlterationDriverAnnotations(@Param("molecularProfileIds") List<String> molecularProfileIds);
    
}