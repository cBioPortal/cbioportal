package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.VariantCount;
import org.cbioportal.legacy.persistence.VariantCountRepository;
import org.cbioportal.legacy.service.VirtualStudyService;

public class VSAwareVariantCountRepository implements VariantCountRepository {

  private final VirtualStudyService virtualStudyService;
  private final VariantCountRepository variantCountRepository;

  public VSAwareVariantCountRepository(
      VirtualStudyService virtualStudyService, VariantCountRepository variantCountRepository) {
    this.virtualStudyService = virtualStudyService;
    this.variantCountRepository = variantCountRepository;
  }

  @Override
  public List<VariantCount> fetchVariantCounts(
      String molecularProfileId, List<Integer> entrezGeneIds, List<String> keywords) {
    Map<String, Pair<String, String>> molecularProfileInfo =
        virtualStudyService.toMolecularProfileInfo(Set.of(molecularProfileId));
    if (molecularProfileInfo.isEmpty() || !molecularProfileInfo.containsKey(molecularProfileId)) {
      return variantCountRepository.fetchVariantCounts(molecularProfileId, entrezGeneIds, keywords);
    }
    List<VariantCount> result = new ArrayList<>();
    Pair<String, String> info = molecularProfileInfo.get(molecularProfileId);
    String materialisedMolecularProfileId = info.getRight();
    for (VariantCount variantCount :
        variantCountRepository.fetchVariantCounts(
            materialisedMolecularProfileId, entrezGeneIds, keywords)) {
      VariantCount newVariantCount = virtualizeVariantCount(molecularProfileId, variantCount);
      result.add(newVariantCount);
    }
    return result;
  }

  private static VariantCount virtualizeVariantCount(
      String molecularProfileId, VariantCount variantCount) {
    VariantCount newVariantCount = new VariantCount();
    newVariantCount.setMolecularProfileId(molecularProfileId);
    newVariantCount.setEntrezGeneId(variantCount.getEntrezGeneId());
    newVariantCount.setNumberOfSamples(variantCount.getNumberOfSamples());
    newVariantCount.setKeyword(variantCount.getKeyword());
    newVariantCount.setNumberOfSamplesWithKeyword(variantCount.getNumberOfSamplesWithKeyword());
    newVariantCount.setNumberOfSamplesWithMutationInGene(
        variantCount.getNumberOfSamplesWithMutationInGene());
    return newVariantCount;
  }
}
