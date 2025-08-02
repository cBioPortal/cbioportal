package org.cbioportal.infrastructure.mutation;

import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Profile("clickhouse")
public class ClickhouseMutationRepository implements MutationRepository {
    
    private final ClickhouseMutationDataMapper clickhouseMutationDataMapper;
    

    public ClickhouseMutationRepository(ClickhouseMutationDataMapper clickhouseMutationDataMapper) {
        this.clickhouseMutationDataMapper = clickhouseMutationDataMapper;
    }

    @Override
    public List<Mutation> getMutationsInMultipleMolecularProfiles(
        List<String> molecularProfileIds,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction){
        return  new ArrayList<>();
    }

    @Override
    public MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds) {
        return null;
    }
}
