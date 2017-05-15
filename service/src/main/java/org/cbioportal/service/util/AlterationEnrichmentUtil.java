package org.cbioportal.service.util;

import org.cbioportal.model.Alteration;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.AlterationSampleCountByGene;
import org.cbioportal.model.Gene;
import org.cbioportal.service.GeneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AlterationEnrichmentUtil {

    @Autowired
    private FisherExactTestCalculator fisherExactTestCalculator;
    @Autowired
    private LogRatioCalculator logRatioCalculator;
    @Autowired
    private BenjaminiHochbergFDRCalculator benjaminiHochbergFDRCalculator;
    @Autowired
    private GeneService geneService;

    public List<AlterationEnrichment> createAlterationEnrichments(
        int alteredSampleCount, int unalteredSampleCount,
        List<? extends AlterationSampleCountByGene> alterationSampleCountByGenes,
        List<? extends Alteration> alterations) {

        Map<Integer, List<Alteration>> discreteCopyNumberDataMap = alterations.stream().collect(Collectors.groupingBy(
            Alteration::getEntrezGeneId));

        List<Gene> genes = geneService.fetchGenes(alterationSampleCountByGenes.stream().map(m ->
            String.valueOf(m.getEntrezGeneId())).collect(Collectors.toList()), "ENTREZ_GENE_ID", "SUMMARY");

        alterationSampleCountByGenes.sort(Comparator.comparing(AlterationSampleCountByGene::getEntrezGeneId));
        genes.sort(Comparator.comparing(Gene::getEntrezGeneId));
        List<AlterationEnrichment> result = new ArrayList<>();
        for (int i = 0; i < alterationSampleCountByGenes.size(); i++) {
            AlterationSampleCountByGene copyNumberSampleCountByGene = alterationSampleCountByGenes.get(i);
            result.add(createAlterationEnrichment(discreteCopyNumberDataMap.get(
                copyNumberSampleCountByGene.getEntrezGeneId()), copyNumberSampleCountByGene, genes.get(i),
                alteredSampleCount, unalteredSampleCount));
        }

        assignQValue(result);
        return result;
    }

    private AlterationEnrichment createAlterationEnrichment(List<? extends Alteration> alterations,
                                                           AlterationSampleCountByGene alterationSampleCountByGene,
                                                           Gene gene, int alteredSampleCount,
                                                           int unalteredSampleCount) {

        AlterationEnrichment alterationEnrichment = new AlterationEnrichment();

        if (alterations == null) {
            alterationEnrichment.setNumberOfSamplesInAlteredGroup(0);
        } else {
            alterationEnrichment.setNumberOfSamplesInAlteredGroup(alterations.stream().collect(
                Collectors.groupingBy(Alteration::getSampleId)).size());
        }
        alterationEnrichment.setNumberOfSamplesInUnalteredGroup(alterationSampleCountByGene.getSampleCount() -
            alterationEnrichment.getNumberOfSamplesInAlteredGroup());
        alterationEnrichment.setEntrezGeneId(alterationSampleCountByGene.getEntrezGeneId());
        alterationEnrichment.setHugoGeneSymbol(gene.getHugoGeneSymbol());
        alterationEnrichment.setCytoband(gene.getCytoband());
        assignLogRatio(alterationEnrichment, alteredSampleCount, unalteredSampleCount);
        assignPValue(alterationEnrichment, alteredSampleCount, unalteredSampleCount);
        return alterationEnrichment;
    }

    private void assignQValue(List<AlterationEnrichment> alterationEnrichments) {

        alterationEnrichments.sort(Comparator.comparing(AlterationEnrichment::getpValue));
        double[] qValues = benjaminiHochbergFDRCalculator.calculate(alterationEnrichments.stream().mapToDouble(a ->
            a.getpValue().doubleValue()).toArray());

        for (int i = 0; i < alterationEnrichments.size(); i++) {
            alterationEnrichments.get(i).setqValue(BigDecimal.valueOf(qValues[i]));
        }
    }

    private void assignLogRatio(AlterationEnrichment alterationEnrichment, int alteredSampleCount,
                                int unalteredSampleCount) {

        double alteredRatio = (double) alterationEnrichment.getNumberOfSamplesInAlteredGroup() /
            alteredSampleCount;
        double unalteredRatio = (double) alterationEnrichment.getNumberOfSamplesInUnalteredGroup() /
            unalteredSampleCount;

        double logRatio = logRatioCalculator.getLogRatio(alteredRatio, unalteredRatio);
        alterationEnrichment.setLogRatio(String.valueOf(logRatio));
    }

    private void assignPValue(AlterationEnrichment alterationEnrichment, int alteredSampleCount,
                              int unalteredSampleCount) {

        int alteredInNoneCount = unalteredSampleCount - alterationEnrichment.getNumberOfSamplesInUnalteredGroup();
        int alteredOnlyInQueryGenesCount = alteredSampleCount - alterationEnrichment.getNumberOfSamplesInAlteredGroup();

        double pValue = fisherExactTestCalculator.getCumlativePValue(alteredInNoneCount,
            alterationEnrichment.getNumberOfSamplesInUnalteredGroup(), alteredOnlyInQueryGenesCount,
            alterationEnrichment.getNumberOfSamplesInAlteredGroup());

        alterationEnrichment.setpValue(BigDecimal.valueOf(pValue));
    }
}
