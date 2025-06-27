package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationCountByStructuralVariant;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.util.Select;
import org.cbioportal.legacy.persistence.AlterationRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.VirtualStudy;

public class VSAwareAlterationRepository implements AlterationRepository {
  private final AlterationRepository alterationRepository;
  private final VirtualStudyService virtualStudyService;

  public VSAwareAlterationRepository(
      VirtualStudyService virtualStudyService, AlterationRepository alterationRepository) {
    this.virtualStudyService = virtualStudyService;
    this.alterationRepository = alterationRepository;
  }

  @Override
  public List<AlterationCountByGene> getSampleAlterationGeneCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      AlterationFilter alterationFilter) {
    return alterationRepository.getSampleAlterationGeneCounts(
        expandMolecularProfileCaseIdentifiers(molecularProfileCaseIdentifiers),
        entrezGeneIds,
        alterationFilter);
  }

  @Override
  public List<AlterationCountByGene> getPatientAlterationGeneCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      AlterationFilter alterationFilter) {
    return alterationRepository.getPatientAlterationGeneCounts(
        expandMolecularProfileCaseIdentifiers(molecularProfileCaseIdentifiers),
        entrezGeneIds,
        alterationFilter);
  }

  @Override
  public List<CopyNumberCountByGene> getSampleCnaGeneCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      AlterationFilter alterationFilter) {
    return alterationRepository.getSampleCnaGeneCounts(
        expandMolecularProfileCaseIdentifiers(molecularProfileCaseIdentifiers),
        entrezGeneIds,
        alterationFilter);
  }

  @Override
  public List<CopyNumberCountByGene> getPatientCnaGeneCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      AlterationFilter alterationFilter) {
    return alterationRepository.getPatientCnaGeneCounts(
        expandMolecularProfileCaseIdentifiers(molecularProfileCaseIdentifiers),
        entrezGeneIds,
        alterationFilter);
  }

  @Override
  public List<AlterationCountByStructuralVariant> getSampleStructuralVariantCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      AlterationFilter alterationFilter) {
    return alterationRepository.getSampleStructuralVariantCounts(
        expandMolecularProfileCaseIdentifiers(molecularProfileCaseIdentifiers), alterationFilter);
  }

  @Override
  public List<AlterationCountByStructuralVariant> getPatientStructuralVariantCounts(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      AlterationFilter alterationFilter) {
    return alterationRepository.getPatientStructuralVariantCounts(
        expandMolecularProfileCaseIdentifiers(molecularProfileCaseIdentifiers), alterationFilter);
  }

  // TODO improve performance
  // FIXME detection of virtual study identifiers is ambiguous here
  private Set<MolecularProfileCaseIdentifier> expandMolecularProfileCaseIdentifiers(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers) {
    Map<String, Map<String, ImmutablePair<String, String>>> virtualStudyIds =
        virtualStudyService.getPublishedVirtualStudies().stream()
            .collect(
                Collectors.toMap(
                    VirtualStudy::getId,
                    vs -> {
                      return vs.getData().getStudies().stream()
                          .flatMap(
                              vss ->
                                  vss.getSamples().stream()
                                      .map(s -> ImmutablePair.of(vss.getId(), s)))
                          .collect(Collectors.toMap(p -> p.getLeft() + "_" + p.getRight(), p -> p));
                    }));
    return molecularProfileCaseIdentifiers.stream()
        .map(
            mpci -> {
              List<String> matchingVirtualStudyIds =
                  virtualStudyIds.keySet().stream()
                      .filter(vsid -> mpci.getMolecularProfileId().startsWith(vsid + "_"))
                      .toList();
              if (matchingVirtualStudyIds.isEmpty()) {
                return mpci;
              }
              if (matchingVirtualStudyIds.size() > 1) {
                throw new IllegalArgumentException(
                    "Multiple virtual studies ("
                        + String.join(", ", matchingVirtualStudyIds)
                        + ") found for molecular profile ID: "
                        + mpci.getMolecularProfileId());
              }
              Map<String, ImmutablePair<String, String>> virtualStudySamples =
                  virtualStudyIds.get(matchingVirtualStudyIds.getFirst());
              ImmutablePair<String, String> pair = virtualStudySamples.get(mpci.getCaseId());
              // TODO shouldn't be that conditional
              String actualCaseId = pair != null ? pair.getRight() : mpci.getCaseId();
              MolecularProfileCaseIdentifier mpciExpanded =
                  new MolecularProfileCaseIdentifier(
                      actualCaseId,
                      mpci.getMolecularProfileId()
                          .replace(matchingVirtualStudyIds.getFirst() + "_", ""));
              return mpciExpanded;
            })
        .collect(Collectors.toSet());
  }
}
