package org.cbioportal.domain.mutation.usecase;

import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.Mutation;

import java.util.ArrayList;
import java.util.List;

public class FetchAllMutationsInProfileUseCase {
    private final MutationRepository mutationRepository;

    public FetchAllMutationsInProfileUseCase(MutationRepository mutationRepository) {
        this.mutationRepository = mutationRepository;
    }

    public List<Mutation> execute(String molecularProfileId,
                                  List<String> sampleIds,
                                  List<Integer> entrezGeneIds,
                                  boolean snpOnly,
                                  String projection,
                                  Integer pageSize,
                                  Integer pageNumber,
                                  String sortBy,
                                  String direction) {
        
        //First validate the molecular profile exists then 
        // return the list from repos with a list of mutations 
        return new ArrayList<Mutation>(); 
    }
}
