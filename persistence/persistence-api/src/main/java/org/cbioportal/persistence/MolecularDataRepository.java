package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.GenesetMolecularAlteration;
import org.cbioportal.model.GeneMolecularAlteration;

public interface MolecularDataRepository {

    String getCommaSeparatedSampleIdsOfMolecularProfile(String molecularProfileId);

    List<GeneMolecularAlteration> getGeneMolecularAlterations(String molecularProfileId, List<Integer> entrezGeneIds,
                                                              String projection);

    List<GenesetMolecularAlteration> getGenesetMolecularAlterations(String molecularProfileId, List<String> genesetIds,
                                                                    String projection);
}
