package org.cbioportal.domain.mutation.usecase;

import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.domain.mutation.util.MutationUtil;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.web.parameter.MutationMultipleStudyFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")

/**
 * Use case for retrieving aggregated mutation metadata.
 * 
 * <p>This use case determines the correct input parameters from the provided
 * {@link MutationMultipleStudyFilter} and delegates the query to the {@link MutationRepository}.
 *
 * <p>If  molecularProfileIds are available directly in the filter, they are passed
 * straight through to the repository. Otherwise, molecular profile IDs and sample IDs are
 * extracted from the filter’s sample–molecular identifiers.
 */
public class FetchMetaMutationsUseCase {
    private final MutationRepository mutationRepository;
    
    
    public FetchMetaMutationsUseCase(MutationRepository mutationRepository) {
        this.mutationRepository = mutationRepository;
    }

    /**
     * Executes the use case to retrieve metadata about mutations based on the provided filter.
     *
     * @param mutationMultipleStudyFilter filter containing study, molecular profile, sample, and gene identifiers
     * @return aggregated mutation metadata ({@link MutationMeta}) for the given filter
     */
    public MutationMeta execute(MutationMultipleStudyFilter mutationMultipleStudyFilter){
        if(mutationMultipleStudyFilter.getMolecularProfileIds() != null){
            return mutationRepository.getMetaMutationsInMultipleMolecularProfiles(
                mutationMultipleStudyFilter.getMolecularProfileIds(), 
                null,
                mutationMultipleStudyFilter.getEntrezGeneIds());
        }
        List<String> molecularProfileIds = 
            MutationUtil.extractMolecularProfileIds(
            mutationMultipleStudyFilter.getSampleMolecularIdentifiers());
        List<String> sampleIds =
            MutationUtil.extractSampleIds(
            mutationMultipleStudyFilter.getSampleMolecularIdentifiers());
        return mutationRepository.getMetaMutationsInMultipleMolecularProfiles(
            molecularProfileIds, 
            sampleIds, 
            mutationMultipleStudyFilter.getEntrezGeneIds());
    }
}
