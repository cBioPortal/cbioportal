package org.cbioportal.service;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface DiscreteCopyNumberService {

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMolecularProfileBySampleListId(String molecularProfileId,
                                                                                        String sampleListId,
                                                                                        List<Integer> entrezGeneIds,
                                                                                        List<Integer> alterationTypes,
                                                                                        String projection)
        throws MolecularProfileNotFoundException;

    BaseMeta getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                        List<Integer> entrezGeneIds,
                                                                        List<Integer> alterationTypes)
        throws MolecularProfileNotFoundException;

    List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInMolecularProfile(String molecularProfileId,
                                                                            List<String> sampleIds,
                                                                            List<Integer> entrezGeneIds,
                                                                            List<Integer> alterationTypes,
                                                                            String projection)
        throws MolecularProfileNotFoundException;

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                                   List<String> sampleIds,
                                                                                   List<Integer> entrezGeneIds,
                                                                                   List<Integer> alterationTypes, 
                                                                                   String projection);

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfilesByGeneQueries(List<String> molecularProfileIds,
                                                                                                List<String> sampleIds,
                                                                                                List<GeneFilterQuery> geneQueries,
                                                                                                String projection);

    BaseMeta fetchMetaDiscreteCopyNumbersInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                            List<Integer> entrezGeneIds, List<Integer> alterationTypes)
        throws MolecularProfileNotFoundException;

    List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(String molecularProfileId,
                                                                              List<String> sampleIds,
                                                                              List<Integer> entrezGeneIds,
                                                                              List<Integer> alterations) 
        throws MolecularProfileNotFoundException;

    List<CopyNumberCount> fetchCopyNumberCounts(String molecularProfileId, List<Integer> entrezGeneIds,
                                                List<Integer> alterations) throws MolecularProfileNotFoundException;
}
