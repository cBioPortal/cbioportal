package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GenesetMolecularAlteration;
import org.cbioportal.persistence.MolecularDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MolecularDataMyBatisRepository implements MolecularDataRepository {

    @Autowired
    private MolecularDataMapper molecularDataMapper;

    @Override
    public String getCommaSeparatedSampleIdsOfMolecularProfile(String molecularProfileId) {

        return molecularDataMapper.getCommaSeparatedSampleIdsOfMolecularProfile(molecularProfileId);
    }

    @Override
    public List<GeneMolecularAlteration> getGeneMolecularAlterations(String molecularProfileId, 
                                                                     List<Integer> entrezGeneIds, String projection) {

        return molecularDataMapper.getGeneMolecularAlterations(molecularProfileId, entrezGeneIds, projection);
    }

	@Override
	public List<GenesetMolecularAlteration> getGenesetMolecularAlterations(String molecularProfileId, 
                                                                           List<String> genesetIds, String projection) {
		return molecularDataMapper.getGenesetMolecularAlterations(molecularProfileId, genesetIds, projection);
	}
}
