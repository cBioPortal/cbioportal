package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GenesetMolecularAlteration;

import java.util.List;

public interface MolecularDataMapper {

    String getCommaSeparatedSampleIdsOfMolecularProfile(String molecularProfileId);

    List<GeneMolecularAlteration> getGeneMolecularAlterations(String molecularProfileId, List<Integer> entrezGeneIds,
                                                              String projection);

	List<GenesetMolecularAlteration> getGenesetMolecularAlterations(String molecularProfileId, List<String> genesetIds,
                                                                  String projection);
}
