package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.GenericAssayAdditionalProperty;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.persistence.GenericAssayRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class GenericAssayClickHouseRepository implements GenericAssayRepository {

	@Override
	public List<GenericAssayMeta> getGenericAssayMeta(List<String> stableIds) {
		// TODO Auto-generated method stub
		return new ArrayList<GenericAssayMeta>();
	}

	@Override
	public List<GenericAssayAdditionalProperty> getGenericAssayAdditionalproperties(List<String> stableIds) {
		// TODO Auto-generated method stub
		return new ArrayList<GenericAssayAdditionalProperty>();
	}

	@Override
	public List<String> getGenericAssayStableIdsByMolecularIds(List<String> molecularProfileIds) {
		// TODO Auto-generated method stub
		return new ArrayList<String>();
	}

}
