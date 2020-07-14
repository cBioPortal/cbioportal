package org.cbioportal.service.util;

import java.math.BigDecimal;
import java.util.*;

import org.cbioportal.model.*;
import org.cbioportal.service.GeneService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlterationEnrichmentUtilTest {

    @InjectMocks
    private AlterationEnrichmentUtil alterationEnrichmentUtil;

    @Mock
    private FisherExactTestCalculator fisherExactTestCalculator;
    @Mock
    private GeneService geneService;

    @Test
    public void createAlterationEnrichments() throws Exception {

        // create molecularProfileCaseSet1, molecularProfileCaseSet2 list of entities
        MolecularProfileCaseIdentifier molecularProfileCase1 = new MolecularProfileCaseIdentifier();
        molecularProfileCase1.setCaseId("sample_id_1");
        molecularProfileCase1.setMolecularProfileId("test1_cna");
        MolecularProfileCaseIdentifier molecularProfileCase2 = new MolecularProfileCaseIdentifier();
        molecularProfileCase2.setCaseId("sample_id_2");
        molecularProfileCase2.setMolecularProfileId("test2_cna");
        List<MolecularProfileCaseIdentifier> molecularProfileCaseSet1 = new ArrayList<>();
        molecularProfileCaseSet1.add(molecularProfileCase1);
        molecularProfileCaseSet1.add(molecularProfileCase2);

        MolecularProfileCaseIdentifier molecularProfileCase3 = new MolecularProfileCaseIdentifier();
        molecularProfileCase3.setCaseId("sample_id_3");
        molecularProfileCase3.setMolecularProfileId("test3_cna");
        MolecularProfileCaseIdentifier molecularProfileCase4 = new MolecularProfileCaseIdentifier();
        molecularProfileCase4.setCaseId("sample_id_4");
        molecularProfileCase4.setMolecularProfileId("test4_cna");
        List<MolecularProfileCaseIdentifier> molecularProfileCaseSet2 = new ArrayList<>();
        molecularProfileCaseSet2.add(molecularProfileCase3);
        molecularProfileCaseSet2.add(molecularProfileCase4);

        MolecularProfileCaseIdentifier molecularProfileCase5 = new MolecularProfileCaseIdentifier();
        molecularProfileCase3.setCaseId("sample_id_5");
        molecularProfileCase3.setMolecularProfileId("test5_cna");
        MolecularProfileCaseIdentifier molecularProfileCase6 = new MolecularProfileCaseIdentifier();
        molecularProfileCase4.setCaseId("sample_id_6");
        molecularProfileCase4.setMolecularProfileId("test6_cna");
        List<MolecularProfileCaseIdentifier> molecularProfileCaseSet3 = new ArrayList<>();
        molecularProfileCaseSet3.add(molecularProfileCase5);
        molecularProfileCaseSet3.add(molecularProfileCase6);

        Map<String, List<MolecularProfileCaseIdentifier>> groupMolecularProfileCaseSets = new HashMap<String, List<MolecularProfileCaseIdentifier>>();
        groupMolecularProfileCaseSets.put("group1", molecularProfileCaseSet1);
        groupMolecularProfileCaseSets.put("group2", molecularProfileCaseSet2);

        AlterationCountByGene alterationSampleCountByGene1 = new AlterationCountByGene();
        alterationSampleCountByGene1.setEntrezGeneId(2);
        alterationSampleCountByGene1.setNumberOfAlteredCases(1);
        alterationSampleCountByGene1.setNumberOfProfiledCases(2);
        AlterationCountByGene alterationSampleCount1ByGene1 = new AlterationCountByGene();
        alterationSampleCount1ByGene1.setEntrezGeneId(3);
        alterationSampleCount1ByGene1.setNumberOfAlteredCases(0);
        alterationSampleCount1ByGene1.setNumberOfProfiledCases(2);

        AlterationCountByGene alterationSampleCountByGene2 = new AlterationCountByGene();
        alterationSampleCountByGene2.setEntrezGeneId(3);
        alterationSampleCountByGene2.setNumberOfAlteredCases(2);
        alterationSampleCountByGene2.setNumberOfProfiledCases(2);
        AlterationCountByGene alterationSampleCount1ByGene2 = new AlterationCountByGene();
        alterationSampleCount1ByGene2.setEntrezGeneId(2);
        alterationSampleCount1ByGene2.setNumberOfAlteredCases(0);
        alterationSampleCount1ByGene2.setNumberOfProfiledCases(2);

        List<Gene> genes = new ArrayList<>();
        Gene gene1 = new Gene();
        gene1.setEntrezGeneId(2);
        gene1.setHugoGeneSymbol("HUGO2");
        gene1.setGeneticEntityId(2);
        genes.add(gene1);
        Gene gene2 = new Gene();
        gene2.setEntrezGeneId(3);
        gene2.setHugoGeneSymbol("HUGO3");
        gene2.setGeneticEntityId(3);
        genes.add(gene2);

        Mockito.when(geneService.fetchGenes(Arrays.asList("2", "3"), "ENTREZ_GENE_ID", "SUMMARY")).thenReturn(genes);

        Map<String, List<? extends AlterationCountByGene>> mutationCountsbyEntrezGeneIdAndGroup = new HashMap<>();
        mutationCountsbyEntrezGeneIdAndGroup.put("group1", Arrays.asList(alterationSampleCountByGene1, alterationSampleCount1ByGene1));
        mutationCountsbyEntrezGeneIdAndGroup.put("group2", Arrays.asList(alterationSampleCountByGene2, alterationSampleCount1ByGene2));

        // START: for 2 groups

        Mockito.when(fisherExactTestCalculator.getCumulativePValue(1, 1, 2, 0)).thenReturn(1.0);
        Mockito.when(fisherExactTestCalculator.getCumulativePValue(2, 0, 0, 2)).thenReturn(0.3);

        List<AlterationEnrichment> result = alterationEnrichmentUtil.createAlterationEnrichments(
                mutationCountsbyEntrezGeneIdAndGroup, groupMolecularProfileCaseSets);

        Assert.assertEquals(2, result.size());
        AlterationEnrichment alterationEnrichment1 = result.get(0);
        Assert.assertEquals((Integer) 2, alterationEnrichment1.getEntrezGeneId());
        Assert.assertEquals("HUGO2", alterationEnrichment1.getHugoGeneSymbol());
        Assert.assertEquals(null, alterationEnrichment1.getCytoband());
        Assert.assertEquals(2, alterationEnrichment1.getCounts().size());
        Assert.assertEquals(new BigDecimal("1.0"), alterationEnrichment1.getpValue());
        alterationEnrichment1.getCounts().forEach(countSummary -> {
            if (countSummary.getName().equals("group2")) {
                Assert.assertEquals((Integer) 0, countSummary.getAlteredCount());
            } else if (countSummary.getName().equals("group1")) {
                Assert.assertEquals((Integer) 1, countSummary.getAlteredCount());
            }
        });

        AlterationEnrichment alterationEnrichment2 = result.get(1);
        Assert.assertEquals((Integer) 3, alterationEnrichment2.getEntrezGeneId());
        Assert.assertEquals("HUGO3", alterationEnrichment2.getHugoGeneSymbol());
        Assert.assertEquals(null, alterationEnrichment2.getCytoband());
        Assert.assertEquals(2, alterationEnrichment2.getCounts().size());
        Assert.assertEquals(new BigDecimal("0.3"), alterationEnrichment2.getpValue());
        alterationEnrichment2.getCounts().forEach(countSummary -> {
            if (countSummary.getName().equals("group2")) {
                Assert.assertEquals((Integer) 2, countSummary.getAlteredCount());
            } else if (countSummary.getName().equals("group1")) {
                Assert.assertEquals((Integer) 0, countSummary.getAlteredCount());
            }
        });

        // END: for 2 groups

        // START: for 3 groups

        groupMolecularProfileCaseSets.put("group3", molecularProfileCaseSet3);
        mutationCountsbyEntrezGeneIdAndGroup.put("group3",
                Arrays.asList(alterationSampleCountByGene1, alterationSampleCountByGene2));

        result = alterationEnrichmentUtil.createAlterationEnrichments(mutationCountsbyEntrezGeneIdAndGroup,
                groupMolecularProfileCaseSets);

        Assert.assertEquals(2, result.size());
        alterationEnrichment1 = result.get(0);
        Assert.assertEquals((Integer) 2, alterationEnrichment1.getEntrezGeneId());
        Assert.assertEquals("HUGO2", alterationEnrichment1.getHugoGeneSymbol());
        Assert.assertEquals(null, alterationEnrichment1.getCytoband());
        Assert.assertEquals(3, alterationEnrichment1.getCounts().size());

        Assert.assertEquals(new BigDecimal("0.4723665527410149"), alterationEnrichment1.getpValue());
        alterationEnrichment1.getCounts().forEach(countSummary -> {
            if (countSummary.getName().equals("group3")) {
                Assert.assertEquals((Integer) 1, countSummary.getAlteredCount());
            } else if (countSummary.getName().equals("group2")) {
                Assert.assertEquals((Integer) 0, countSummary.getAlteredCount());
            } else if (countSummary.getName().equals("group1")) {
                Assert.assertEquals((Integer) 1, countSummary.getAlteredCount());
            }
        });

        alterationEnrichment2 = result.get(1);
        Assert.assertEquals((Integer) 3, alterationEnrichment2.getEntrezGeneId());
        Assert.assertEquals("HUGO3", alterationEnrichment2.getHugoGeneSymbol());
        Assert.assertEquals(null, alterationEnrichment2.getCytoband());
        Assert.assertEquals(3, alterationEnrichment2.getCounts().size());

        Assert.assertEquals(new BigDecimal("0.04978706836786395"), alterationEnrichment2.getpValue());
        alterationEnrichment2.getCounts().forEach(countSummary -> {
            if (countSummary.getName().equals("group3")) {
                Assert.assertEquals((Integer) 2, countSummary.getAlteredCount());
            } else if (countSummary.getName().equals("group2")) {
                Assert.assertEquals((Integer) 2, countSummary.getAlteredCount());
            } else if (countSummary.getName().equals("group1")) {
                Assert.assertEquals((Integer) 0, countSummary.getAlteredCount());
            }
        });

        // END: for 3 groups
    }
}
