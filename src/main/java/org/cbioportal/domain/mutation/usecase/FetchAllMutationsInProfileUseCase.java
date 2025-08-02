package org.cbioportal.domain.mutation.usecase;

import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.Mutation;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class FetchAllMutationsInProfileUseCase {
    private final MutationRepository mutationRepository;

    public FetchAllMutationsInProfileUseCase(MutationRepository mutationRepository) {
        this.mutationRepository = mutationRepository;
    }

    public List<Mutation> execute(List<String> molecularProfileIds,
                                  List<String> sampleIds,
                                  List<Integer> entrezGeneIds,
                                  String projection,
                                  Integer pageSize,
                                  Integer pageNumber,
                                  String sortBy,
                                  String direction) {
        
        return mutationRepository.getMutationsInMultipleMolecularProfiles(
            molecularProfileIds,
            sampleIds,
            entrezGeneIds,
            projection,
            pageSize,
            pageNumber,
            sortBy,
            direction); 
    }
}
