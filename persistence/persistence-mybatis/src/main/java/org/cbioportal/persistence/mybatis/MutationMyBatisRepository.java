package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCount;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.dto.AltCount;
import org.cbioportal.persistence.dto.KeywordSampleCount;
import org.cbioportal.persistence.dto.MutatedGeneSampleCount;
import org.cbioportal.persistence.dto.SignificantlyMutatedGene;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import org.cbioportal.persistence.dto.PositionMutationCount;

@Repository
public class MutationMyBatisRepository implements MutationRepository {

    @Autowired
    MutationMapper mutationMapper;

    public List<Mutation> getMutationsDetailed(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                               List<String> sampleStableIds, String sampleListStableId) {

        return mutationMapper.getMutationsDetailed(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds,
                sampleListStableId);
    }

    public List<Mutation> getMutations(List<Integer> sampleIds, List<Integer> entrezGeneIds, Integer geneticProfileId) {

        return mutationMapper.getMutations(sampleIds, entrezGeneIds, geneticProfileId, false);
    }

    public List<Mutation> getMutations(List<Integer> sampleIds, Integer entrezGeneId, Integer geneticProfileId) {

        return mutationMapper.getMutations(sampleIds, Arrays.asList(entrezGeneId), geneticProfileId, false);
    }

    public List<Mutation> getMutations(Integer entrezGeneId, Integer geneticProfileId) {

        return mutationMapper.getMutations(null, Arrays.asList(entrezGeneId), geneticProfileId, false);
    }

    public List<Mutation> getMutations(List<Integer> sampleIds, Integer geneticProfileId) {

        return mutationMapper.getMutations(sampleIds, null, geneticProfileId, false);
    }

    public List<Mutation> getSimplifiedMutations(List<Integer> sampleIds, List<Integer> entrezGeneIds,
                                                 Integer geneticProfileId) {

        return mutationMapper.getMutations(sampleIds, entrezGeneIds, geneticProfileId, true);
    }

    public Boolean hasAlleleFrequencyData(Integer geneticProfileId, Integer sampleId) {
        return mutationMapper.hasAlleleFrequencyData(geneticProfileId, sampleId);
    }

    public List<SignificantlyMutatedGene> getSignificantlyMutatedGenes(Integer geneticProfileId,
                                                                       List<Integer> entrezGeneIds,
                                                                       List<Integer> sampleIds,
                                                                       Integer thresholdRecurrence,
                                                                       Integer thresholdNumGenes) {

        return getSignificantlyMutatedGenes(geneticProfileId, entrezGeneIds, sampleIds, thresholdRecurrence,
                thresholdNumGenes, true);
    }

    public List<SignificantlyMutatedGene> getSignificantlyMutatedGenes(Integer geneticProfileId,
                                                                       List<Integer> entrezGeneIds,
                                                                       List<Integer> sampleIds,
                                                                       Integer thresholdRecurrence,
                                                                       Integer thresholdNumGenes,
                                                                       boolean setGroupConcatMaxLen) {

        if (setGroupConcatMaxLen) {
            Integer returnVal = mutationMapper.groupConcatMaxLenSet();
        }
        return mutationMapper.getSignificantlyMutatedGenes(geneticProfileId, entrezGeneIds, sampleIds,
                thresholdRecurrence, thresholdNumGenes);

    }

    public List<MutationCount> countMutationEvents(Integer geneticProfileId, List<Integer> sampleIds) {

        return mutationMapper.countMutationEvents(geneticProfileId, sampleIds);
    }

    public List<MutatedGeneSampleCount> countSamplesWithMutatedGenes(Integer geneticProfileId,
                                                                     List<Integer> entrezGeneIds) {

        return mutationMapper.countSamplesWithMutatedGenes(geneticProfileId, entrezGeneIds);
    }

    public List<KeywordSampleCount> countSamplesWithKeywords(Integer geneticProfileId, List<String> keywords) {
        return mutationMapper.countSamplesWithKeywords(geneticProfileId, keywords);
    }

    public List<Integer> getGenesOfMutations(List<Integer> mutationEventIds) {
        return mutationMapper.getGenesOfMutations(mutationEventIds);
    }

    public List<String> getKeywordsOfMutations(List<Integer> mutationEventIds) {
        return mutationMapper.getKeywordsOfMutations(mutationEventIds);
    }

    public List<AltCount> getMutationsCounts(String type, String hugoGeneSymbol, Integer start, Integer end,
                                             List<String> cancerStudyIdentifiers, Boolean perStudy) {

        return mutationMapper.getMutationsCounts(type, hugoGeneSymbol, start, end, cancerStudyIdentifiers, perStudy);
    }
    
    public List<PositionMutationCount> getPositionMutationCounts(String hugoGeneSymbol, List<Integer> positions) {
	    return mutationMapper.getPositionMutationCounts(hugoGeneSymbol, positions);
    }
}
