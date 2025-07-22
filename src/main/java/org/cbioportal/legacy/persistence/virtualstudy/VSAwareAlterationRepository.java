package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationCountByStructuralVariant;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.util.Select;
import org.cbioportal.legacy.persistence.AlterationRepository;

public class VSAwareAlterationRepository implements AlterationRepository {
  private final AlterationRepository alterationRepository;
  private final VirtualizationService virtualizationService;

  public VSAwareAlterationRepository(
      VirtualizationService virtualizationService, AlterationRepository alterationRepository) {
    this.virtualizationService = virtualizationService;
    this.alterationRepository = alterationRepository;
  }

  @Override
  public List<AlterationCountByGene> getSampleAlterationGeneCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      AlterationFilter alterationFilter) {
    return alterationRepository.getSampleAlterationGeneCounts(
        materializeMolecularProfileCaseIdentifiers(molecularProfileCaseIdentifiers),
        entrezGeneIds,
        alterationFilter);
  }

  @Override
  public List<AlterationCountByGene> getPatientAlterationGeneCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      AlterationFilter alterationFilter) {
    return alterationRepository.getPatientAlterationGeneCounts(
        materializeMolecularProfileCaseIdentifiers(molecularProfileCaseIdentifiers),
        entrezGeneIds,
        alterationFilter);
  }

  @Override
  public List<CopyNumberCountByGene> getSampleCnaGeneCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      AlterationFilter alterationFilter) {
    return alterationRepository.getSampleCnaGeneCounts(
        materializeMolecularProfileCaseIdentifiers(molecularProfileCaseIdentifiers),
        entrezGeneIds,
        alterationFilter);
  }

  @Override
  public List<CopyNumberCountByGene> getPatientCnaGeneCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      AlterationFilter alterationFilter) {
    return alterationRepository.getPatientCnaGeneCounts(
        materializeMolecularProfileCaseIdentifiers(molecularProfileCaseIdentifiers),
        entrezGeneIds,
        alterationFilter);
  }

  @Override
  public List<AlterationCountByStructuralVariant> getSampleStructuralVariantCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      AlterationFilter alterationFilter) {
    return alterationRepository.getSampleStructuralVariantCounts(
        materializeMolecularProfileCaseIdentifiers(molecularProfileCaseIdentifiers),
        alterationFilter);
  }

  @Override
  public List<AlterationCountByStructuralVariant> getPatientStructuralVariantCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      AlterationFilter alterationFilter) {
    return alterationRepository.getPatientStructuralVariantCounts(
        materializeMolecularProfileCaseIdentifiers(molecularProfileCaseIdentifiers),
        alterationFilter);
  }

  /**
   * Convert virtual study molecular profile case identifiers to actual case identifiers if needed.
   * If the molecular profile is already a real molecular profile, it will be returned as is.
   */
  private Set<MolecularProfileCaseIdentifier> materializeMolecularProfileCaseIdentifiers(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers) {
    Set<String> molecularProfileIds =
        molecularProfileCaseIdentifiers.stream()
            .map(MolecularProfileCaseIdentifier::getMolecularProfileId)
            .collect(Collectors.toSet());
    Map<String, Pair<String, Set<String>>> molProfDef =
        virtualizationService.getVirtualMolecularProfileDefinition(molecularProfileIds);
    return molecularProfileCaseIdentifiers.stream()
        .map(
            molecularProfileCaseIdentifier -> {
              String molecularProfileId = molecularProfileCaseIdentifier.getMolecularProfileId();
              Pair<String, Set<String>> molProfDefPair = molProfDef.get(molecularProfileId);
              String materializeMolecularProfileId = molProfDefPair.getLeft();
              Set<String> stableSampleIds = molProfDefPair.getRight();
              if (stableSampleIds != null
                  && !stableSampleIds.contains(molecularProfileCaseIdentifier.getCaseId())) {
                return null;
              }
              return new MolecularProfileCaseIdentifier(
                  molecularProfileCaseIdentifier.getCaseId(), materializeMolecularProfileId);
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }
}
