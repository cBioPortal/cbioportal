package org.mskcc.cbio.portal.repository;

import java.util.List;
import org.mskcc.cbio.portal.model.KeywordSampleCount;
import org.mskcc.cbio.portal.model.MutatedGeneSampleCount;
import org.mskcc.cbio.portal.model.Mutation;
import org.mskcc.cbio.portal.model.MutationCount;
import org.mskcc.cbio.portal.model.SignificantlyMutatedGene;

public interface MutationRepositoryLegacy {
    List<Mutation> getMutationsDetailed(
        List<String> geneticProfileStableIds,
        List<String> hugoGeneSymbols,
        List<String> sampleStableIds,
        String sampleListStableId
    );

    List<Mutation> getMutations(
        List<Integer> sampleIds,
        List<Integer> entrezGeneIds,
        Integer geneticProfileId
    );

    List<Mutation> getMutations(
        List<Integer> sampleIds,
        Integer entrezGeneId,
        Integer geneticProfileId
    );

    List<Mutation> getMutations(Integer entrezGeneId, Integer geneticProfileId);

    List<Mutation> getMutations(
        List<Integer> sampleIds,
        Integer geneticProfileId
    );

    List<Mutation> getSimplifiedMutations(
        List<Integer> sampleIds,
        List<Integer> entrezGeneIds,
        Integer geneticProfileId
    );

    Boolean hasAlleleFrequencyData(Integer geneticProfileId, Integer sampleId);

    List<SignificantlyMutatedGene> getSignificantlyMutatedGenes(
        Integer geneticProfileId,
        List<Integer> entrezGeneIds,
        List<Integer> sampleIds,
        Integer thresholdRecurrence,
        Integer thresholdNumGenes
    );

    List<MutationCount> countMutationEvents(
        Integer geneticProfileId,
        List<Integer> sampleIds
    );

    List<MutatedGeneSampleCount> countSamplesWithMutatedGenes(
        Integer geneticProfileId,
        List<Integer> entrezGeneIds
    );

    List<KeywordSampleCount> countSamplesWithKeywords(
        Integer geneticProfileId,
        List<String> keywords
    );

    List<Integer> getGenesOfMutations(List<Integer> mutationEventIds);

    List<String> getKeywordsOfMutations(List<Integer> mutationEventIds);
}
