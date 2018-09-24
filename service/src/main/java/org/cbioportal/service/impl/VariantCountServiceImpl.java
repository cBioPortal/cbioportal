package org.cbioportal.service.impl;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.VariantCount;
import org.cbioportal.persistence.VariantCountRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.VariantCountService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VariantCountServiceImpl implements VariantCountService {

    private static final String SEQUENCED_LIST_SUFFIX = "_sequenced";
    
    @Autowired
    private VariantCountRepository variantCountRepository;
    @Autowired
    private MutationService mutationService;
    @Autowired
    private SampleListService sampleListService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    
    @Override
    public List<VariantCount> fetchVariantCounts(String molecularProfileId, List<Integer> entrezGeneIds, 
                                                 List<String> keywords) throws MolecularProfileNotFoundException {

        MolecularProfile molecularProfile = validateMolecularProfile(molecularProfileId);
        
        Integer numberOfSamplesInMolecularProfile = getNumberOfSamplesInMolecularProfile(molecularProfile);
        
        List<VariantCount> variantCounts = variantCountRepository.fetchVariantCounts(molecularProfileId, entrezGeneIds, 
            keywords);
        variantCounts.forEach(v -> v.setNumberOfSamples(numberOfSamplesInMolecularProfile));
        return variantCounts;
    }

	private Integer getNumberOfSamplesInMolecularProfile(MolecularProfile molecularProfile)
			throws MolecularProfileNotFoundException {

		try {
            return sampleListService.getSampleList(
                molecularProfile.getCancerStudyIdentifier() + SEQUENCED_LIST_SUFFIX).getSampleCount();
        } catch (SampleListNotFoundException ex) {
            return mutationService.fetchMetaMutationsInMolecularProfile(molecularProfile.getStableId(), null, null)
                .getSampleCount();
        }
	}

    private MolecularProfile validateMolecularProfile(String molecularProfileId) throws MolecularProfileNotFoundException {

        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);

        if (!(molecularProfile.getMolecularAlterationType()
            .equals(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED) || molecularProfile.getMolecularAlterationType()
            .equals(MolecularProfile.MolecularAlterationType.MUTATION_UNCALLED))) {

            throw new MolecularProfileNotFoundException(molecularProfileId);
        }

        return molecularProfile;
    }
}
