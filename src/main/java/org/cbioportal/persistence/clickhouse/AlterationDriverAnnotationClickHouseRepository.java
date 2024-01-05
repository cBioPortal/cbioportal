package org.cbioportal.persistence.clickhouse;

import java.util.Collections;
import java.util.List;

import org.cbioportal.model.AlterationDriverAnnotation;
import org.cbioportal.persistence.AlterationDriverAnnotationRepository;
import org.cbioportal.persistence.clickhouse.mapper.AlterationDriverAnnotationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class AlterationDriverAnnotationClickHouseRepository implements AlterationDriverAnnotationRepository {

	@Autowired
	AlterationDriverAnnotationMapper alterationDriverAnnotationMapper;

	@Override
	public List<AlterationDriverAnnotation> getAlterationDriverAnnotations(
			List<String> molecularProfileCaseIdentifiers) {
		
        if (molecularProfileCaseIdentifiers == null || molecularProfileCaseIdentifiers.isEmpty()) {
            return Collections.emptyList();
        }

        
		return alterationDriverAnnotationMapper.getAlterationDriverAnnotations(molecularProfileCaseIdentifiers);
	}

}
