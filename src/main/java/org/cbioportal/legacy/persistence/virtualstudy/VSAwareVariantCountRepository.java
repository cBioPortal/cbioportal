package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.VariantCount;
import org.cbioportal.legacy.persistence.VariantCountRepository;

public class VSAwareVariantCountRepository implements VariantCountRepository {

  private final VariantCountRepository variantCountRepository;
  private final VirtualizationService virtualizationService;

  public VSAwareVariantCountRepository(
      VirtualizationService virtualizationService, VariantCountRepository variantCountRepository) {
    this.virtualizationService = virtualizationService;
    this.variantCountRepository = variantCountRepository;
  }

  @Override
  public List<VariantCount> fetchVariantCounts(
      String molecularProfileId, List<Integer> entrezGeneIds, List<String> keywords) {
    return virtualizationService.handleMolecularData(
        molecularProfileId,
        mpid -> variantCountRepository.fetchVariantCounts(mpid, entrezGeneIds, keywords),
        VSAwareVariantCountRepository::virtualizeVariantCount);
  }

  private static VariantCount virtualizeVariantCount(
      MolecularProfile molecularProfile, VariantCount variantCount) {
    VariantCount newVariantCount = new VariantCount();
    newVariantCount.setMolecularProfileId(molecularProfile.getStableId());
    newVariantCount.setEntrezGeneId(variantCount.getEntrezGeneId());
    newVariantCount.setNumberOfSamples(variantCount.getNumberOfSamples());
    newVariantCount.setKeyword(variantCount.getKeyword());
    newVariantCount.setNumberOfSamplesWithKeyword(variantCount.getNumberOfSamplesWithKeyword());
    newVariantCount.setNumberOfSamplesWithMutationInGene(
        variantCount.getNumberOfSamplesWithMutationInGene());
    return newVariantCount;
  }
}
