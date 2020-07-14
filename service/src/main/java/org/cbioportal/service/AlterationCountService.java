package org.cbioportal.service;

import org.cbioportal.model.*;
import org.cbioportal.model.QueryElement;
import org.cbioportal.model.util.Select;

import java.util.List;

public interface AlterationCountService {

    List<AlterationCountByGene> getSampleAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                          Select<Integer> entrezGeneIds,
                                                          boolean includeFrequency,
                                                          boolean includeMissingAlterationsFromGenePanel,
                                                          Select<MutationEventType> mutationEventTypes,
                                                          Select<CNA> cnaEventTypes,
                                                          QueryElement searchFusions,
                                                          boolean includeDriver,
                                                          boolean includeVUS,
                                                          boolean includeUnknownOncogenicity,
                                                          Select<String> selectedTiers,
                                                          boolean includeUnknownTier,
                                                          boolean includeGermline,
                                                          boolean includeSomatic,
                                                          boolean includeUnknownStatus);

    List<AlterationCountByGene> getPatientAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                           Select<Integer> entrezGeneIds,
                                                           boolean includeFrequency,
                                                           boolean includeMissingAlterationsFromGenePanel,
                                                           Select<MutationEventType> mutationEventTypes,
                                                           Select<CNA> cnaEventTypes,
                                                           QueryElement searchFusions,
                                                           boolean includeDriver,
                                                           boolean includeVUS,
                                                           boolean includeUnknownOncogenicity,
                                                           Select<String> selectedTiers,
                                                           boolean includeUnknownTier,
                                                           boolean includeGermline,
                                                           boolean includeSomatic,
                                                           boolean includeUnknownStatus);

    List<AlterationCountByGene> getSampleMutationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                        Select<Integer> entrezGeneIds,
                                                        boolean includeFrequency,
                                                        boolean includeMissingAlterationsFromGenePanel,
                                                        Select<MutationEventType> mutationEventTypes,
                                                        boolean includeDriver,
                                                        boolean includeVUS,
                                                        boolean includeUnknownOncogenicity,
                                                        Select<String> selectedTiers,
                                                        boolean includeUnknownTier,
                                                        boolean includeGermline,
                                                        boolean includeSomatic,
                                                        boolean includeUnknownStatus);

    List<AlterationCountByGene> getPatientMutationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                         Select<Integer> entrezGeneIds,
                                                         boolean includeFrequency,
                                                         boolean includeMissingAlterationsFromGenePanel,
                                                         Select<MutationEventType> mutationEventTypes,
                                                         boolean includeDriver,
                                                         boolean includeVUS,
                                                         boolean includeUnknownOncogenicity,
                                                         Select<String> selectedTiers,
                                                         boolean includeUnknownTier,
                                                         boolean includeGermline,
                                                         boolean includeSomatic,
                                                         boolean includeUnknownStatus);

    List<AlterationCountByGene> getSampleFusionCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                      Select<Integer> entrezGeneIds,
                                                      boolean includeFrequency,
                                                      boolean includeMissingAlterationsFromGenePanel,
                                                      Select<MutationEventType> mutationEventTypes,
                                                      boolean includeDriver,
                                                      boolean includeVUS,
                                                      boolean includeUnknownOncogenicity,
                                                      Select<String> selectedTiers,
                                                      boolean includeUnknownTier,
                                                      boolean includeGermline,
                                                      boolean includeSomatic,
                                                      boolean includeUnknownStatus);

    List<AlterationCountByGene> getPatientFusionCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                       Select<Integer> entrezGeneIds,
                                                       boolean includeFrequency,
                                                       boolean includeMissingAlterationsFromGenePanel,
                                                       Select<MutationEventType> mutationEventTypes,
                                                       boolean includeDriver,
                                                       boolean includeVUS,
                                                       boolean includeUnknownOncogenicity,
                                                       Select<String> selectedTiers,
                                                       boolean includeUnknownTier,
                                                       boolean includeGermline,
                                                       boolean includeSomatic,
                                                       boolean includeUnknownStatus);

// Should be restored when old CNA count endpoint is retired
//    List<AlterationCountByGene> getSampleCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
//                                                   Select<Integer> entrezGeneIds,
//                                                   boolean includeFrequency,
//                                                   boolean includeMissingAlterationsFromGenePanel,
//                                                   List<CNA> cnaEventTypes,
//                                                   boolean excludeVUS,
//                                                   Select<String> selectedTiers);
//
//    List<AlterationCountByGene> getPatientCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
//                                                    Select<Integer> entrezGeneIds,
//                                                    boolean includeFrequency,
//                                                    boolean includeMissingAlterationsFromGenePanel,
//                                                    List<CNA> cnaEventTypes,
//                                                   boolean excludeVUS,
//                                                   Select<String> selectedTiers);

    // Should be removed when old CNA count endpoint is retired
    List<CopyNumberCountByGene> getSampleCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                   Select<Integer> entrezGeneIds,
                                                   boolean includeFrequency,
                                                   boolean includeMissingAlterationsFromGenePanel,
                                                   Select<CNA> cnaEventTypes,
                                                   boolean includeDriver,
                                                   boolean includeVUS,
                                                   boolean includeUnknownOncogenicity,
                                                   Select<String> selectedTiers, boolean includeUnknownTier);

    List<CopyNumberCountByGene> getPatientCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                    Select<Integer> entrezGeneIds,
                                                    boolean includeFrequency,
                                                    boolean includeMissingAlterationsFromGenePanel,
                                                    Select<CNA> cnaEventTypes,
                                                    boolean includeDriver,
                                                    boolean includeVUS,
                                                    boolean includeUnknownOncogenicity,
                                                    Select<String> selectedTiers, boolean includeUnknownTier);
    
}
