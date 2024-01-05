package org.cbioportal.persistence.clickhouse;

import java.util.List;

import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CancerTypeRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.clickhouse.mapper.CancerTypeMapper;
import org.cbioportal.persistence.clickhouse.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class CancerTypeClickHouseRepository implements CancerTypeRepository {
	
    @Autowired
    private CancerTypeMapper cancerTypeMapper;

	@Override
	public List<TypeOfCancer> getAllCancerTypes(String projection, Integer pageSize, Integer pageNumber, String sortBy,
			String direction) {
		return cancerTypeMapper.getAllCancerTypes(projection, pageSize,
                OffsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
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
