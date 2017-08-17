package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCount;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.MutationCountByGene;
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
                                                                     List<Integer> entrezGeneIds, Boolean snpOnly,
                                                                     String projection, Integer pageSize,
                                                                     Integer pageNumber, String sortBy,
                                                                     String direction) {

        return mutationMapper.getMutationsBySampleListId(geneticProfileId, sampleListId, entrezGeneIds, snpOnly,
            projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public MutationMeta getMetaMutationsInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId,
                                                                       List<Integer> entrezGeneIds) {

        return mutationMapper.getMetaMutationsBySampleListId(geneticProfileId, sampleListId, entrezGeneIds, null);
    }

    @Override
    public List<Mutation> getMutationsInMultipleGeneticProfiles(List<String> geneticProfileIds, List<String> sampleIds,
                                                                List<Integer> entrezGeneIds, String projection,
                                                                Integer pageSize, Integer pageNumber, String sortBy,
                                                                String direction) {

        return mutationMapper.getMutationsInMultipleGeneticProfiles(geneticProfileIds, sampleIds, entrezGeneIds, null,
            projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public MutationMeta getMetaMutationsInMultipleGeneticProfiles(List<String> geneticProfileIds,
                                                                  List<String> sampleIds, List<Integer> entrezGeneIds) {

        return mutationMapper.getMetaMutationsInMultipleGeneticProfiles(geneticProfileIds, sampleIds, entrezGeneIds,
            null);
    }

    @Override
    public List<Mutation> fetchMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds,
                                                         List<Integer> entrezGeneIds, Boolean snpOnly,
                                                         String projection, Integer pageSize, Integer pageNumber,
                                                         String sortBy, String direction) {

        return mutationMapper.getMutationsBySampleIds(geneticProfileId, sampleIds, entrezGeneIds, snpOnly, projection,
            pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public MutationMeta fetchMetaMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds,
                                                           List<Integer> entrezGeneIds) {

        return mutationMapper.getMetaMutationsBySampleIds(geneticProfileId, sampleIds, entrezGeneIds, null);
    }

    @Override
    public List<MutationCountByGene> getSampleCountByEntrezGeneIdsAndSampleIds(String geneticProfileId,
                                                                               List<String> sampleIds,
                                                                               List<Integer> entrezGeneIds) {

        return mutationMapper.getSampleCountByEntrezGeneIdsAndSampleIds(geneticProfileId, sampleIds, entrezGeneIds,
            null);
    }

    @Override
    public List<MutationCountByGene> getPatientCountByEntrezGeneIdsAndSampleIds(String geneticProfileId,
                                                                                List<String> patientIds,
                                                                                List<Integer> entrezGeneIds) {

        return mutationMapper.getPatientCountByEntrezGeneIdsAndSampleIds(geneticProfileId, patientIds, entrezGeneIds,
            null);
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

    @Override
    public MutationCountByPosition getMutationCountByPosition(Integer entrezGeneId, Integer proteinPosStart,
                                                              Integer proteinPosEnd) {

        return mutationMapper.getMutationCountByPosition(entrezGeneId, proteinPosStart, proteinPosEnd);
    }
}
