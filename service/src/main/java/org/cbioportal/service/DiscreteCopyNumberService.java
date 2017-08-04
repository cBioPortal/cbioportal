package org.cbioportal.service;

import org.cbioportal.model.CopyNumberCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface DiscreteCopyNumberService {

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInGeneticProfileBySampleListId(String geneticProfileId,
                                                                                      String sampleListId,
                                                                                      List<Integer> entrezGeneIds,
                                                                                      List<Integer> alterationTypes,
                                                                                      String projection)
        throws GeneticProfileNotFoundException;

    BaseMeta getMetaDiscreteCopyNumbersInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId,
                                                                      List<Integer> entrezGeneIds,
                                                                      List<Integer> alterationTypes)
        throws GeneticProfileNotFoundException;

    List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInGeneticProfile(String geneticProfileId,
                                                                          List<String> sampleIds,
                                                                          List<Integer> entrezGeneIds,
                                                                          List<Integer> alterationTypes,
                                                                          String projection)
        throws GeneticProfileNotFoundException;

    BaseMeta fetchMetaDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, List<String> sampleIds,
                                                          List<Integer> entrezGeneIds, List<Integer> alterationTypes)
        throws GeneticProfileNotFoundException;

    List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(String geneticProfileId,
                                                                              List<String> sampleIds,
                                                                              List<Integer> entrezGeneIds,
                                                                              List<Integer> alterations);

    List<CopyNumberCountByGene> getPatientCountByGeneAndAlterationAndPatientIds(String geneticProfileId,
                                                                                List<String> patientIds,
                                                                                List<Integer> entrezGeneIds,
                                                                                List<Integer> alterations);

    List<CopyNumberCount> fetchCopyNumberCounts(String geneticProfileId, List<Integer> entrezGeneIds,
                                                List<Integer> alterations) throws GeneticProfileNotFoundException;
}
