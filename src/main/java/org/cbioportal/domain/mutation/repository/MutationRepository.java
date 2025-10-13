package org.cbioportal.domain.mutation.repository;


import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.shared.MutationSearchCriteria;

import java.util.List;

/**
 * Repository interface for accessing mutation data or mutation.
 * 
 * <p>This abstraction defines the contract for retrieving both detailed mutation records
 * and aggregated mutation metadata across multiple molecular profiles, samples, and genes.
 */
public interface MutationRepository {
    
    /**
     *  Retrieves a list of mutations that match the specified filters and search criteria. 
     *  The util(molecularProfileCaseIdentifierUtil) filters and groups sampleId and
     *  molecularProfileIds to sanitize users input,avoiding round trip
     *  
     * @param molecularProfileIds List of molecularProfileIds
     * @param sampleIds List of sampleIds
     * @param entrezGeneIds  List of entrezGeneIds
     * @param mutationSearchCriteria A criteria to control the appearance of the dataset 
     * @return a list of {@link Mutation} objects that match the given criteria 
     * @see MutationSearchCriteria
     */
    List<Mutation> getMutationsInMultipleMolecularProfiles(
        List<String> molecularProfileIds,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        MutationSearchCriteria mutationSearchCriteria);

    /**
     * Retrieves aggregated metadata about mutations that match the specified filters.
     *
     *  <p>This method is typically used to determine dataset size, counts information without
     *  fetching full mutation details.The util(molecularProfileCaseIdentifierUtil) filters 
     *  and groups sampleId and molecularProfileIds to sanitize users input,avoiding round trip
     *  
     * @param molecularProfileIds List of molecularProfileIds
     * @param sampleIds List of sampleIds
     * @param entrezGeneIds List of Entrez gene identifiers 
     * @return {@link MutationMeta} containing aggregated information about the dataset
     */
    MutationMeta getMetaMutationsInMultipleMolecularProfiles(
        List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds);
}
