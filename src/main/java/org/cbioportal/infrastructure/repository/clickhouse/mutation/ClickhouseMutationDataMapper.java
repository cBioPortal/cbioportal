package org.cbioportal.infrastructure.repository.clickhouse.mutation;

import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;

import java.util.List;

public interface ClickhouseMutationDataMapper {

    List<Mutation> getMutationsInMultipleMolecularProfiles(
            List<String> molecularProfileIds,
            List<String> sampleIds,
            List<Integer> entrezGeneIds,
            String projection,
            Integer pageSize,
            Integer pageNumber,
            String sortBy,
            String direction);

    MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                    List<String> sampleIds, 
                                                                    List<Integer> entrezGeneIds);
}
