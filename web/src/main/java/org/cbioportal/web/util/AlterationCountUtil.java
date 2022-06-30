package org.cbioportal.web.util;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.*;
import org.cbioportal.model.util.Select;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.web.parameter.AlterationCountFilter;
import org.cbioportal.web.parameter.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AlterationCountUtil {
    @Autowired
    private AlterationCountService alterationCountService;
    @Autowired
    private GenePanelService genePanelService;
    
    public Pair<List<AlterationCountByGene>, Long> getAlterationCounts(
        AlterationCountFilter alterationCountFilter,
        AlterationCountType alterationCountType,
        List<Integer> entrezGeneIds
    ) {
        // select all genes if no list of entrez gene ids provided
        Select<Integer> geneSelect = (entrezGeneIds == null || entrezGeneIds.isEmpty()) ?
            Select.all(): Select.byValues(entrezGeneIds);
        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers =
            alterationCountFilter.getMolecularProfileCaseIdentifiers();
        boolean includeFrequency = true;
        boolean includeMissingAlterationsFromGenePanel = false;
        AlterationFilter alterationFilter = alterationCountFilter.getAlterationFilter();

         return alterationCountType.equals(AlterationCountType.SAMPLE) ?
            alterationCountService.getSampleAlterationCounts(
                molecularProfileCaseIdentifiers,
                geneSelect,
                includeFrequency,
                includeMissingAlterationsFromGenePanel,
                alterationFilter
            ) :
            alterationCountService.getPatientAlterationCounts(
                molecularProfileCaseIdentifiers,
                geneSelect,
                includeFrequency,
                includeMissingAlterationsFromGenePanel,
                alterationFilter
            );
    }
    
    public Set<GenePanelToGene> getProfiledGenes(
        AlterationCountFilter alterationCountFilter,
        AlterationCountType alterationCountType,
        List<Integer> entrezGeneIds
    ) {
        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers =
            alterationCountFilter.getMolecularProfileCaseIdentifiers();
        Set<GenePanelToGene> profiledGenes;
        List<GenePanelData> genePanelDataList = alterationCountType.equals(AlterationCountType.SAMPLE) ?
            genePanelService.fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileCaseIdentifiers) :
            genePanelService.fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(molecularProfileCaseIdentifiers);

        if (genePanelDataList
            .stream()
            .anyMatch(
                genePanelData -> genePanelData.getProfiled() && genePanelData.getGenePanelId() == null
            )
        ) {
            // if there is at least one sample/patient with all genes profiled,
            // then all genes are profiled for the current data set
            profiledGenes = null;
        } else {
            List<String> profiledPanelIds =  genePanelDataList
                .stream()
                .filter(GenePanelData::getProfiled)
                .map(GenePanelData::getGenePanelId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            if (profiledPanelIds.isEmpty()) {
                profiledGenes = Collections.emptySet();
            } else {
                List<GenePanel> panels = genePanelService.fetchGenePanels(
                    profiledPanelIds,
                    Projection.DETAILED.name()
                );

                Stream<GenePanelToGene> genePanelGenes = panels
                    .stream()
                    .map(GenePanel::getGenes)
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream);

                // if entrezGeneIds is not empty, further filter by given entrez gene ids
                if (entrezGeneIds != null && !entrezGeneIds.isEmpty()) {
                    Set<Integer> entrezGeneIdSet = new HashSet<>(entrezGeneIds);
                    genePanelGenes = genePanelGenes.filter(gene -> entrezGeneIdSet.contains(gene.getEntrezGeneId()));
                }

                profiledGenes = genePanelGenes.collect(Collectors.toSet());
            }
        }

        return profiledGenes;
    }
}
