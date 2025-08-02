package org.cbioportal.infrastructure.repository.clickhouse.mutation;

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
    
    private final ClickhouseMutationDataMapper mapper;
    

    public ClickhouseMutationRepository(ClickhouseMutationDataMapper mapper) {
        this.mapper = mapper;
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
        return  mapper.getMutationsInMultipleMolecularProfiles(molecularProfileIds,
            sampleIds,
            entrezGeneIds,
            projection,
            pageSize,
            pageNumber,sortBy,
            direction);
    }

    @Override
    public MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                    List<String> sampleIds, 
                                                                    List<Integer> entrezGeneIds) {
        return mapper.getMetaMutationsInMultipleMolecularProfiles(molecularProfileIds, sampleIds, entrezGeneIds);
    }
}
