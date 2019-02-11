package org.cbioportal.service.util;

import org.cbioportal.model.Alteration;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.AlterationCountByGene;
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
    private GeneService geneService;

    public List<AlterationEnrichment> createAlterationEnrichments(
        int alteredCount, int unalteredCount,
        List<? extends AlterationCountByGene> alterationCountByGenes,
        List<? extends Alteration> alterations, String enrichmentType) {

        Map<Integer, List<Alteration>> discreteCopyNumberDataMap = alterations.stream().collect(Collectors.groupingBy(
            Alteration::getEntrezGeneId));

        List<Gene> genes = geneService.fetchGenes(alterationCountByGenes.stream().map(m ->
            String.valueOf(m.getEntrezGeneId())).collect(Collectors.toList()), "ENTREZ_GENE_ID", "SUMMARY");

        alterationCountByGenes.sort(Comparator.comparing(AlterationCountByGene::getEntrezGeneId));
        genes.sort(Comparator.comparing(Gene::getEntrezGeneId));
        List<AlterationEnrichment> alterationEnrichments = new ArrayList<>();
        for (int i = 0; i < alterationCountByGenes.size(); i++) {
            AlterationCountByGene copyNumberCountByGene = alterationCountByGenes.get(i);
            alterationEnrichments.add(createAlterationEnrichment(discreteCopyNumberDataMap.get(
                copyNumberCountByGene.getEntrezGeneId()), copyNumberCountByGene, genes.get(i), alteredCount, 
                unalteredCount, enrichmentType));
        }

        return alterationEnrichments;
    }

    private AlterationEnrichment createAlterationEnrichment(List<? extends Alteration> alterations,
                                                           AlterationCountByGene alterationCountByGene,
                                                           Gene gene, int alteredCount,
                                                           int unalteredCount, String enrichmentType) {

        AlterationEnrichment alterationEnrichment = new AlterationEnrichment();

        if (alterations == null) {
            alterationEnrichment.setAlteredInSet1Count(0);
        } else {
            if (enrichmentType.equals("SAMPLE")) {
                alterationEnrichment.setAlteredInSet1Count(alterations.stream().collect(
                    Collectors.groupingBy(Alteration::getSampleId)).size());
            } else {
                alterationEnrichment.setAlteredInSet1Count(alterations.stream().collect(
                    Collectors.groupingBy(Alteration::getPatientId)).size());
            }
        }
        alterationEnrichment.setAlteredInSet2Count(alterationCountByGene.getCountByEntity() -
            alterationEnrichment.getAlteredInSet1Count());
        alterationEnrichment.setEntrezGeneId(alterationCountByGene.getEntrezGeneId());
        alterationEnrichment.setHugoGeneSymbol(gene.getHugoGeneSymbol());
        alterationEnrichment.setCytoband(gene.getCytoband());
        assignLogRatio(alterationEnrichment, alteredCount, unalteredCount);
        assignPValue(alterationEnrichment, alteredCount, unalteredCount);
        return alterationEnrichment;
    }

    private void assignLogRatio(AlterationEnrichment alterationEnrichment, int alteredCount,
                                int unalteredCount) {

        double alteredRatio = (double) alterationEnrichment.getAlteredInSet1Count() / alteredCount;
        double unalteredRatio = (double) alterationEnrichment.getAlteredInSet2Count() / unalteredCount;

        double logRatio = logRatioCalculator.getLogRatio(alteredRatio, unalteredRatio);
        alterationEnrichment.setLogRatio(String.valueOf(logRatio));
    }

    private void assignPValue(AlterationEnrichment alterationEnrichment, int alteredCount,
                              int unalteredCount) {

        int alteredInNoneCount = unalteredCount - alterationEnrichment.getAlteredInSet2Count();
        int alteredOnlyInQueryGenesCount = alteredCount - alterationEnrichment.getAlteredInSet1Count();

        double pValue = fisherExactTestCalculator.getCumulativePValue(alteredInNoneCount,
            alterationEnrichment.getAlteredInSet2Count(), alteredOnlyInQueryGenesCount,
            alterationEnrichment.getAlteredInSet1Count());

        alterationEnrichment.setpValue(BigDecimal.valueOf(pValue));
    }
}
