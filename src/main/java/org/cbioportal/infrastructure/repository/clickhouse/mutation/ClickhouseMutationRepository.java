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
        
        Integer limit= mutationSearchCriteria.pageSize();
        Integer offset= PaginationCalculator.offset(mutationSearchCriteria.pageSize(),mutationSearchCriteria.pageNumber());

        Map<String, Set<String>> groupedCases=  molecularProfileCaseIdentifierUtil
            .getGroupedCasesByMolecularProfileId(molecularProfileIds,sampleIds);

        List<String> allMolecularProfileIds= new ArrayList<>(groupedCases.keySet());
        List<String> allSampleIds =  groupedCases.values().stream().flatMap(Collection::stream).distinct().toList();
            
        var projection=mutationSearchCriteria.projection();
        return  switch (projection){
            case ID-> 
                mapper.getMutationsInMultipleMolecularProfilesId(
                    allMolecularProfileIds,
                    allSampleIds,
                    entrezGeneIds,
                    false,
                    mutationSearchCriteria.projection().name(),
                    limit,
                    offset);
            case SUMMARY->
                mapper.getSummaryMutationsInMultipleMolecularProfiles(
                    allMolecularProfileIds,
                    allSampleIds,
                    entrezGeneIds,
                    false,
                    mutationSearchCriteria.projection().name(),
                    limit,
                    offset,
                    mutationSearchCriteria.sortBy(),
                    mutationSearchCriteria.direction().name()
                );
            case DETAILED->
               mapper.getDetailedMutationsInMultipleMolecularProfiles(
                   allMolecularProfileIds,
                   allSampleIds,
                   entrezGeneIds,
                   false,
                   mutationSearchCriteria.projection().name(),
                   limit,
                   offset,
                   mutationSearchCriteria.sortBy(),
                   mutationSearchCriteria.direction().name()
               );
            default -> new ArrayList<>();
        };
    }

    @Override
    public MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                    List<String> sampleIds, 
                                                                    List<Integer> entrezGeneIds) {
        Map<String, Set<String>> groupedCases=  molecularProfileCaseIdentifierUtil
            .getGroupedCasesByMolecularProfileId(molecularProfileIds,sampleIds);

        List<String> allMolecularProfileIds= new ArrayList<>(groupedCases.keySet());
        List<String> allSampleIds =  groupedCases.values().stream().flatMap(Collection::stream).distinct().toList();
        return mapper.getMetaMutationsInMultipleMolecularProfiles(allMolecularProfileIds,allSampleIds,entrezGeneIds,false);
    }
}
