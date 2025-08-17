package org.cbioportal.infrastructure.repository.clickhouse.mutation;

import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.persistence.mybatis.util.PaginationCalculator;
import org.cbioportal.shared.MutationSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@Profile("clickhouse")
public class ClickhouseMutationRepository implements MutationRepository {
    
    private final ClickhouseMutationDataMapper mapper;
    

    public ClickhouseMutationRepository(ClickhouseMutationDataMapper clickhouseMutationMapper) {
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

        return  mapper.getMutationsInMultipleMolecularProfiles(molecularProfileIds,
            sampleIds,
            entrezGeneIds, 
                false,
            mutationSearchCriteria.projection().name(),
                Limit,
                offset,
            mutationSearchCriteria.sortBy(),
            mutationSearchCriteria.direction().name());
    }

    @Override
    public MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                    List<String> sampleIds, 
                                                                    List<Integer> entrezGeneIds) {
        return mapper.getMetaMutationsInMultipleMolecularProfiles(molecularProfileIds, sampleIds, entrezGeneIds,false);
    }
}
