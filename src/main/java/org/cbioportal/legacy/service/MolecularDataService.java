package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.GeneFilterQuery;
import org.cbioportal.legacy.model.GeneMolecularAlteration;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;

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
