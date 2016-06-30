package org.cbioportal.persistence;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCount;
import org.cbioportal.persistence.dto.AltCount;
import org.cbioportal.persistence.dto.KeywordSampleCount;
import org.cbioportal.persistence.dto.MutatedGeneSampleCount;
import org.cbioportal.persistence.dto.SignificantlyMutatedGene;

import java.util.List;

public interface MutationRepository {

    List<Mutation> getMutationsDetailed(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                        List<String> sampleStableIds, String sampleListStableId);

    List<Mutation> getMutations(List<Integer> sampleIds, List<Integer> entrezGeneIds, Integer geneticProfileId);

    List<Mutation> getMutations(List<Integer> sampleIds, Integer entrezGeneId, Integer geneticProfileId);

    List<Mutation> getMutations(Integer entrezGeneId, Integer geneticProfileId);

    List<Mutation> getMutations(List<Integer> sampleIds, Integer geneticProfileId);

    List<Mutation> getSimplifiedMutations(List<Integer> sampleIds, List<Integer> entrezGeneIds,
                                          Integer geneticProfileId);

    Boolean hasAlleleFrequencyData(Integer geneticProfileId, Integer sampleId);

    List<SignificantlyMutatedGene> getSignificantlyMutatedGenes(Integer geneticProfileId, List<Integer> entrezGeneIds,
                                                                List<Integer> sampleIds, Integer thresholdRecurrence,
                                                                Integer thresholdNumGenes);

    List<MutationCount> countMutationEvents(Integer geneticProfileId, List<Integer> sampleIds);

    List<MutatedGeneSampleCount> countSamplesWithMutatedGenes(Integer geneticProfileId, List<Integer> entrezGeneIds);

    List<KeywordSampleCount> countSamplesWithKeywords(Integer geneticProfileId, List<String> keywords);

    List<Integer> getGenesOfMutations(List<Integer> mutationEventIds);

    List<String> getKeywordsOfMutations(List<Integer> mutationEventIds);

    List<AltCount> getMutationsCounts(String type, String hugoGeneSymbol, Integer start, Integer end,
                                      List<String> cancerStudyIdentifiers, Boolean perStudy);
}
