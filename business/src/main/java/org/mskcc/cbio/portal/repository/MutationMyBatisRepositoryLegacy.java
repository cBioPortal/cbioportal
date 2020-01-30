package org.mskcc.cbio.portal.repository;

import java.util.Arrays;
import java.util.List;
import org.mskcc.cbio.portal.model.KeywordSampleCount;
import org.mskcc.cbio.portal.model.MutatedGeneSampleCount;
import org.mskcc.cbio.portal.model.Mutation;
import org.mskcc.cbio.portal.model.MutationCount;
import org.mskcc.cbio.portal.model.SignificantlyMutatedGene;
import org.mskcc.cbio.portal.persistence.MutationMapperLegacy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MutationMyBatisRepositoryLegacy
    implements MutationRepositoryLegacy {
    @Autowired
    private MutationMapperLegacy mutationMapperLegacy;

    public List<Mutation> getMutationsDetailed(
        List<String> geneticProfileStableIds,
        List<String> hugoGeneSymbols,
        List<String> sampleStableIds,
        String sampleListStableId
    ) {
        return mutationMapperLegacy.getMutationsDetailed(
            geneticProfileStableIds,
            hugoGeneSymbols,
            sampleStableIds,
            sampleListStableId
        );
    }

    public List<Mutation> getMutations(
        List<Integer> sampleIds,
        List<Integer> entrezGeneIds,
        Integer geneticProfileId
    ) {
        return mutationMapperLegacy.getMutations(
            sampleIds,
            entrezGeneIds,
            geneticProfileId,
            false
        );
    }

    public List<Mutation> getMutations(
        List<Integer> sampleIds,
        Integer entrezGeneId,
        Integer geneticProfileId
    ) {
        return mutationMapperLegacy.getMutations(
            sampleIds,
            Arrays.asList(entrezGeneId),
            geneticProfileId,
            false
        );
    }

    public List<Mutation> getMutations(
        Integer entrezGeneId,
        Integer geneticProfileId
    ) {
        return mutationMapperLegacy.getMutations(
            null,
            Arrays.asList(entrezGeneId),
            geneticProfileId,
            false
        );
    }

    public List<Mutation> getMutations(
        List<Integer> sampleIds,
        Integer geneticProfileId
    ) {
        return mutationMapperLegacy.getMutations(
            sampleIds,
            null,
            geneticProfileId,
            false
        );
    }

    public List<Mutation> getSimplifiedMutations(
        List<Integer> sampleIds,
        List<Integer> entrezGeneIds,
        Integer geneticProfileId
    ) {
        return mutationMapperLegacy.getMutations(
            sampleIds,
            entrezGeneIds,
            geneticProfileId,
            true
        );
    }

    public Boolean hasAlleleFrequencyData(
        Integer geneticProfileId,
        Integer sampleId
    ) {
        return mutationMapperLegacy.hasAlleleFrequencyData(
            geneticProfileId,
            sampleId
        );
    }

    public List<SignificantlyMutatedGene> getSignificantlyMutatedGenes(
        Integer geneticProfileId,
        List<Integer> entrezGeneIds,
        List<Integer> sampleIds,
        Integer thresholdRecurrence,
        Integer thresholdNumGenes
    ) {
        return getSignificantlyMutatedGenes(
            geneticProfileId,
            entrezGeneIds,
            sampleIds,
            thresholdRecurrence,
            thresholdNumGenes,
            true
        );
    }

    public List<SignificantlyMutatedGene> getSignificantlyMutatedGenes(
        Integer geneticProfileId,
        List<Integer> entrezGeneIds,
        List<Integer> sampleIds,
        Integer thresholdRecurrence,
        Integer thresholdNumGenes,
        boolean setGroupConcatMaxLen
    ) {
        if (setGroupConcatMaxLen) {
            Integer returnVal = mutationMapperLegacy.groupConcatMaxLenSet();
        }
        return mutationMapperLegacy.getSignificantlyMutatedGenes(
            geneticProfileId,
            entrezGeneIds,
            sampleIds,
            thresholdRecurrence,
            thresholdNumGenes
        );
    }

    public List<MutationCount> countMutationEvents(
        Integer geneticProfileId,
        List<Integer> sampleIds
    ) {
        return mutationMapperLegacy.countMutationEvents(
            geneticProfileId,
            sampleIds
        );
    }

    public List<MutatedGeneSampleCount> countSamplesWithMutatedGenes(
        Integer geneticProfileId,
        List<Integer> entrezGeneIds
    ) {
        return mutationMapperLegacy.countSamplesWithMutatedGenes(
            geneticProfileId,
            entrezGeneIds
        );
    }

    public List<KeywordSampleCount> countSamplesWithKeywords(
        Integer geneticProfileId,
        List<String> keywords
    ) {
        return mutationMapperLegacy.countSamplesWithKeywords(
            geneticProfileId,
            keywords
        );
    }

    public List<Integer> getGenesOfMutations(List<Integer> mutationEventIds) {
        return mutationMapperLegacy.getGenesOfMutations(mutationEventIds);
    }

    public List<String> getKeywordsOfMutations(List<Integer> mutationEventIds) {
        return mutationMapperLegacy.getKeywordsOfMutations(mutationEventIds);
    }

    public void setMutationMapperLegacy(
        MutationMapperLegacy mutationMapperLegacy
    ) {
        this.mutationMapperLegacy = mutationMapperLegacy;
    }
}
