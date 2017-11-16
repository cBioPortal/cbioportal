package org.cbioportal.service.impl;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.VariantCount;
import org.cbioportal.persistence.VariantCountRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.VariantCountService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VariantCountServiceImpl implements VariantCountService {
    
    @Autowired
    private VariantCountRepository variantCountRepository;
    @Autowired
    private MutationService mutationService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    
    @Override
    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    public List<VariantCount> fetchVariantCounts(String molecularProfileId, List<Integer> entrezGeneIds, 
                                                 List<String> keywords) throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);
        
        Integer numberOfSamplesInMolecularProfile = mutationService.fetchMetaMutationsInMolecularProfile(
            molecularProfileId, null, null).getSampleCount();
        
        List<VariantCount> variantCounts = variantCountRepository.fetchVariantCounts(molecularProfileId, entrezGeneIds, 
            keywords);
        variantCounts.forEach(v -> v.setNumberOfSamples(numberOfSamplesInMolecularProfile));
        return variantCounts;
    }

    private void validateMolecularProfile(String molecularProfileId) throws MolecularProfileNotFoundException {

        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);

        if (!molecularProfile.getMolecularAlterationType()
            .equals(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED)) {

            throw new MolecularProfileNotFoundException(molecularProfileId);
        }
    }
}
