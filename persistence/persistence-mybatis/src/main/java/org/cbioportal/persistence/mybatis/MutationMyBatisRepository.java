package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.SampleType;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.dto.SampleMutationCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MutationMyBatisRepository implements MutationRepository {

    @Autowired
    private MutationMapper mutationMapper;

    public List<Mutation> getMutations(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                       List<String> sampleStableIds, String sampleListStableId) {

        return mutationMapper.getMutations(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds,
                sampleListStableId);
    }

    public List<SampleMutationCount> getMutationCounts(String geneticProfileStableId, List<String> sampleStableIds) {

        return mutationMapper.getMutationCounts(geneticProfileStableId, sampleStableIds, SampleType.getNonNormalTypes());
    }
}
