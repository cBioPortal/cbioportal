package org.cbioportal.service.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.Gene;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.GeneService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

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

        Map<String, List<MolecularProfileCaseIdentifier>> groupMolecularProfileCaseSets = new HashMap<String, List<MolecularProfileCaseIdentifier>>();
        groupMolecularProfileCaseSets.put("altered group", molecularProfileCaseSet1);
        groupMolecularProfileCaseSets.put("unaltered group", molecularProfileCaseSet2);

        
        
        List<AlterationCountByGene> alterationSampleCountByGenes = new ArrayList<>();
        AlterationCountByGene alterationSampleCountByGene1 = new AlterationCountByGene();
        alterationSampleCountByGene1.setEntrezGeneId(2);
        alterationSampleCountByGene1.setNumberOfAlteredCases(1);
        alterationSampleCountByGenes.add(alterationSampleCountByGene1);
        AlterationCountByGene alterationSampleCountByGene2 = new AlterationCountByGene();
        alterationSampleCountByGene2.setEntrezGeneId(3);
        alterationSampleCountByGene2.setNumberOfAlteredCases(2);
        alterationSampleCountByGenes.add(alterationSampleCountByGene2);

        List<Gene> genes = new ArrayList<>();
        Gene gene1 = new Gene();
        gene1.setEntrezGeneId(2);
        gene1.setHugoGeneSymbol("HUGO2");
        gene1.setCytoband("CYTOBAND2");
        genes.add(gene1);
        Gene gene2 = new Gene();
        gene2.setEntrezGeneId(3);
        gene2.setHugoGeneSymbol("HUGO3");
        gene2.setCytoband("CYTOBAND3");
        genes.add(gene2);

        Mockito.when(geneService.fetchGenes(Arrays.asList("2", "3"), "ENTREZ_GENE_ID", "SUMMARY")).thenReturn(genes);
        
        Map<String, List<? extends AlterationCountByGene>> mutationCountsbyEntrezGeneIdAndGroup = new HashMap<>();
        mutationCountsbyEntrezGeneIdAndGroup.put("altered group", Arrays.asList(alterationSampleCountByGene1));
        mutationCountsbyEntrezGeneIdAndGroup.put("unaltered group", Arrays.asList(alterationSampleCountByGene2));


        Mockito.when(fisherExactTestCalculator.getCumulativePValue(2, 0, 1, 1)).thenReturn(1.0);
        Mockito.when(fisherExactTestCalculator.getCumulativePValue(0, 2, 2, 0)).thenReturn(0.3);

        
        List<AlterationEnrichment> result = alterationEnrichmentUtil.createAlterationEnrichments(mutationCountsbyEntrezGeneIdAndGroup, 
                groupMolecularProfileCaseSets, "SAMPLE");

        Assert.assertEquals(2, result.size());
        AlterationEnrichment alterationEnrichment1 = result.get(0);
        Assert.assertEquals((Integer) 2, alterationEnrichment1.getEntrezGeneId());
        Assert.assertEquals("HUGO2", alterationEnrichment1.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND2", alterationEnrichment1.getCytoband());
        Assert.assertEquals(2, alterationEnrichment1.getCounts().size());
        Assert.assertEquals((Integer) 1, alterationEnrichment1.getCounts().get(0).getAlteredCount());
        Assert.assertEquals((Integer) 0, alterationEnrichment1.getCounts().get(1).getAlteredCount());
        Assert.assertEquals(new BigDecimal("1.0"), alterationEnrichment1.getpValue());
        AlterationEnrichment alterationEnrichment2 = result.get(1);
        Assert.assertEquals((Integer) 3, alterationEnrichment2.getEntrezGeneId());
        Assert.assertEquals("HUGO3", alterationEnrichment2.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND3", alterationEnrichment2.getCytoband());
        Assert.assertEquals(2, alterationEnrichment2.getCounts().size());
        Assert.assertEquals((Integer) 0, alterationEnrichment2.getCounts().get(0).getAlteredCount());
        Assert.assertEquals((Integer) 2, alterationEnrichment2.getCounts().get(1).getAlteredCount());
        Assert.assertEquals(new BigDecimal("0.3"), alterationEnrichment2.getpValue());
    }
}
