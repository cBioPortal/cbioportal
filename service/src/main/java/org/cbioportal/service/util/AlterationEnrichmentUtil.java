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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.cbioportal.model.Entity;

@Component
public class AlterationEnrichmentUtil {

    @Autowired
    private FisherExactTestCalculator fisherExactTestCalculator;
    @Autowired
    private LogRatioCalculator logRatioCalculator;
    @Autowired
    private GeneService geneService;

    public List<AlterationEnrichment> createAlterationEnrichments(
        int set1Count, int set2Count,
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
                copyNumberCountByGene.getEntrezGeneId()), copyNumberCountByGene, genes.get(i), set1Count,
                set2Count, enrichmentType));
        }

        return alterationEnrichments;
    }

    private AlterationEnrichment createAlterationEnrichment(List<? extends Alteration> alterations,
                                                           AlterationCountByGene alterationCountByGene,
                                                           Gene gene, int set1Count,
                                                           int set2Count, String enrichmentType) {

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
        alterationEnrichment.setProfiledInSet1Count(set1Count);
        alterationEnrichment.setProfiledInSet2Count(set2Count);
        assignLogRatio(alterationEnrichment, set1Count, set2Count);
        assignPValue(alterationEnrichment, set1Count, set2Count);
        return alterationEnrichment;
    }

    private void assignLogRatio(AlterationEnrichment alterationEnrichment, int set1Count,
                                int set2Count) {

        double set1Ratio = (double) alterationEnrichment.getAlteredInSet1Count() / set1Count;
        double set2Ratio = (double) alterationEnrichment.getAlteredInSet2Count() / set2Count;

        double logRatio = logRatioCalculator.getLogRatio(set1Ratio, set2Ratio);
        alterationEnrichment.setLogRatio(String.valueOf(logRatio));
    }

    private void assignPValue(AlterationEnrichment alterationEnrichment, int set1Count,
                              int set2Count) {

        int alteredInNoneCount = set2Count - alterationEnrichment.getAlteredInSet2Count();
        int alteredOnlyInQueryGenesCount = set1Count - alterationEnrichment.getAlteredInSet1Count();

        double pValue = fisherExactTestCalculator.getCumulativePValue(alteredInNoneCount,
            alterationEnrichment.getAlteredInSet2Count(), alteredOnlyInQueryGenesCount,
            alterationEnrichment.getAlteredInSet1Count());

        alterationEnrichment.setpValue(BigDecimal.valueOf(pValue));
    }

    public Map<String, List<String>> mapMolecularProfileIdToEntityId(List<Entity> entities) {
        Map<String, List<String>> molecularProfileIdToEntityIdMap = new HashMap<>();
        for (Entity entity : entities) {
            String molecularProfileId = entity.getMolecularProfileId();
            String entityId = entity.getEntityId();
            if (!molecularProfileIdToEntityIdMap.containsKey(molecularProfileId)) {
                molecularProfileIdToEntityIdMap.put(molecularProfileId, new ArrayList<>());
            }
            molecularProfileIdToEntityIdMap.get(molecularProfileId).add(entityId);
        }
        return molecularProfileIdToEntityIdMap;
    }
}
