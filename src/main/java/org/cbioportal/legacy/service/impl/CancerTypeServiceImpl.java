package org.cbioportal.legacy.service.impl;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.model.TypeOfCancer;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.CancerTypeRepository;
import org.cbioportal.legacy.service.CancerTypeService;
import org.cbioportal.legacy.service.exception.CancerTypeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CancerTypeServiceImpl implements CancerTypeService {

  private static final String TISSUE = "tissue";

  @Autowired private CancerTypeRepository cancerTypeRepository;

  private Map<String, TypeOfCancer> primarySiteMap = new HashMap<>();

  @PostConstruct
  public void initPrimarySiteMap() {

    List<TypeOfCancer> allCancerTypes = getAllCancerTypes("SUMMARY", null, null, null, null);

    for (TypeOfCancer typeOfCancer : allCancerTypes) {

      if (!typeOfCancer.getTypeOfCancerId().equals(TISSUE)) {
        TypeOfCancer primarySite = getParent(allCancerTypes, typeOfCancer);
        primarySiteMap.put(typeOfCancer.getTypeOfCancerId(), primarySite);
      }
    }
  }

  @Override
  public List<TypeOfCancer> getAllCancerTypes(
      String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {

    return cancerTypeRepository.getAllCancerTypes(
        projection, pageSize, pageNumber, sortBy, direction);
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

  @Override
  public Map<String, TypeOfCancer> getPrimarySiteMap() {
    return primarySiteMap;
  }

  private TypeOfCancer getParent(List<TypeOfCancer> allCancerTypes, TypeOfCancer typeOfCancer) {

    if (typeOfCancer.getParent().equals(TISSUE)) {
      return typeOfCancer;
    }

    return getParent(
        allCancerTypes,
        allCancerTypes.stream()
            .filter(c -> c.getTypeOfCancerId().equals(typeOfCancer.getParent()))
            .findFirst()
            .get());
  }
}
