package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MutationMyBatisRepository implements MutationRepository {

    @Autowired
    private MutationMapper mutationMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<Mutation> getMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                       List<Integer> entrezGeneIds, Boolean snpOnly,
                                                                       String projection, Integer pageSize,
                                                                       Integer pageNumber, String sortBy,
                                                                       String direction) {

        return mutationMapper.getMutationsBySampleListId(molecularProfileId, sampleListId, entrezGeneIds, snpOnly,
            projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public MutationMeta getMetaMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                         List<Integer> entrezGeneIds) {

        return mutationMapper.getMetaMutationsBySampleListId(molecularProfileId, sampleListId, entrezGeneIds, null);
    }

    @Override
    public List<Mutation> getMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                  List<String> sampleIds, List<Integer> entrezGeneIds,
                                                                  String projection, Integer pageSize,
                                                                  Integer pageNumber, String sortBy, String direction) {
        boolean searchFusions = false;
        return mutationMapper.getMutationsInMultipleMolecularProfiles(molecularProfileIds, sampleIds, entrezGeneIds,
            null, searchFusions, projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public List<Mutation> getMutationsInMultipleMolecularProfilesByGeneQueries(List<String> molecularProfileIds,
                                                                               List<String> sampleIds,
                                                                               List<GeneFilterQuery> geneQueries,
                                                                               String projection,
                                                                               Integer pageSize,
                                                                               Integer pageNumber,
                                                                               String sortBy,
                                                                               String direction) {
        if (geneQueries.isEmpty())
            return Collections.emptyList();
        
        boolean searchFusions = false;
        return mutationMapper.getMutationsInMultipleMolecularProfilesByGeneQueries(molecularProfileIds, sampleIds,
            null, searchFusions, projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber),
           sortBy, direction, geneQueries);
    }

    @Override
    public MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                    List<String> sampleIds,
                                                                    List<Integer> entrezGeneIds) {

        return mutationMapper.getMetaMutationsInMultipleMolecularProfiles(molecularProfileIds, sampleIds, entrezGeneIds,
            null);
    }

    @Override
    public List<Mutation> fetchMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                           List<Integer> entrezGeneIds, Boolean snpOnly,
                                                           String projection, Integer pageSize, Integer pageNumber,
                                                           String sortBy, String direction) {

        return mutationMapper.getMutationsBySampleIds(molecularProfileId, sampleIds, entrezGeneIds, snpOnly, projection,
            pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public MutationMeta fetchMetaMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                             List<Integer> entrezGeneIds) {

        return mutationMapper.getMetaMutationsBySampleIds(molecularProfileId, sampleIds, entrezGeneIds, null);
    }

    @Override
    public MutationCountByPosition getMutationCountByPosition(Integer entrezGeneId, Integer proteinPosStart,
                                                              Integer proteinPosEnd) {

        return mutationMapper.getMutationCountByPosition(entrezGeneId, proteinPosStart, proteinPosEnd);
    }

    // TODO: cleanup once fusion/structural data is fixed in database
    @Override
    public List<Mutation> getFusionsInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                List<String> sampleIds,
                                                                List<Integer> entrezGeneIds,
                                                                String projection,
                                                                Integer pageSize,
                                                                Integer pageNumber,
                                                                String sortBy,
                                                                String direction) {
        boolean searchFusion = true;
        return mutationMapper.getMutationsInMultipleMolecularProfiles(molecularProfileIds,
            sampleIds, entrezGeneIds, false, searchFusion, projection, pageSize, pageNumber, sortBy, direction);
    }
    
    @Override
    public List<Mutation> getFusionsInMultipleMolecularProfilesByGeneQueries(List<String> molecularProfileIds,
                                                                             List<String> sampleIds,
                                                                             List<GeneFilterQuery> geneQueries,
                                                                             String projection,
                                                                             Integer pageSize,
                                                                             Integer pageNumber,
                                                                             String sortBy,
                                                                             String direction) {
        
        if (geneQueries.isEmpty())
            return Collections.emptyList();
        
        boolean searchFusions = true;
        return mutationMapper.getMutationsInMultipleMolecularProfilesByGeneQueries(molecularProfileIds, sampleIds,
            null, searchFusions, projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber),
            sortBy, direction, geneQueries);
    }
    // TODO: cleanup once fusion/structural data is fixed in database

}
