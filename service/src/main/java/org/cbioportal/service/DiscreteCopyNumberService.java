package org.cbioportal.service;

import org.cbioportal.model.CopyNumberSampleCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface DiscreteCopyNumberService {

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, String sampleId,
                                                                        List<Integer> alterations,
                                                                        String projection)
        throws GeneticProfileNotFoundException;

    BaseMeta getMetaDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, String sampleId,
                                                        List<Integer> alterations)
        throws GeneticProfileNotFoundException;

    List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInGeneticProfile(String geneticProfileId,
                                                                          List<String> sampleIds,
                                                                          List<Integer> entrezGeneIds,
                                                                          List<Integer> alterations, String projection)
        throws GeneticProfileNotFoundException;

    BaseMeta fetchMetaDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, List<String> sampleIds,
                                                          List<Integer> entrezGeneIds, List<Integer> alterations)
        throws GeneticProfileNotFoundException;

    List<CopyNumberSampleCountByGene> getSampleCountByGeneAndAlteration(String geneticProfileId,
                                                                        List<Integer> entrezGeneIds,
                                                                        List<Integer> alterations);
}
