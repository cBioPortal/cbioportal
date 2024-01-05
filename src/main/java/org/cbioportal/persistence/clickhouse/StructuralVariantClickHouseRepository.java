package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.StructuralVariant;
import org.cbioportal.model.StructuralVariantFilterQuery;
import org.cbioportal.model.StructuralVariantQuery;
import org.cbioportal.persistence.StructuralVariantRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class StructuralVariantClickHouseRepository implements StructuralVariantRepository {

	@Override
	public List<StructuralVariant> fetchStructuralVariants(List<String> molecularProfileIds, List<String> sampleIds,
			List<Integer> entrezGeneIds, List<StructuralVariantQuery> structuralVariantQueries) {
		// TODO Auto-generated method stub
		return new ArrayList<StructuralVariant>();
	}

	@Override
	public List<StructuralVariant> fetchStructuralVariantsByGeneQueries(List<String> molecularProfileIds,
			List<String> sampleIds, List<GeneFilterQuery> geneQueries) {
		// TODO Auto-generated method stub
		return new ArrayList<StructuralVariant>();
	}

	@Override
	public List<StructuralVariant> fetchStructuralVariantsByStructVarQueries(List<String> molecularProfileIds,
			List<String> sampleIds, List<StructuralVariantFilterQuery> structVarQueries) {
		// TODO Auto-generated method stub
		return new ArrayList<StructuralVariant>();
	}

}
