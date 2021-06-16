package org.cbioportal.service;

import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface MolecularDataService {

    List<GeneMolecularData> getMolecularData(String molecularProfileId, String sampleListId,
                                             List<Integer> entrezGeneIds, String projection)
        throws MolecularProfileNotFoundException;

    BaseMeta getMetaMolecularData(String molecularProfileId, String sampleListId, List<Integer> entrezGeneIds)
        throws MolecularProfileNotFoundException;

    List<GeneMolecularData> fetchMolecularData(String molecularProfileId, List<String> sampleIds,
                                               List<Integer> entrezGeneIds, String projection)
        throws MolecularProfileNotFoundException;

    BaseMeta fetchMetaMolecularData(String molecularProfileId, List<String> sampleIds, List<Integer> entrezGeneIds)
        throws MolecularProfileNotFoundException;

    Iterable<GeneMolecularAlteration> getMolecularAlterations(String molecularProfileId, List<Integer> entrezGeneIds,
                                                              String projection) throws MolecularProfileNotFoundException;

    Integer getNumberOfSamplesInMolecularProfile(String molecularProfileId);

    List<GeneMolecularData> getMolecularDataInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                        List<String> sampleIds,
                                                                        List<Integer> entrezGeneIds,
                                                                        String projection);

    List<GeneMolecularData> getMolecularDataInMultipleMolecularProfilesByGeneQueries(List<String> molecularProfileIds,
                                                                                     List<String> sampleIds,
                                                                                     List<GeneFilterQuery> geneQueries,
                                                                                     String projection);

	BaseMeta getMetaMolecularDataInMultipleMolecularProfiles(List<String> molecularProfileIds, List<String> sampleIds,
		                                                     List<Integer> entrezGeneIds);

}
