package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.TypeOfCancer;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.CancerTypeNotFoundException;

import java.util.List;
import java.util.Map;

public interface CancerTypeService {

    List<TypeOfCancer> getAllCancerTypes(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                                String direction);

    BaseMeta getMetaCancerTypes();

    TypeOfCancer getCancerType(String cancerTypeId) throws CancerTypeNotFoundException;

    Map<String, TypeOfCancer> getPrimarySiteMap(); 
}
