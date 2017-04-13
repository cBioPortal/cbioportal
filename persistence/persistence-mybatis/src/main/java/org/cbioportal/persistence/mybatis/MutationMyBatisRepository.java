package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCount;
import org.cbioportal.model.MutationSampleCountByGene;
import org.cbioportal.model.MutationSampleCountByKeyword;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MutationMyBatisRepository implements MutationRepository {

    @Autowired
    private MutationMapper mutationMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<Mutation> getMutationsInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId, 
                                                                     List<Integer> entrezGeneIds, String projection, 
                                                                     Integer pageSize, Integer pageNumber, 
                                                                     String sortBy, String direction) {

        return mutationMapper.getMutationsBySampleListId(geneticProfileId, sampleListId, entrezGeneIds, projection, 
            pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public MutationMeta getMetaMutationsInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId, 
                                                                       List<Integer> entrezGeneIds) {

        return mutationMapper.getMetaMutationsBySampleListId(geneticProfileId, sampleListId, entrezGeneIds);
    }

    @Override
    public List<Mutation> fetchMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds, 
                                                         List<Integer> entrezGeneIds, String projection, 
                                                         Integer pageSize, Integer pageNumber, String sortBy, 
                                                         String direction) {

        return mutationMapper.getMutationsBySampleIds(geneticProfileId, sampleIds, entrezGeneIds, projection, pageSize,
            offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public MutationMeta fetchMetaMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds, 
                                                           List<Integer> entrezGeneIds) {

        return mutationMapper.getMetaMutationsBySampleIds(geneticProfileId, sampleIds, entrezGeneIds);
    }

    @Override
    public List<MutationSampleCountByGene> getSampleCountByEntrezGeneIds(String geneticProfileId, 
                                                                         List<Integer> entrezGeneIds) {
        
        return mutationMapper.getSampleCountByEntrezGeneIds(geneticProfileId, entrezGeneIds);
    }

    @Override
    public List<MutationSampleCountByKeyword> getSampleCountByKeywords(String geneticProfileId, List<String> keywords) {
        
        return mutationMapper.getSampleCountByKeywords(geneticProfileId, keywords);
    }

    @Override
    public List<MutationCount> getMutationCountsInGeneticProfileBySampleListId(String geneticProfileId, 
                                                                               String sampleListId) {
        
        return mutationMapper.getMutationCountsBySampleListId(geneticProfileId, sampleListId);
    }
    
    @Override
    public List<MutationCount> fetchMutationCountsInGeneticProfile(String geneticProfileId, List<String> sampleIds) {

        return mutationMapper.getMutationCountsBySampleIds(geneticProfileId, sampleIds);
    }
}
