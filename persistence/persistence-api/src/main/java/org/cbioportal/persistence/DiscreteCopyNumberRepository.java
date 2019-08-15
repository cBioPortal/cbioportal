package org.cbioportal.persistence;

import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface DiscreteCopyNumberRepository {

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMolecularProfileBySampleListId(String molecularProfileId,
                                                                                        String sampleListId,
                                                                                        List<Integer> entrezGeneIds,
                                                                                        List<Integer> alterationTypes,
                                                                                        String projection);


    BaseMeta getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                        List<Integer> entrezGeneIds,
                                                                        List<Integer> alterationTypes);

    List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInMolecularProfile(String molecularProfileId,
                                                                            List<String> sampleIds,
                                                                            List<Integer> entrezGeneIds,
                                                                            List<Integer> alterationTypes,
                                                                            String projection);

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                                   List<String> sampleIds,
                                                                                   List<Integer> entrezGeneIds,
                                                                                   List<Integer> alterationTypes, 
                                                                                   String projection);

    BaseMeta fetchMetaDiscreteCopyNumbersInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                            List<Integer> entrezGeneIds, List<Integer> alterationTypes);

    List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(String molecularProfileId,
                                                                              List<String> sampleIds,
                                                                              List<Integer> entrezGeneIds,
                                                                              List<Integer> alterations);

    List<CopyNumberCountByGene> getSampleCountInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                          List<String> sampleIds, 
                                                                          List<Integer> entrezGeneIds, 
                                                                          List<Integer> alterations);

    List<CopyNumberCountByGene> getPatientCountInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                                List<String> patientIds,
                                                                                List<Integer> entrezGeneIds,
                                                                                List<Integer> alterations);
}
