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
        
        Integer Limit= mutationSearchCriteria.pageSize();
        Integer offset= PaginationCalculator.offset(mutationSearchCriteria.pageSize(),mutationSearchCriteria.pageNumber());
        List<ProfileSamplePair> profileSamplesPairs = groupAndFilterProfileAndSample(molecularProfileIds,sampleIds);
        
        var projection=mutationSearchCriteria.projection();
        return  switch (projection){
            case ID-> 
                mapper.getMutationsInMultipleMolecularProfilesId(
                    profileSamplesPairs,
                    entrezGeneIds,
                    false,
                    mutationSearchCriteria.projection().name(),
                    Limit,
                    offset,
                    mutationSearchCriteria.sortBy(),
                    mutationSearchCriteria.direction().name());
            case SUMMARY->
                mapper.getSummaryMutationsInMultipleMolecularProfiles(
                    profileSamplesPairs,
                    entrezGeneIds,
                    false,
                    mutationSearchCriteria.projection().name(),
                    Limit,
                    offset,
                    mutationSearchCriteria.sortBy(),
                    mutationSearchCriteria.direction().name()
                );
            case DETAILED->
               mapper.getDetailedMutationsInMultipleMolecularProfiles(
                   profileSamplesPairs,
                   entrezGeneIds,
                   false,
                   mutationSearchCriteria.projection().name(),
                   Limit,
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
        List<ProfileSamplePair> profileSamplesPairs = groupAndFilterProfileAndSample(molecularProfileIds,sampleIds);
        return mapper.getMetaMutationsInMultipleMolecularProfiles(profileSamplesPairs, entrezGeneIds,false);
    }

    /**
     * Groups and filters the molecularProfile and sampleIds to help performance in the database
     * @param molecularProfileIds List of molecularProfiles
     * @param sampleIds List of sampleIds 
     * @return List of ProfileSamplePair
     */
    private List<ProfileSamplePair> groupAndFilterProfileAndSample(List<String> molecularProfileIds,
                                                                     List<String> sampleIds){
        Map<String, Set<String>> groupedCases=  molecularProfileCaseIdentifierUtil
            .getGroupedCasesByMolecularProfileId(molecularProfileIds,sampleIds);

        return groupedCases.entrySet()
            .stream()
            .map(entry -> new ProfileSamplePair(
                entry.getKey(),                       
                new ArrayList<>(entry.getValue())    
            ))
            .toList();
    }
}
