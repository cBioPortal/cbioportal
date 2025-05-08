package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.TypeOfCancer;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.CancerTypeRepository;
import org.cbioportal.legacy.persistence.PersistenceConstants;
import org.cbioportal.legacy.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CancerTypeMyBatisRepository implements CancerTypeRepository {

  @Autowired private CancerTypeMapper cancerTypeMapper;

  @Override
  public List<TypeOfCancer> getAllCancerTypes(
      String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {

    return cancerTypeMapper.getAllCancerTypes(
        projection, pageSize, PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
  }

  @Override
  public BaseMeta getMetaCancerTypes() {
    return cancerTypeMapper.getMetaCancerTypes();
  }

  @Override
  public TypeOfCancer getCancerType(String cancerTypeId) {
    return cancerTypeMapper.getCancerType(cancerTypeId, PersistenceConstants.DETAILED_PROJECTION);
  }
}
