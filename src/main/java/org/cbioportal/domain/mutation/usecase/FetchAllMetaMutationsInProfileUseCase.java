package org.cbioportal.domain.mutation.usecase;

import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
/**
 * Use case for retrieving  MetaMutation data 
 */
public class FetchAllMetaMutationsInProfileUseCase {
    private final MutationRepository mutationRepository;
    
    
    public FetchAllMetaMutationsInProfileUseCase(MutationRepository mutationRepository) {
        this.mutationRepository = mutationRepository;
    }
    public MutationMeta execute(List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds){
        return mutationRepository.getMetaMutationsInMultipleMolecularProfiles(molecularProfileIds, sampleIds, entrezGeneIds);
    }
}
