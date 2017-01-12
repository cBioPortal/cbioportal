package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class MutationMyBatisRepository implements MutationRepository {

    @Autowired
    private MutationMapper mutationMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<Mutation> getMutationsInGeneticProfile(String geneticProfileId, String sampleId, String projection, 
                                                       Integer pageSize, Integer pageNumber, String sortBy, 
                                                       String direction) {
       
        return mutationMapper.getMutations(geneticProfileId, sampleId == null ? null :Arrays.asList(sampleId),
            projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaMutationsInGeneticProfile(String geneticProfileId, String sampleId) {
        
        return mutationMapper.getMetaMutations(geneticProfileId, sampleId == null ? null :Arrays.asList(sampleId));
    }

    @Override
    public List<Mutation> fetchMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds,
                                                         String projection, Integer pageSize, Integer pageNumber,
                                                         String sortBy, String direction) {

        return mutationMapper.getMutations(geneticProfileId, sampleIds, projection, pageSize,
            offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta fetchMetaMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds) {

        return mutationMapper.getMetaMutations(geneticProfileId, sampleIds);
    }
}
