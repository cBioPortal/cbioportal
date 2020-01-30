package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CancerTypeRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CancerTypeMyBatisRepository implements CancerTypeRepository {
    @Autowired
    private CancerTypeMapper cancerTypeMapper;

    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<TypeOfCancer> getAllCancerTypes(
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    ) {
        return cancerTypeMapper.getAllCancerTypes(
            projection,
            pageSize,
            offsetCalculator.calculate(pageSize, pageNumber),
            sortBy,
            direction
        );
    }

    @Override
    public BaseMeta getMetaCancerTypes() {
        return cancerTypeMapper.getMetaCancerTypes();
    }

    @Override
    public TypeOfCancer getCancerType(String cancerTypeId) {
        return cancerTypeMapper.getCancerType(
            cancerTypeId,
            PersistenceConstants.DETAILED_PROJECTION
        );
    }
}
