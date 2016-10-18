package org.cbioportal.service.impl;

import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CancerTypeRepository;
import org.cbioportal.service.CancerTypeService;
import org.cbioportal.service.exception.CancerTypeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CancerTypeServiceImpl implements CancerTypeService {

    @Autowired
    private CancerTypeRepository cancerTypeRepository;

    @Override
    public List<TypeOfCancer> getAllCancerTypes(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                                String direction) {

        return cancerTypeRepository.getAllCancerTypes(projection, pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaCancerTypes() {
        return cancerTypeRepository.getMetaCancerTypes();
    }

    @Override
    public TypeOfCancer getCancerType(String cancerTypeId) throws CancerTypeNotFoundException {

        TypeOfCancer typeOfCancer = cancerTypeRepository.getCancerType(cancerTypeId);
        if (typeOfCancer == null) {
            throw new CancerTypeNotFoundException(cancerTypeId);
        }

        return typeOfCancer;
    }
}
