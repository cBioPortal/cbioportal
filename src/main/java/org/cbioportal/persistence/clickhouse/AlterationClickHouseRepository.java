package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationCountByStructuralVariant;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.AlterationRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class AlterationClickHouseRepository implements AlterationRepository {

	@Override
	public List<AlterationCountByGene> getSampleAlterationGeneCounts(
			Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers, Select<Integer> entrezGeneIds,
			AlterationFilter alterationFilter) {
		// TODO Auto-generated method stub
		return new ArrayList<AlterationCountByGene>();
	}

	@Override
	public List<AlterationCountByGene> getPatientAlterationGeneCounts(
			Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers, Select<Integer> entrezGeneIds,
			AlterationFilter alterationFilter) {
		// TODO Auto-generated method stub
		return new ArrayList<AlterationCountByGene>();
	}

	@Override
	public List<CopyNumberCountByGene> getSampleCnaGeneCounts(
			Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers, Select<Integer> entrezGeneIds,
			AlterationFilter alterationFilter) {
		// TODO Auto-generated method stub
		return new ArrayList<CopyNumberCountByGene>();
	}

	@Override
	public List<CopyNumberCountByGene> getPatientCnaGeneCounts(
			Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers, Select<Integer> entrezGeneIds,
			AlterationFilter alterationFilter) {
		// TODO Auto-generated method stub
		return new ArrayList<CopyNumberCountByGene>();
	}

	@Override
	public List<AlterationCountByStructuralVariant> getSampleStructuralVariantCounts(
			Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers, AlterationFilter alterationFilter) {
		// TODO Auto-generated method stub
		return new ArrayList<AlterationCountByStructuralVariant>();
	}

	@Override
	public List<AlterationCountByStructuralVariant> getPatientStructuralVariantCounts(
			Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers, AlterationFilter alterationFilter) {
		// TODO Auto-generated method stub
		return new ArrayList<AlterationCountByStructuralVariant>();
	}

}
