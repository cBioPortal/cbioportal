package org.cbioportal.infrastructure.repository.clickhouse.mutation;

import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.persistence.mybatis.util.MolecularProfileCaseIdentifierUtil;
import org.cbioportal.legacy.persistence.mybatis.util.PaginationCalculator;
import org.cbioportal.shared.MutationSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Profile("clickhouse")
public class ClickhouseMutationRepository implements MutationRepository {
    
    private final ClickhouseMutationMapper mapper;
    private final MolecularProfileCaseIdentifierUtil molecularProfileCaseIdentifierUtil;
    

    public ClickhouseMutationRepository(ClickhouseMutationMapper clickhouseMutationMapper) {
        this.mapper = clickhouseMutationMapper;
        this.molecularProfileCaseIdentifierUtil = new MolecularProfileCaseIdentifierUtil();
    }

    @Override
    public List<Mutation> getMutationsInMultipleMolecularProfiles(
        List<String> molecularProfileIds,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        MutationSearchCriteria mutationSearchCriteria){
        
        Integer Limit= mutationSearchCriteria.pageSize();
        Integer offset= PaginationCalculator.offset(mutationSearchCriteria.pageSize(),mutationSearchCriteria.pageNumber());

        var projection=mutationSearchCriteria.projection();
        return  switch (projection){
            case ID->
                molecularProfileCaseIdentifierUtil.getGroupedCasesByMolecularProfileId(molecularProfileIds,sampleIds)
                    .entrySet()
                    .stream()
                    .flatMap(entry ->
                        mapper.getMutationsInMultipleMolecularProfilesId(
                            Arrays.asList(entry.getKey()),
                            new ArrayList<>(entry.getValue()),
                            entrezGeneIds,
                            false,
                            mutationSearchCriteria.projection().name(),
                            Limit,
                            offset,
                            mutationSearchCriteria.sortBy(),
                            mutationSearchCriteria.direction().name()).stream())
                    .collect(Collectors.toList());
                    
            case SUMMARY->
                molecularProfileCaseIdentifierUtil.getGroupedCasesByMolecularProfileId(molecularProfileIds,sampleIds)
                    .entrySet()
                    .stream()
                    .flatMap(entry ->
                        mapper.getSummaryMutationsInMultipleMolecularProfiles(
                            Arrays.asList(entry.getKey()),
                            new ArrayList<>(entry.getValue()),
                            entrezGeneIds,
                            false,
                            mutationSearchCriteria.projection().name(),
                            Limit,
                            offset,
                            mutationSearchCriteria.sortBy(),
                            mutationSearchCriteria.direction().name()).stream())
                    .collect(Collectors.toList());
            case DETAILED->
                molecularProfileCaseIdentifierUtil.getGroupedCasesByMolecularProfileId(molecularProfileIds,sampleIds)
                    .entrySet()
                    .stream()
                    .flatMap(entry ->
                        mapper.getDetailedMutationsInMultipleMolecularProfiles(
                            Arrays.asList(entry.getKey()),
                            new ArrayList<>(entry.getValue()),
                            entrezGeneIds,
                            false,
                            mutationSearchCriteria.projection().name(),
                            Limit,
                            offset,
                            mutationSearchCriteria.sortBy(),
                            mutationSearchCriteria.direction().name()).stream())
                    .collect(Collectors.toList());
            default -> new ArrayList<>();
        };
    }

    @Override
    public MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                    List<String> sampleIds, 
                                                                    List<Integer> entrezGeneIds) {
        return mapper.getMetaMutationsInMultipleMolecularProfiles(molecularProfileIds, sampleIds, entrezGeneIds,false);
    }
}
