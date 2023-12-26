package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.AlterationDriverAnnotation;
import org.cbioportal.persistence.AlterationDriverAnnotationRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class AlterationDriverAnnotationClickHouseRepository implements AlterationDriverAnnotationRepository {

	@Override
	public List<AlterationDriverAnnotation> getAlterationDriverAnnotations(
			List<String> molecularProfileCaseIdentifiers) {
		// TODO Auto-generated method stub
		return new ArrayList<AlterationDriverAnnotation>();
	}

}
