package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import java.util.Set;
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
        virtualizationService.toMaterializedMolecularProfileIds(molecularProfileCaseIdentifiers),
        entrezGeneIds,
        alterationFilter);
  }

  @Override
  public List<AlterationCountByGene> getPatientAlterationGeneCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      AlterationFilter alterationFilter) {
    return alterationRepository.getPatientAlterationGeneCounts(
        virtualizationService.toMaterializedMolecularProfileIds(molecularProfileCaseIdentifiers),
        entrezGeneIds,
        alterationFilter);
  }

  @Override
  public List<CopyNumberCountByGene> getSampleCnaGeneCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      AlterationFilter alterationFilter) {
    return alterationRepository.getSampleCnaGeneCounts(
        virtualizationService.toMaterializedMolecularProfileIds(molecularProfileCaseIdentifiers),
        entrezGeneIds,
        alterationFilter);
  }

  @Override
  public List<CopyNumberCountByGene> getPatientCnaGeneCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      AlterationFilter alterationFilter) {
    return alterationRepository.getPatientCnaGeneCounts(
        virtualizationService.toMaterializedMolecularProfileIds(molecularProfileCaseIdentifiers),
        entrezGeneIds,
        alterationFilter);
  }

  @Override
  public List<AlterationCountByStructuralVariant> getSampleStructuralVariantCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      AlterationFilter alterationFilter) {
    return alterationRepository.getSampleStructuralVariantCounts(
        virtualizationService.toMaterializedMolecularProfileIds(molecularProfileCaseIdentifiers),
        alterationFilter);
  }

  @Override
  public List<AlterationCountByStructuralVariant> getPatientStructuralVariantCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      AlterationFilter alterationFilter) {
    return alterationRepository.getPatientStructuralVariantCounts(
        virtualizationService.toMaterializedMolecularProfileIds(molecularProfileCaseIdentifiers),
        alterationFilter);
  }
}
