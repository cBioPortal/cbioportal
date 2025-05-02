package org.cbioportal.legacy.service.impl;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.legacy.model.AlterationCountBase;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationCountByStructuralVariant;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.GenePanel;
import org.cbioportal.legacy.model.GenePanelData;
import org.cbioportal.legacy.model.GenePanelToGene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.util.Select;
import org.cbioportal.legacy.persistence.AlterationRepository;
import org.cbioportal.legacy.persistence.MolecularProfileRepository;
import org.cbioportal.legacy.service.AlterationCountService;
import org.cbioportal.legacy.service.GenePanelService;
import org.cbioportal.legacy.service.util.AlterationCountServiceUtil;
import org.cbioportal.legacy.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AlterationCountServiceImpl implements AlterationCountService {

    private final AlterationRepository alterationRepository;
    private final AlterationEnrichmentUtil<AlterationCountByGene> alterationEnrichmentUtil;
    private final AlterationEnrichmentUtil<CopyNumberCountByGene> alterationEnrichmentUtilCna;
    private final AlterationEnrichmentUtil<AlterationCountByStructuralVariant> alterationEnrichmentUtilStructVar;
    private final MolecularProfileRepository molecularProfileRepository;
    private final GenePanelService genePanelService;
    
    @Autowired
    public AlterationCountServiceImpl(AlterationRepository alterationRepository, AlterationEnrichmentUtil<AlterationCountByGene> alterationEnrichmentUtil,
                                      AlterationEnrichmentUtil<CopyNumberCountByGene> alterationEnrichmentUtilCna,
                                      AlterationEnrichmentUtil<AlterationCountByStructuralVariant> alterationEnrichmentUtilStructVar,
                                      MolecularProfileRepository molecularProfileRepository,
                                      GenePanelService genePanelService) {
        this.alterationRepository = alterationRepository;
        this.alterationEnrichmentUtil = alterationEnrichmentUtil;
        this.alterationEnrichmentUtilCna = alterationEnrichmentUtilCna;
        this.alterationEnrichmentUtilStructVar = alterationEnrichmentUtilStructVar;
        this.molecularProfileRepository = molecularProfileRepository;
        this.genePanelService = genePanelService;
    }
    
    @Override
    public Pair<List<AlterationCountByGene>, Long> getSampleAlterationGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                 Select<Integer> entrezGeneIds,
                                                                                 boolean includeFrequency,
                                                                                 boolean includeMissingAlterationsFromGenePanel,
                                                                                 AlterationFilter alterationFilter,
                                                                                 boolean includeOffPanelAlterations) {

        Function<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>> dataFetcher = profileCaseIdentifiers ->
            alterationRepository.getSampleAlterationGeneCounts(new TreeSet<>(profileCaseIdentifiers), entrezGeneIds, alterationFilter);

        BiFunction<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>, Long> includeFrequencyFunction =
            (a, b) -> alterationEnrichmentUtil.includeFrequencyForSamples(a, b, includeMissingAlterationsFromGenePanel);

        return getAlterationGeneCounts(
            molecularProfileCaseIdentifiers,
            includeFrequency,
            dataFetcher,
            includeFrequencyFunction,
            includeOffPanelAlterations
        );
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getPatientAlterationGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                  Select<Integer> entrezGeneIds,
                                                                                  boolean includeFrequency,
                                                                                  boolean includeMissingAlterationsFromGenePanel,
                                                                                  AlterationFilter alterationFilter,
                                                                                  boolean includeOffPanelAlterations) {

        Function<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>> dataFetcher = profileCaseIdentifiers ->
            alterationRepository.getPatientAlterationGeneCounts(new TreeSet<>(profileCaseIdentifiers), entrezGeneIds, alterationFilter);

        BiFunction<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>, Long> includeFrequencyFunction =
            (a, b) -> alterationEnrichmentUtil.includeFrequencyForPatients(a, b, includeMissingAlterationsFromGenePanel);

        return getAlterationGeneCounts(
            molecularProfileCaseIdentifiers,
            includeFrequency,
            dataFetcher,
            includeFrequencyFunction,
            includeOffPanelAlterations
        );
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getSampleMutationGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                               Select<Integer> entrezGeneIds,
                                                                               boolean includeFrequency,
                                                                               boolean includeMissingAlterationsFromGenePanel,
                                                                               AlterationFilter alterationFilter) {
        return getSampleAlterationGeneCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter,
            false
        );
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getPatientMutationGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                Select<Integer> entrezGeneIds,
                                                                                boolean includeFrequency,
                                                                                boolean includeMissingAlterationsFromGenePanel,
                                                                                AlterationFilter alterationFilter) {
        return getPatientAlterationGeneCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter,
            false
        );
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getSampleStructuralVariantGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                        Select<Integer> entrezGeneIds,
                                                                                        boolean includeFrequency,
                                                                                        boolean includeMissingAlterationsFromGenePanel,
                                                                                        AlterationFilter alterationFilter) {
        return getSampleAlterationGeneCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter,
            true
        );
    }

    @Override
    public Pair<List<AlterationCountByGene>, Long> getPatientStructuralVariantGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                         Select<Integer> entrezGeneIds,
                                                                                         boolean includeFrequency,
                                                                                         boolean includeMissingAlterationsFromGenePanel,
                                                                                         AlterationFilter alterationFilter) {
        return getPatientAlterationGeneCounts(molecularProfileCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter,
            true
        );
    }

    @Override
    public Pair<List<AlterationCountByStructuralVariant>, Long> getSampleStructuralVariantCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                                 boolean includeFrequency,
                                                                                                 boolean includeMissingAlterationsFromGenePanel,
                                                                                                 AlterationFilter alterationFilter) {
                                                                                                 
        Function<List<MolecularProfileCaseIdentifier>, List<AlterationCountByStructuralVariant>> dataFetcher = profileCaseIdentifiers ->
            alterationRepository.getSampleStructuralVariantCounts(new TreeSet<>(profileCaseIdentifiers), alterationFilter);

        BiFunction<List<MolecularProfileCaseIdentifier>, List<AlterationCountByStructuralVariant>, Long> includeFrequencyFunction =
            (a, b) -> alterationEnrichmentUtilStructVar.includeFrequencyForSamples(a, b, includeMissingAlterationsFromGenePanel);
            
        return getAlterationGeneCounts(
            molecularProfileCaseIdentifiers,
            includeFrequency,
            dataFetcher,
            includeFrequencyFunction,
            true
        );
    }

    @Override
    public Pair<List<CopyNumberCountByGene>, Long> getSampleCnaGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                          Select<Integer> entrezGeneIds,
                                                                          boolean includeFrequency,
                                                                          boolean includeMissingAlterationsFromGenePanel,
                                                                          AlterationFilter alterationFilter) {

        Function<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>> dataFetcher = profileCaseIdentifiers ->
            alterationRepository.getSampleCnaGeneCounts(new TreeSet<>(profileCaseIdentifiers), entrezGeneIds, alterationFilter);

        BiFunction<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>, Long> includeFrequencyFunction =
            (a, b) -> alterationEnrichmentUtilCna.includeFrequencyForSamples(a, b, includeMissingAlterationsFromGenePanel);

        return getAlterationGeneCounts(
            molecularProfileCaseIdentifiers,
            includeFrequency,
            dataFetcher,
            includeFrequencyFunction,
            false
        );
    }

    @Override
    public Pair<List<CopyNumberCountByGene>, Long> getPatientCnaGeneCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                           Select<Integer> entrezGeneIds,
                                                                           boolean includeFrequency,
                                                                           boolean includeMissingAlterationsFromGenePanel,
                                                                           AlterationFilter alterationFilter) {

        Function<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>> dataFetcher = profileCaseIdentifiers ->
            alterationRepository.getPatientCnaGeneCounts(new TreeSet<>(profileCaseIdentifiers), entrezGeneIds, alterationFilter);

        BiFunction<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>, Long> includeFrequencyFunction =
            (a, b) -> alterationEnrichmentUtilCna.includeFrequencyForPatients(a, b, includeMissingAlterationsFromGenePanel);

        return getAlterationGeneCounts(
            molecularProfileCaseIdentifiers,
            includeFrequency,
            dataFetcher,
            includeFrequencyFunction,
            false
        );
    }

    private <S extends AlterationCountBase> Pair<List<S>, Long> getAlterationGeneCounts(
        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
        boolean includeFrequency,
        Function<List<MolecularProfileCaseIdentifier>, List<S>> dataFetcher,
        BiFunction<List<MolecularProfileCaseIdentifier>, List<S>, Long> includeFrequencyFunction,
        boolean includeOffPanelAlterations) {
        AtomicReference<Long> profiledCasesCount = new AtomicReference<>(0L);
        if (molecularProfileCaseIdentifiers.isEmpty()) {
            return new Pair<>(Collections.emptyList(), profiledCasesCount.get());
        }
        
        Set<String> molecularProfileIds = molecularProfileCaseIdentifiers
            .stream()
            .map(MolecularProfileCaseIdentifier::getMolecularProfileId)
            .collect(Collectors.toSet());
        Map<String, String> molecularProfileIdStudyIdMap = molecularProfileRepository
            .getMolecularProfiles(molecularProfileIds, "SUMMARY")
            .stream()
            .collect(Collectors.toMap(MolecularProfile::getStableId, MolecularProfile::getCancerStudyIdentifier));

        Map<String, S> totalResult = new HashMap<>();
        molecularProfileCaseIdentifiers
            .stream()
            .collect(Collectors
                .groupingBy(identifier -> molecularProfileIdStudyIdMap.get(identifier.getMolecularProfileId())))
            .values()
            .forEach(studyMolecularProfileCaseIdentifiers -> {
                List<S> studyAlterationCountByGenes = dataFetcher.apply(studyMolecularProfileCaseIdentifiers);
                if (!includeOffPanelAlterations) {
                    studyAlterationCountByGenes = filterOffPanelAlterations(studyMolecularProfileCaseIdentifiers, studyAlterationCountByGenes);
                }
                if (includeFrequency) {
                    Long studyProfiledCasesCount = includeFrequencyFunction.apply(studyMolecularProfileCaseIdentifiers, studyAlterationCountByGenes);
                    profiledCasesCount.updateAndGet(v -> v + studyProfiledCasesCount);
                }
                AlterationCountServiceUtil.setupAlterationGeneCountsMap(studyAlterationCountByGenes, totalResult);
            });
        
        List<S> alterationCountByGenes = new ArrayList<>(totalResult.values());
        return new Pair<>(alterationCountByGenes, profiledCasesCount.get());
    }

    /**
     * Filters alterations based on gene panels associated with the given molecular profiles.
     * Retains alterations if:
     * 1. Not gene-specific (SV).
     * 2. Gene is on panels associated with the specific molecular profiles.
     * 3. Gene is NOT present in ANY gene panel (globally) - these are considered to be part of WES.
     * Discards alterations for genes on other panels not associated with current profiles.
     */
    private <S extends AlterationCountBase> List<S> filterOffPanelAlterations(
        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
        List<S> studyAlterationCountByGenes) {

        // Fetch gene panel data for the current molecular profiles
        List<GenePanelData> panelData = genePanelService.fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileCaseIdentifiers);

        // Extract panel IDs, filtering out nulls
        Set<String> associatedPanelIds = panelData.stream()
            .map(GenePanelData::getGenePanelId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // Get genes from associated panels
        Set<Integer> genesOnAssociatedPanels = new HashSet<>();
        if (!associatedPanelIds.isEmpty()) {
            List<GenePanel> detailedPanels = genePanelService.fetchGenePanels(new ArrayList<>(associatedPanelIds), "DETAILED");

            // Extract genes from all panels in a single stream
            genesOnAssociatedPanels = detailedPanels.stream()
                .map(GenePanel::getGenes)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(GenePanelToGene::getEntrezGeneId)
                .collect(Collectors.toSet());
        }

        // Process alterations into filtered and pending groups
        List<S> filteredAlterations = new ArrayList<>();
        Map<Integer, List<S>> pendingAlterations = new HashMap<>();

        // First pass: separate alterations we can decide on immediately vs. those needing checks
        for (S alteration : studyAlterationCountByGenes) {
            Integer geneId = alteration instanceof AlterationCountByGene alterationCountByGene ? alterationCountByGene.getEntrezGeneId() : null;

            if (geneId == null || genesOnAssociatedPanels.contains(geneId)) {
                // Keep alterations that are non-gene-specific or on associated panels
                filteredAlterations.add(alteration);
            } else {
                // Group alterations by gene ID for pending check
                pendingAlterations.computeIfAbsent(geneId, k -> new ArrayList<>()).add(alteration);
            }
        }

        // Check remaining genes against global panel database
        if (!pendingAlterations.isEmpty()) {
            Set<Integer> genesToCheck = pendingAlterations.keySet();
            Set<Integer> genesFoundInAnyPanel = genePanelService.findGeneIdsAssociatedWithAnyPanel(genesToCheck);

            // Add alterations for genes not found in any panel
            for (Integer geneId : genesToCheck) {
                if (!genesFoundInAnyPanel.contains(geneId)) {
                    filteredAlterations.addAll(pendingAlterations.get(geneId));
                }
            }
        }

        return filteredAlterations;
    }
}
