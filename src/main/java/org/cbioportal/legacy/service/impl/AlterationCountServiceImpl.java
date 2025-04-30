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
                    studyAlterationCountByGenes = filterAlterationsByGenePanel(studyMolecularProfileCaseIdentifiers, studyAlterationCountByGenes);
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
    
    private <S extends AlterationCountBase> List<S> filterAlterationsByGenePanel(List<MolecularProfileCaseIdentifier> studyMolecularProfileCaseIdentifiers, List<S> studyAlterationCountByGenes) {
        // Fetch GenePanelData using the service
        List<GenePanelData> genePanelDataList = genePanelService.fetchGenePanelDataInMultipleMolecularProfiles(studyMolecularProfileCaseIdentifiers);

        // Extract unique panel IDs used in this group
        Set<String> studyPanelIds = genePanelDataList.stream()
            .map(GenePanelData::getGenePanelId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // Only filter if panels are actually associated
        if (!studyPanelIds.isEmpty()) {
            // Fetch the GenePanel objects for these IDs, requesting detailed info (includes gene list)
            List<GenePanel> detailedGenePanels = genePanelService.fetchGenePanels(new ArrayList<>(studyPanelIds), "DETAILED");
            
            // Filter the alteration list based on the collected panel gene IDs
            final Set<Integer> finalPanelGeneIds = detailedGenePanels.stream() // Stream<GenePanel>
                .map(GenePanel::getGenes)     // Stream<List<GenePanelToGene>>
                .filter(Objects::nonNull)     // Filter out panels with null gene lists (safety check)
                .flatMap(List::stream)     // Stream<GenePanelToGene> (flatten list of lists)
                .map(GenePanelToGene::getEntrezGeneId) // Stream<Integer>
                .collect(Collectors.toSet());
            
            studyAlterationCountByGenes = studyAlterationCountByGenes.stream()
                .filter(alterationCount -> {
                    Integer entrezGeneId = getEntrezGeneIdIfApplicable(alterationCount); // Use helper
                    if (entrezGeneId == null) {
                        return true; // Keep non-gene-specific alterations
                    }
                    return finalPanelGeneIds.contains(entrezGeneId); // Keep if gene is on panel
                })
                .toList();
        }
        return studyAlterationCountByGenes;
    }
    
    private Integer getEntrezGeneIdIfApplicable(AlterationCountBase alterationCount) {
        // Check if the object is an instance of AlterationCountByGene or its subclasses
        // (this covers both mutations and CNAs based on the class hierarchy).
        if (alterationCount instanceof AlterationCountByGene alterationCountByGene) {
            // Return the gene ID for types applicable to gene-based filtering.
            return alterationCountByGene.getEntrezGeneId();
        } else {
            // Return null for types (like Structural Variants) that should bypass
            // the gene panel filtering based on the current logic.
            return null;
        }
    }

}
