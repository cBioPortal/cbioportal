package org.cbioportal.infrastructure.repository.clickhouse.mutation;

import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.persistence.mybatis.util.PaginationCalculator;
import org.cbioportal.shared.MutationSearchCriteria;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
@Profile("clickhouse")
public class ClickhouseMutationRepository implements MutationRepository {
    
    private final ClickhouseMutationMapper mapper;
    

    public ClickhouseMutationRepository(ClickhouseMutationMapper clickhouseMutationMapper) {
        this.mapper = clickhouseMutationMapper;
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
                    mapper.getMutationsInMultipleMolecularProfiles(molecularProfileIds,
                            sampleIds,
                            entrezGeneIds,
                            false,
                            mutationSearchCriteria.projection().name(),
                            Limit,
                            offset,
                            mutationSearchCriteria.sortBy(),
                            mutationSearchCriteria.direction().name());
            case SUMMARY->
                    mapper.getSummaryMutationsInMultipleMolecularProfiles(molecularProfileIds,
                            sampleIds,
                            entrezGeneIds,
                            false,
                            mutationSearchCriteria.projection().name(),
                            Limit,
                            offset,
                            mutationSearchCriteria.sortBy(),
                            mutationSearchCriteria.direction().name());
            case DETAILED->
                    mapper.getDetailedMutationsInMultipleMolecularProfiles(molecularProfileIds,
                            sampleIds,
                            entrezGeneIds,
                            false,
                            mutationSearchCriteria.projection().name(),
                            Limit,
                            offset,
                            mutationSearchCriteria.sortBy(),
                            mutationSearchCriteria.direction().name());
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
