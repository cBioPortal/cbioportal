package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Mutation;
import org.cbioportal.persistence.MutationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MutationMyBatisRepository implements MutationRepository {

    @Autowired
    MutationMapper mutationMapper;

    public List<Mutation> getMutations(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                       List<String> sampleStableIds, String sampleListStableId) {

        return mutationMapper.getMutations(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds,
                sampleListStableId);
    }
}
