package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.VariantCount;
import org.cbioportal.persistence.VariantCountRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class VariantCountClickHouseRepository implements VariantCountRepository {

	@Override
	public List<VariantCount> fetchVariantCounts(String molecularProfileId, List<Integer> entrezGeneIds,
			List<String> keywords) {
		// TODO Auto-generated method stub
		return new ArrayList<VariantCount>();
	}

}
