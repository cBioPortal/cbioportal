package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.Gene;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationSampleCountByGene;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.MutationEnrichmentService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.service.util.BenjaminiHochbergFDRCalculator;
import org.cbioportal.service.util.FisherExactTestCalculator;
import org.cbioportal.service.util.LogRatioCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MutationEnrichmentServiceImpl implements MutationEnrichmentService {

    @Autowired
    private MutationService mutationService;
    @Autowired
    private FisherExactTestCalculator fisherExactTestCalculator;
    @Autowired
    private LogRatioCalculator logRatioCalculator;
    @Autowired
    private BenjaminiHochbergFDRCalculator benjaminiHochbergFDRCalculator;
    @Autowired
    private GeneService geneService;

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<AlterationEnrichment> getMutationEnrichments(String geneticProfileId, List<String> alteredSampleIds,
                                                             List<String> unalteredSampleIds,
                                                             List<Integer> entrezGeneIds)
        throws GeneticProfileNotFoundException {

        List<String> allSampleIds = new ArrayList<>(alteredSampleIds);
        allSampleIds.addAll(unalteredSampleIds);
        List<MutationSampleCountByGene> mutationSampleCountByGeneList = mutationService
            .getSampleCountByEntrezGeneIdsAndSampleIds(geneticProfileId, allSampleIds, null);
        mutationSampleCountByGeneList.removeIf(m -> entrezGeneIds.contains(m.getEntrezGeneId()));
        List<Gene> genes = geneService.fetchGenes(mutationSampleCountByGeneList.stream().map(m -> 
            String.valueOf(m.getEntrezGeneId())).collect(Collectors.toList()), "ENTREZ_GENE_ID", "SUMMARY");

        Map<Integer, List<Mutation>> mutationMap = mutationService.fetchMutationsInGeneticProfile(geneticProfileId,
            alteredSampleIds, null, "ID", null, null, null, null).stream().collect(Collectors.groupingBy(
                Mutation::getEntrezGeneId));

        mutationSampleCountByGeneList.sort(Comparator.comparing(MutationSampleCountByGene::getEntrezGeneId));
        genes.sort(Comparator.comparing(Gene::getEntrezGeneId));
        List<AlterationEnrichment> result = new ArrayList<>();
        for (int i = 0; i < mutationSampleCountByGeneList.size(); i++) {
            MutationSampleCountByGene mutationSampleCountByGene = mutationSampleCountByGeneList.get(i);
            result.add(createAlterationEnrichment(mutationMap.get(mutationSampleCountByGene.getEntrezGeneId()), 
                mutationSampleCountByGene, genes.get(i), alteredSampleIds.size(), unalteredSampleIds.size()));
        }
        
        setQValue(result);
        return result;
    }

    private AlterationEnrichment createAlterationEnrichment(List<Mutation> mutations, 
                                                            MutationSampleCountByGene mutationSampleCountByGene,
                                                            Gene gene, int alteredSampleCount, 
                                                            int unalteredSampleCount) {
        
        AlterationEnrichment alterationEnrichment = new AlterationEnrichment();
        
        if (mutations == null) {
            alterationEnrichment.setNumberOfSamplesInAlteredGroup(0);
        } else {
            alterationEnrichment.setNumberOfSamplesInAlteredGroup(mutations.stream().collect(Collectors.groupingBy(
                Mutation::getSampleId)).size());
        }
        alterationEnrichment.setNumberOfSamplesInUnalteredGroup(mutationSampleCountByGene.getSampleCount() -
            alterationEnrichment.getNumberOfSamplesInAlteredGroup());
        alterationEnrichment.setEntrezGeneId(mutationSampleCountByGene.getEntrezGeneId());
        alterationEnrichment.setHugoGeneSymbol(gene.getHugoGeneSymbol());
        alterationEnrichment.setCytoband(gene.getCytoband());
        setLogRatio(alterationEnrichment, alteredSampleCount, unalteredSampleCount);
        setPValue(alterationEnrichment, alteredSampleCount, unalteredSampleCount);
        return alterationEnrichment;
    }

    private void setQValue(List<AlterationEnrichment> alterationEnrichments) {

        alterationEnrichments.sort(Comparator.comparing(AlterationEnrichment::getpValue));
        double[] qValues = benjaminiHochbergFDRCalculator.calculate(alterationEnrichments.stream().mapToDouble(a ->
            a.getpValue().doubleValue()).toArray());

        for (int i = 0; i < alterationEnrichments.size(); i++) {
            alterationEnrichments.get(i).setqValue(BigDecimal.valueOf(qValues[i]));
        }
    }

    private void setLogRatio(AlterationEnrichment alterationEnrichment, int alteredSampleCount,
                             int unalteredSampleCount) {

        double alteredRatio = (double) alterationEnrichment.getNumberOfSamplesInAlteredGroup() /
            alteredSampleCount;
        double unalteredRatio = (double) alterationEnrichment.getNumberOfSamplesInUnalteredGroup() /
            unalteredSampleCount;

        double logRatio = logRatioCalculator.getLogRatio(alteredRatio, unalteredRatio);
        alterationEnrichment.setLogRatio(String.valueOf(logRatio));
    }

    private void setPValue(AlterationEnrichment alterationEnrichment, int alteredSampleCount,
                           int unalteredSampleCount) {

        int alteredInNoneCount = unalteredSampleCount - alterationEnrichment.getNumberOfSamplesInUnalteredGroup();
        int alteredOnlyInQueryGenesCount = alteredSampleCount - alterationEnrichment.getNumberOfSamplesInAlteredGroup();

        double pValue = fisherExactTestCalculator.getCumlativePValue(alteredInNoneCount,
            alterationEnrichment.getNumberOfSamplesInUnalteredGroup(), alteredOnlyInQueryGenesCount,
            alterationEnrichment.getNumberOfSamplesInAlteredGroup());

        alterationEnrichment.setpValue(BigDecimal.valueOf(pValue));
    }
}
