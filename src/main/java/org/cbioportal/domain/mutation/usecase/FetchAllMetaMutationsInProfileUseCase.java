package org.cbioportal.domain.mutation.usecase;

import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.domain.mutation.util.MutationUtil;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
