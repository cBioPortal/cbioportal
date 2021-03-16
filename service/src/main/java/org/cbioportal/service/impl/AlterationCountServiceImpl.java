package org.cbioportal.service.impl;

import org.apache.commons.math3.util.Pair;

import org.cbioportal.model.*;
import org.cbioportal.model.QueryElement;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.AlterationRepository;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AlterationCountServiceImpl implements AlterationCountService {

    @Autowired
    private AlterationRepository alterationRepository;
    @Autowired
    private AlterationEnrichmentUtil<AlterationCountByGene> alterationEnrichmentUtil;
    @Autowired
    private AlterationEnrichmentUtil<CopyNumberCountByGene> alterationEnrichmentUtilCna;

    @Override
    public Pair<List<AlterationCountByGene>, Long> getSampleAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                             Select<Integer> entrezGeneIds,
                                                                             boolean includeFrequency,
                                                                             boolean includeMissingAlterationsFromGenePanel,
                                                                             Select<MutationEventType> mutationEventTypes,
                                                                             Select<CNA> cnaEventTypes,
                                                                             QueryElement searchFusions) {

        List<AlterationCountByGene> alterationCountByGenes;
        Long profiledCasesCount = 0L;
        if (molecularProfileCaseIdentifiers.isEmpty()) {
            alterationCountByGenes = Collections.emptyList();
        } else {
            List<MolecularProfileCaseIdentifier> updatedProfileCaseIdentifiers = molecularProfileCaseIdentifiers
                .stream()
                .map(molecularProfileCaseIdentifier -> {
                    molecularProfileCaseIdentifier.setMolecularProfileId(molecularProfileCaseIdentifier.getMolecularProfileId().replace("_fusion", "_mutations"));
                    return molecularProfileCaseIdentifier;
                })
                .distinct()
                .collect(Collectors.toList());
            alterationCountByGenes = alterationRepository.getSampleAlterationCounts(
                updatedProfileCaseIdentifiers,
                entrezGeneIds,
                mutationEventTypes,
                cnaEventTypes,
                searchFusions);
            if (includeFrequency) {
                profiledCasesCount = alterationEnrichmentUtil.includeFrequencyForSamples(updatedProfileCaseIdentifiers,
                    alterationCountByGenes,
                    includeMissingAlterationsFromGenePanel);
            }
        }
        return new Pair<>(alterationCountByGenes, profiledCasesCount);
    }
    
    @Override
    public Pair<List<AlterationCountByGene>, Long> getPatientAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                              Select<Integer> entrezGeneIds,
                                                                              boolean includeFrequency,
                                                                              boolean includeMissingAlterationsFromGenePanel,
                                                                              Select<MutationEventType> mutationEventTypes,
                                                                              Select<CNA> cnaEventTypes,
                                                                              QueryElement searchFusions) {

        List<AlterationCountByGene> alterationCountByGenes;
        Long profiledCasesCount = 0L;
        if (molecularProfileCaseIdentifiers.isEmpty()) {
            alterationCountByGenes = Collections.emptyList();
        } else {
            List<MolecularProfileCaseIdentifier> updatedProfileCaseIdentifiers = molecularProfileCaseIdentifiers
                .stream()
                .map(molecularProfileCaseIdentifier -> {
                    molecularProfileCaseIdentifier.setMolecularProfileId(molecularProfileCaseIdentifier.getMolecularProfileId().replace("_fusion", "_mutations"));
                    return molecularProfileCaseIdentifier;
                })
                .distinct()
                .collect(Collectors.toList());
            alterationCountByGenes = alterationRepository.getPatientAlterationCounts(
                updatedProfileCaseIdentifiers,
                entrezGeneIds,
                mutationEventTypes,
                cnaEventTypes,
                searchFusions);
            if (includeFrequency) {
                profiledCasesCount = alterationEnrichmentUtil.includeFrequencyForPatients(updatedProfileCaseIdentifiers, alterationCountByGenes, includeMissingAlterationsFromGenePanel);
            }
        }
        return new Pair<>(alterationCountByGenes, profiledCasesCount);
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getSampleMutationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                           Select<Integer> entrezGeneIds,
                                                                           boolean includeFrequency,
                                                                           boolean includeMissingAlterationsFromGenePanel,
                                                                           Select<MutationEventType> mutationEventTypes) {
        return getSampleAlterationCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            mutationEventTypes,
            Select.none(),
            QueryElement.INACTIVE
        );
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getPatientMutationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                            Select<Integer> entrezGeneIds,
                                                                            boolean includeFrequency,
                                                                            boolean includeMissingAlterationsFromGenePanel,
                                                                            Select<MutationEventType> mutationEventTypes) {
        return getPatientAlterationCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            mutationEventTypes,
            Select.none(),
            QueryElement.INACTIVE
        );
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long>  getSampleStructuralVariantCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                             Select<Integer> entrezGeneIds,
                                                             boolean includeFrequency,
                                                             boolean includeMissingAlterationsFromGenePanel) {
        return getSampleAlterationCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            Select.all(),
            Select.none(),
            QueryElement.ACTIVE
        );
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long>  getPatientStructuralVariantCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                              Select<Integer> entrezGeneIds,
                                                              boolean includeFrequency,
                                                              boolean includeMissingAlterationsFromGenePanel) {
        return getPatientAlterationCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            Select.all(),
            Select.none(),
            QueryElement.ACTIVE
            );    
        }
            
// -- Should be reinstated when the legacy CNA count endpoint retires            
//    @Override
//    public List<AlterationCountByGene> getSampleCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
//                                                          Select<Integer> entrezGeneIds,
//                                                          boolean includeFrequency,
//                                                          boolean includeMissingAlterationsFromGenePanel,
//                                                          List<CNA> cnaEventTypes) {
//        return getSampleAlterationCounts(molecularProfileCaseIdentifiers,
//            entrezGeneIds,
//            includeFrequency,
//            includeMissingAlterationsFromGenePanel,
//            new ArrayList<>(),
//            cnaEventTypes,
//            false);
//    }
//
//    @Override
//    public List<AlterationCountByGene> getPatientCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
//                                                           List<Integer> entrezGeneIds,
//                                                           boolean includeFrequency,
//                                                           boolean includeMissingAlterationsFromGenePanel,
//                                                           List<CNA> cnaEventTypes) {
//        return getPatientAlterationCounts(molecularProfileCaseIdentifiers,
//            entrezGeneIds,
//            includeFrequency,
//            includeMissingAlterationsFromGenePanel,
//            new ArrayList<>(),
//            cnaEventTypes,
//            false);
//    }
    
    @Override
    public Pair<List<CopyNumberCountByGene>, Long> getSampleCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                      Select<Integer> entrezGeneIds,
                                                                      boolean includeFrequency,
                                                                      boolean includeMissingAlterationsFromGenePanel,
                                                                      Select<CNA> cnaEventTypes) {
        List<CopyNumberCountByGene> alterationCountByGenes;
        Long profiledCasesCount = 0L;
        if (molecularProfileCaseIdentifiers.isEmpty()) {
            alterationCountByGenes = Collections.emptyList();
        } else {
            alterationCountByGenes = alterationRepository.getSampleCnaCounts(
                molecularProfileCaseIdentifiers,
                entrezGeneIds,
                cnaEventTypes);
            if (includeFrequency) {
                profiledCasesCount = alterationEnrichmentUtilCna.includeFrequencyForSamples(molecularProfileCaseIdentifiers, alterationCountByGenes, includeMissingAlterationsFromGenePanel);
            }
        }

        return new Pair<>(alterationCountByGenes, profiledCasesCount);
    }

    @Override
    public Pair<List<CopyNumberCountByGene>, Long> getPatientCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                       Select<Integer> entrezGeneIds,
                                                                       boolean includeFrequency,
                                                                       boolean includeMissingAlterationsFromGenePanel,
                                                                       Select<CNA> cnaEventTypes) {
        List<CopyNumberCountByGene> alterationCountByGenes;
        Long profiledCasesCount = 0L;
        if (molecularProfileCaseIdentifiers.isEmpty()) {
            alterationCountByGenes = Collections.emptyList();
        } else {
            alterationCountByGenes = alterationRepository.getPatientCnaCounts(
                molecularProfileCaseIdentifiers,
                entrezGeneIds,
                cnaEventTypes);
            if (includeFrequency) {
                profiledCasesCount = alterationEnrichmentUtilCna.includeFrequencyForPatients(molecularProfileCaseIdentifiers, alterationCountByGenes, includeMissingAlterationsFromGenePanel);
            }
        }

        return new Pair<>(alterationCountByGenes, profiledCasesCount);
    }

}
