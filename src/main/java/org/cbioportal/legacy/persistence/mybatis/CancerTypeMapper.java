package org.cbioportal.legacy.persistence.mybatis;

import org.cbioportal.legacy.model.TypeOfCancer;
import org.cbioportal.legacy.model.meta.BaseMeta;

import java.util.List;

public interface CancerTypeMapper {

    List<TypeOfCancer> getAllCancerTypes(String projection, Integer limit, Integer offset, String sortBy, 
                                         String direction);

    BaseMeta getMetaCancerTypes();

    TypeOfCancer getCancerType(String cancerTypeId, String projection);
}
