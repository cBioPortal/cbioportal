package org.cbioportal.persistence;

import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface CancerTypeRepository {

    List<TypeOfCancer> getAllCancerTypes(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                         String direction);

    BaseMeta getMetaCancerTypes();

    TypeOfCancer getCancerType(String cancerTypeId);
}
