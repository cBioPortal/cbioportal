package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface CancerTypeMapper {

    List<TypeOfCancer> getAllCancerTypes(String projection, Integer limit, Integer offset, String sortBy, 
                                         String direction);

    BaseMeta getMetaCancerTypes();

    TypeOfCancer getCancerType(String cancerTypeId, String projection);
}
