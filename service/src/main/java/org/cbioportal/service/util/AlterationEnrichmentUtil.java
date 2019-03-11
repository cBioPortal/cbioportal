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
import org.cbioportal.model.CountSummary;
import org.cbioportal.model.MolecularProfileCaseIdentifier;

@Component
public class AlterationEnrichmentUtil {

    @Autowired
    private FisherExactTestCalculator fisherExactTestCalculator;
    @Autowired
    private LogRatioCalculator logRatioCalculator;
    @Autowired
    private GeneService geneService;

    public List<AlterationEnrichment> createAlterationEnrichments(
        int molecularProfileCaseSet1Count, int molecularProfileCaseSet2Count,
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
                copyNumberCountByGene.getEntrezGeneId()), copyNumberCountByGene, genes.get(i), molecularProfileCaseSet1Count,
                molecularProfileCaseSet2Count, enrichmentType));
        }

        return alterationEnrichments;
    }

    private AlterationEnrichment createAlterationEnrichment(List<? extends Alteration> alterations,
                                                           AlterationCountByGene alterationCountByGene,
                                                           Gene gene, int molecularProfileCaseSet1Count,
                                                           int molecularProfileCaseSet2Count, String enrichmentType) {

        AlterationEnrichment alterationEnrichment = new AlterationEnrichment();
        CountSummary set1CountSummary = new CountSummary();
        CountSummary set2CountSummary = new CountSummary();
        if (alterations == null) {
            set1CountSummary.setAlteredCount(0);
        } else {
            if (enrichmentType.equals("SAMPLE")) {
                set1CountSummary.setAlteredCount(alterations.stream().collect(
                    Collectors.groupingBy(Alteration::getSampleId)).size());
            } else {
                set1CountSummary.setAlteredCount(alterations.stream().collect(
                    Collectors.groupingBy(Alteration::getPatientId)).size());
            }
        }
        set1CountSummary.setProfiledCount(molecularProfileCaseSet1Count);
        set2CountSummary.setAlteredCount(alterationCountByGene.getNumberOfAlteredCases() -
            set1CountSummary.getAlteredCount());
        set2CountSummary.setProfiledCount(molecularProfileCaseSet2Count);
        alterationEnrichment.setSet1CountSummary(set1CountSummary);
        alterationEnrichment.setSet2CountSummary(set2CountSummary);
        alterationEnrichment.setEntrezGeneId(alterationCountByGene.getEntrezGeneId());
        alterationEnrichment.setHugoGeneSymbol(gene.getHugoGeneSymbol());
        alterationEnrichment.setCytoband(gene.getCytoband());
        assignLogRatio(alterationEnrichment, molecularProfileCaseSet1Count, molecularProfileCaseSet2Count);
        assignPValue(alterationEnrichment, molecularProfileCaseSet1Count, molecularProfileCaseSet2Count);
        return alterationEnrichment;
    }

    private void assignLogRatio(AlterationEnrichment alterationEnrichment, int molecularProfileCaseSet1Count,
                                int molecularProfileCaseSet2Count) {

        double molecularProfileCaseSet1Ratio = (double) alterationEnrichment.getSet1CountSummary().getAlteredCount() / molecularProfileCaseSet1Count;
        double molecularProfileCaseSet2Ratio = (double) alterationEnrichment.getSet2CountSummary().getAlteredCount() / molecularProfileCaseSet2Count;

        double logRatio = logRatioCalculator.getLogRatio(molecularProfileCaseSet1Ratio, molecularProfileCaseSet2Ratio);
        alterationEnrichment.setLogRatio(String.valueOf(logRatio));
    }

    private void assignPValue(AlterationEnrichment alterationEnrichment, int molecularProfileCaseSet1Count,
                              int molecularProfileCaseSet2Count) {

        int alteredInNoneCount = molecularProfileCaseSet2Count - alterationEnrichment.getSet2CountSummary().getAlteredCount();
        int alteredOnlyInQueryGenesCount = molecularProfileCaseSet1Count - alterationEnrichment.getSet1CountSummary().getAlteredCount();

        double pValue = fisherExactTestCalculator.getCumulativePValue(alteredInNoneCount,
            alterationEnrichment.getSet2CountSummary().getAlteredCount(), alteredOnlyInQueryGenesCount,
            alterationEnrichment.getSet1CountSummary().getAlteredCount());

        alterationEnrichment.setpValue(BigDecimal.valueOf(pValue));
    }

    public Map<String, List<String>> mapMolecularProfileIdToCaseId(List<MolecularProfileCaseIdentifier> molecularProfileCases) {
        Map<String, List<String>> molecularProfileIdToCaseIdMap = new HashMap<>();
        for (MolecularProfileCaseIdentifier mpc : molecularProfileCases) {
            String molecularProfileId = mpc.getMolecularProfileId();
            String entityId = mpc.getCaseId();
            if (!molecularProfileIdToCaseIdMap.containsKey(molecularProfileId)) {
                molecularProfileIdToCaseIdMap.put(molecularProfileId, new ArrayList<>());
            }
            molecularProfileIdToCaseIdMap.get(molecularProfileId).add(entityId);
        }
        return molecularProfileIdToCaseIdMap;
    }
}
