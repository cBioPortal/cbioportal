package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CancerTypeRepository;
import org.cbioportal.persistence.mybatis.tool.Constants;
import org.cbioportal.persistence.mybatis.tool.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CancerTypeMyBatisRepository implements CancerTypeRepository {

    @Autowired
    private CancerTypeMapper cancerTypeMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<TypeOfCancer> getAllCancerTypes(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                                String direction) {

        return cancerTypeMapper.getAllCancerTypes(projection, pageSize,
                offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaCancerTypes() {
        return cancerTypeMapper.getMetaCancerTypes();
    }

    @Override
    public TypeOfCancer getCancerType(String cancerTypeId) {
        return cancerTypeMapper.getCancerType(cancerTypeId, Constants.DETAILED_PROJECTION);
    }
}
