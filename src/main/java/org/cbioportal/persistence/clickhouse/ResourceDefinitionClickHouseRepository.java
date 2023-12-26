package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.ResourceDefinition;
import org.cbioportal.persistence.ResourceDefinitionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class ResourceDefinitionClickHouseRepository implements ResourceDefinitionRepository {

	@Override
	public ResourceDefinition getResourceDefinition(String studyId, String resourceId) {
		// TODO Auto-generated method stub
		return new ResourceDefinition();
	}

	@Override
	public List<ResourceDefinition> fetchResourceDefinitions(List<String> studyIds, String projection, Integer pageSize,
			Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<ResourceDefinition>();
	}

}
