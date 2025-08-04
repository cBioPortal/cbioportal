package org.cbioportal.domain.mutation.repository;


import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;

import java.util.List;

/**
 * Repository interface for accessing mutation data.
 * 
 * 
 */
public interface MutationRepository {
    /**
     * 
     * @param molecularProfileIds
     * @param sampleIds
     * @param entrezGeneIds
     * @param projection
     * @param pageSize
     * @param pageNumber
     * @param sortBy
     * @param direction
     * @return mutation 
     */
    List<Mutation> getMutationsInMultipleMolecularProfiles(
        List<String> molecularProfileIds,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction);

    
    MutationMeta getMetaMutationsInMultipleMolecularProfiles(
        List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds);
}
