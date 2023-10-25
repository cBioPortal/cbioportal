package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.mybatis.util.MolecularProfileCaseIdentifierUtil;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class MutationMyBatisRepository implements MutationRepository {

    @Autowired
    private MutationMapper mutationMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;
    @Autowired
    private MolecularProfileCaseIdentifierUtil molecularProfileCaseIdentifierUtil;

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

        return molecularProfileCaseIdentifierUtil
            .getGroupedCasesByMolecularProfileId(molecularProfileIds, sampleIds)
            .entrySet()
            .stream()
            .flatMap(entry -> mutationMapper.getMutationsInMultipleMolecularProfiles(
                Arrays.asList(entry.getKey()),
                new ArrayList<>(entry.getValue()),
                entrezGeneIds,
                null,
                projection,
                pageSize,
                offsetCalculator.calculate(pageSize, pageNumber),
                sortBy,
                direction).stream())
            .collect(Collectors.toList());
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

        return molecularProfileCaseIdentifierUtil
            .getGroupedCasesByMolecularProfileId(molecularProfileIds, sampleIds)
            .entrySet()
            .stream()
            .flatMap(entry -> mutationMapper.getMutationsInMultipleMolecularProfilesByGeneQueries(
                Arrays.asList(entry.getKey()),
                new ArrayList<>(entry.getValue()),
                null,
                projection,
                pageSize,
                offsetCalculator.calculate(pageSize, pageNumber),
                sortBy,
                direction,
                geneQueries).stream())
            .collect(Collectors.toList());
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

        return mutationMapper.getMutationsInMultipleMolecularProfiles(
            Arrays.asList(molecularProfileId),
            new ArrayList<>(sampleIds),
            entrezGeneIds,
            snpOnly,
            projection,
            pageSize,
            offsetCalculator.calculate(pageSize, pageNumber),
            sortBy,
            direction);
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

}
