package org.cbioportal.domain.mutation.usecase;

import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.domain.mutation.util.MutationUtil;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Profile("clickhouse")
/**
 * Use case for retrieving Mutation data 
 */
public class FetchAllMutationsInProfileUseCase {
    private final MutationRepository mutationRepository;

    public FetchAllMutationsInProfileUseCase(MutationRepository mutationRepository) {
        this.mutationRepository = mutationRepository;
    }

    public List<Mutation> execute(MutationMultipleStudyFilter mutationMultipleStudyFilter,
                                  String projection,
                                  Integer pageSize,
                                  Integer pageNumber,
                                  String sortBy,
                                  String direction) {
        if(mutationMultipleStudyFilter.getMolecularProfileIds() != null){
            return mutationRepository.getMutationsInMultipleMolecularProfiles(
                mutationMultipleStudyFilter.getMolecularProfileIds(),
                null,
                mutationMultipleStudyFilter.getEntrezGeneIds(),
                projection,
                pageSize,
                pageNumber,
                sortBy,
                direction);

        }
        
        List<String> molecularProfileIds=
            MutationUtil.extractMolecularProfileIds(mutationMultipleStudyFilter.getSampleMolecularIdentifiers());
        List<String> sampleIds= 
            MutationUtil.extractSampleIds(mutationMultipleStudyFilter.getSampleMolecularIdentifiers());
        return mutationRepository.getMutationsInMultipleMolecularProfiles(
            molecularProfileIds,
            sampleIds,
            mutationMultipleStudyFilter.getEntrezGeneIds(),
            projection,
            pageSize,
            pageNumber,
            sortBy,
            direction); 
    }
}
