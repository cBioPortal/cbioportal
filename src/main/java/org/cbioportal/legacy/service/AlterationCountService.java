package org.cbioportal.legacy.service;

import java.util.List;
import org.apache.commons.math3.util.Pair;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationCountByStructuralVariant;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.util.Select;

public interface AlterationCountService {

  Pair<List<AlterationCountByGene>, Long> getSampleAlterationGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter);

  Pair<List<AlterationCountByGene>, Long> getPatientAlterationGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter);

  Pair<List<AlterationCountByGene>, Long> getSampleMutationGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter);

  Pair<List<AlterationCountByGene>, Long> getPatientMutationGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter);

  Pair<List<AlterationCountByGene>, Long> getSampleStructuralVariantGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter);

  Pair<List<AlterationCountByGene>, Long> getPatientStructuralVariantGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter);

  Pair<List<AlterationCountByStructuralVariant>, Long> getSampleStructuralVariantCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter);

  // Should be restored when old CNA count endpoint is retired
  //    Pair<List<AlterationCountByGene>, Long>
  // getSampleCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
  //                                                   Select<Integer> entrezGeneIds,
  //                                                   boolean includeFrequency,
  //                                                   boolean
  // includeMissingAlterationsFromGenePanel,
  //                                                   AlterationEventTypeFilter alterationFilter);
  //
  //    Pair<List<AlterationCountByGene>, Long>
  // getPatientCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
  //                                                    Select<Integer> entrezGeneIds,
  //                                                    boolean includeFrequency,
  //                                                    boolean
  // includeMissingAlterationsFromGenePanel,
  //                                                   AlterationEventTypeFilter alterationFilter);

  // Should be removed when old CNA count endpoint is retired
  Pair<List<CopyNumberCountByGene>, Long> getSampleCnaGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter);

  Pair<List<CopyNumberCountByGene>, Long> getPatientCnaGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter);
}
