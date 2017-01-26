package org.cbioportal.persistence.mybatis;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface CancerTypeMapper {

    List<TypeOfCancer> getAllCancerTypes(@Param("projection") String projection,
                                         @Param("limit") Integer limit,
                                         @Param("offset") Integer offset,
                                         @Param("sortBy") String sortBy,
                                         @Param("direction") String direction);

    BaseMeta getMetaCancerTypes();

    TypeOfCancer getCancerType(@Param("cancerTypeId") String cancerTypeId,
                               @Param("projection") String projection);
}
