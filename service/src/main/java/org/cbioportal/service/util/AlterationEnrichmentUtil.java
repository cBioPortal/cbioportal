package org.cbioportal.service.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.Gene;
import org.cbioportal.model.CountSummary;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.GeneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlterationEnrichmentUtil {

    @Autowired
    private FisherExactTestCalculator fisherExactTestCalculator;
    @Autowired
    private GeneService geneService;

    public List<AlterationEnrichment> createAlterationEnrichments(
            Map<String, List<? extends AlterationCountByGene>> mutationCountsbyGroup,
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, String enrichmentType) {

        Map<String, Map<Integer, AlterationCountByGene>> mutationCountsbyEntrezGeneIdAndGroup = mutationCountsbyGroup
                .entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> {
                    return entry.getValue().stream()
                            .collect(Collectors.toMap(AlterationCountByGene::getEntrezGeneId, c -> c));
                }));

        Set<Integer> allGeneIds = mutationCountsbyEntrezGeneIdAndGroup.values().stream()
                .flatMap(x -> x.keySet().stream()).collect(Collectors.toSet());
        Set<String> groups = mutationCountsbyEntrezGeneIdAndGroup.keySet();

        List<Gene> genes = geneService.fetchGenes(
                allGeneIds.stream().map(Object::toString).collect(Collectors.toList()), "ENTREZ_GENE_ID", "SUMMARY");

        return genes.stream().map(gene -> {

            AlterationEnrichment alterationEnrichment = new AlterationEnrichment();
            alterationEnrichment.setEntrezGeneId(gene.getEntrezGeneId());
            alterationEnrichment.setHugoGeneSymbol(gene.getHugoGeneSymbol());
            alterationEnrichment.setCytoband(gene.getCytoband());

            List<CountSummary> counts = groups.stream().map(group -> {
                CountSummary groupCasesCount = new CountSummary();
                AlterationCountByGene mutationCountByGene = mutationCountsbyEntrezGeneIdAndGroup
                        .getOrDefault(group, new HashMap<Integer, AlterationCountByGene>()).get(gene.getEntrezGeneId());
                Integer count = mutationCountByGene != null ? mutationCountByGene.getNumberOfAlteredCases() : 0;
                groupCasesCount.setName(group);
                groupCasesCount.setAlteredCount(count);
                groupCasesCount.setProfiledCount(molecularProfileCaseSets.get(group).size());
                return groupCasesCount;
            }).collect(Collectors.toList());

            double pValue;
            // if groups size is two do Fisher Exact test else do Chi-Square test
            if (groups.size() == 2) {

                int alteredInNoneCount = counts.get(1).getProfiledCount() - counts.get(1).getAlteredCount();
                int alteredOnlyInQueryGenesCount = counts.get(0).getProfiledCount() - counts.get(0).getAlteredCount();

                pValue = fisherExactTestCalculator.getCumulativePValue(alteredInNoneCount,
                        counts.get(1).getAlteredCount(), alteredOnlyInQueryGenesCount, counts.get(0).getAlteredCount());
            } else {

                long[][] array = counts.stream().map(count -> {
                    return new long[] { count.getAlteredCount(), count.getProfiledCount() - count.getAlteredCount() };
                }).toArray(long[][]::new);

                ChiSquareTest chiSquareTest = new ChiSquareTest();
                pValue = chiSquareTest.chiSquareTest(array);
            }

            alterationEnrichment.setpValue(BigDecimal.valueOf(pValue));
            alterationEnrichment.setCounts(counts);
            return alterationEnrichment;
        }).collect(Collectors.toList());

    }

}
