package org.cbioportal.service;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.*;
import org.cbioportal.model.util.Select;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
import org.cbioportal.web.parameter.CustomSampleIdentifier;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.util.List;

public interface AlterationCountService {

    Pair<List<AlterationCountByGene>, Long> getSampleAlterationGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                          Select<Integer> entrezGeneIds,
                                                                          boolean includeFrequency,
                                                                          boolean includeMissingAlterationsFromGenePanel,
                                                                          AlterationFilter alterationFilter);

    Pair<List<AlterationCountByGene>, Long> getPatientAlterationGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                           Select<Integer> entrezGeneIds,
                                                                           boolean includeFrequency,
                                                                           boolean includeMissingAlterationsFromGenePanel,
                                                                           AlterationFilter alterationFilter);

    Pair<List<AlterationCountByGene>, Long> getSampleMutationGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                        Select<Integer> entrezGeneIds,
                                                                        boolean includeFrequency,
                                                                        boolean includeMissingAlterationsFromGenePanel,
                                                                        AlterationFilter alterationFilter);

    Pair<List<AlterationCountByGene>, Long> getPatientMutationGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                         Select<Integer> entrezGeneIds,
                                                                         boolean includeFrequency,
                                                                         boolean includeMissingAlterationsFromGenePanel,
                                                                         AlterationFilter alterationFilter);

    Pair<List<AlterationCountByGene>, Long> getSampleStructuralVariantGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                 Select<Integer> entrezGeneIds,
                                                                                 boolean includeFrequency,
                                                                                 boolean includeMissingAlterationsFromGenePanel,
                                                                                 AlterationFilter alterationFilter);

    Pair<List<AlterationCountByGene>, Long> getPatientStructuralVariantGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                  Select<Integer> entrezGeneIds,
                                                                                  boolean includeFrequency,
                                                                                  boolean includeMissingAlterationsFromGenePanel,
                                                                                  AlterationFilter alterationFilter);

    Pair<List<AlterationCountByStructuralVariant>, Long> getSampleStructuralVariantCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                          boolean includeFrequency,
                                                                                          boolean includeMissingAlterationsFromGenePanel,
                                                                                          AlterationFilter alterationFilter);

// Should be restored when old CNA count endpoint is retired
//    Pair<List<AlterationCountByGene>, Long> getSampleCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
//                                                   Select<Integer> entrezGeneIds,
//                                                   boolean includeFrequency,
//                                                   boolean includeMissingAlterationsFromGenePanel,
//                                                   AlterationEventTypeFilter alterationFilter);
//
//    Pair<List<AlterationCountByGene>, Long> getPatientCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
//                                                    Select<Integer> entrezGeneIds,
//                                                    boolean includeFrequency,
//                                                    boolean includeMissingAlterationsFromGenePanel,
//                                                   AlterationEventTypeFilter alterationFilter);

    // Should be removed when old CNA count endpoint is retired
    Pair<List<CopyNumberCountByGene>, Long> getSampleCnaGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                   Select<Integer> entrezGeneIds,
                                                                   boolean includeFrequency,
                                                                   boolean includeMissingAlterationsFromGenePanel,
                                                                   AlterationFilter alterationFilter);

    Pair<List<CopyNumberCountByGene>, Long> getPatientCnaGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                    Select<Integer> entrezGeneIds,
                                                                    boolean includeFrequency,
                                                                    boolean includeMissingAlterationsFromGenePanel,
                                                                    AlterationFilter alterationFilter);
}
