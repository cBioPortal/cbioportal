package org.cbioportal.service;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.*;
import org.cbioportal.model.QueryElement;
import org.cbioportal.model.util.Select;

import java.util.List;

public interface AlterationCountService {

    Pair<List<AlterationCountByGene>, Long> getSampleAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                      Select<Integer> entrezGeneIds,
                                                                      boolean includeFrequency,
                                                                      boolean includeMissingAlterationsFromGenePanel,
                                                                      Select<MutationEventType> mutationEventTypes,
                                                                      Select<CNA> cnaEventTypes,
                                                                      QueryElement searchFusions);

    Pair<List<AlterationCountByGene>, Long> getPatientAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                       Select<Integer> entrezGeneIds,
                                                                       boolean includeFrequency,
                                                                       boolean includeMissingAlterationsFromGenePanel,
                                                                       Select<MutationEventType> mutationEventTypes,
                                                                       Select<CNA> cnaEventTypes,
                                                                       QueryElement searchFusions);

    Pair<List<AlterationCountByGene>, Long> getSampleMutationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                    Select<Integer> entrezGeneIds,
                                                                    boolean includeFrequency,
                                                                    boolean includeMissingAlterationsFromGenePanel,
                                                                    Select<MutationEventType> mutationEventTypes);

    Pair<List<AlterationCountByGene>, Long> getPatientMutationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                     Select<Integer> entrezGeneIds,
                                                                     boolean includeFrequency,
                                                                     boolean includeMissingAlterationsFromGenePanel,
                                                                     Select<MutationEventType> mutationEventTypes);

    Pair<List<AlterationCountByGene>, Long> getSampleFusionCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                  Select<Integer> entrezGeneIds,
                                                                  boolean includeFrequency,
                                                                  boolean includeMissingAlterationsFromGenePanel,
                                                                  Select<MutationEventType> mutationEventTypes);

    Pair<List<AlterationCountByGene>, Long> getPatientFusionCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                   Select<Integer> entrezGeneIds,
                                                                   boolean includeFrequency,
                                                                   boolean includeMissingAlterationsFromGenePanel,
                                                                   Select<MutationEventType> mutationEventTypes);

// Should be restored when old CNA count endpoint is retired
//    List<AlterationCountByGene> getSampleCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
//                                                   Select<Integer> entrezGeneIds,
//                                                   boolean includeFrequency,
//                                                   boolean includeMissingAlterationsFromGenePanel,
//                                                   List<CNA> cnaEventTypes);
//
//    List<AlterationCountByGene> getPatientCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
//                                                    Select<Integer> entrezGeneIds,
//                                                    boolean includeFrequency,
//                                                    boolean includeMissingAlterationsFromGenePanel,
//                                                    List<CNA> cnaEventTypes);
    
// Should be removed when old CNA count endpoint is retired
    Pair<List<CopyNumberCountByGene>, Long> getSampleCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                               Select<Integer> entrezGeneIds,
                                                               boolean includeFrequency,
                                                               boolean includeMissingAlterationsFromGenePanel,
                                                               Select<CNA> cnaEventTypes);

    Pair<List<CopyNumberCountByGene>, Long> getPatientCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                Select<Integer> entrezGeneIds,
                                                                boolean includeFrequency,
                                                                boolean includeMissingAlterationsFromGenePanel,
                                                                Select<CNA> cnaEventTypes);
    
}
