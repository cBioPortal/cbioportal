package org.cbioportal.service.impl;

import org.cbioportal.model.Gene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GeneRepository;
import org.cbioportal.service.exception.GeneNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class GeneServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private GeneServiceImpl geneService;

    @Mock
    private GeneRepository geneRepository;

    @Test
    public void getAllGenes() throws Exception {

        List<Gene> expectedGeneList = new ArrayList<>();
        Gene gene = new Gene();
        gene.setCytoband("19q13.4");
        expectedGeneList.add(gene);

        Mockito.when(geneRepository.getAllGenes(PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
                .thenReturn(expectedGeneList);

        List<Gene> result = geneService.getAllGenes(PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedGeneList, result);
        Assert.assertEquals("19", result.get(0).getChromosome());
    }

    @Test
    public void getMetaGenes() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(geneRepository.getMetaGenes()).thenReturn(expectedBaseMeta);
        BaseMeta result = geneService.getMetaGenes();

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = GeneNotFoundException.class)
    public void getGeneByEntrezGeneIdNotFound() throws Exception {

        Mockito.when(geneRepository.getGeneByEntrezGeneId(ENTREZ_GENE_ID)).thenReturn(null);

        geneService.getGene(ENTREZ_GENE_ID.toString());
    }

    @Test
    public void getGeneByEntrezGeneId() throws Exception {

        Gene expectedGene = new Gene();
        expectedGene.setCytoband("Xq13.3");
        Mockito.when(geneRepository.getGeneByEntrezGeneId(ENTREZ_GENE_ID)).thenReturn(expectedGene);
        Gene result = geneService.getGene(ENTREZ_GENE_ID.toString());

        Assert.assertEquals(expectedGene, result);
        Assert.assertEquals("X", result.getChromosome());
    }

    @Test(expected = GeneNotFoundException.class)
    public void getGeneByHugoGeneSymbolNotFound() throws Exception {

        Mockito.when(geneRepository.getGeneByHugoGeneSymbol(HUGO_GENE_SYMBOL)).thenReturn(null);

        geneService.getGene(HUGO_GENE_SYMBOL);
    }

    @Test
    public void getGeneByHugoGeneSymbol() throws Exception {

        Gene expectedGene = new Gene();
        expectedGene.setCytoband("Yq11");
        Mockito.when(geneRepository.getGeneByHugoGeneSymbol(HUGO_GENE_SYMBOL)).thenReturn(expectedGene);
        Gene result = geneService.getGene(HUGO_GENE_SYMBOL);

        Assert.assertEquals(expectedGene, result);
        Assert.assertEquals("Y", result.getChromosome());
    }

    @Test
    public void getAliasesOfGeneByEntrezGeneId() throws Exception {

        List<String> expectedAliases = new ArrayList<>();
        expectedAliases.add("alias");
        Mockito.when(geneRepository.getAliasesOfGeneByEntrezGeneId(ENTREZ_GENE_ID)).thenReturn(expectedAliases);
        List<String> result = geneService.getAliasesOfGene(ENTREZ_GENE_ID.toString());

        Assert.assertEquals(expectedAliases, result);
    }

    @Test
    public void getAliasesOfGeneByHugoGeneSymbol() throws Exception {

        List<String> expectedAliases = new ArrayList<>();
        expectedAliases.add("alias");
        Mockito.when(geneRepository.getAliasesOfGeneByHugoGeneSymbol(HUGO_GENE_SYMBOL)).thenReturn(expectedAliases);
        List<String> result = geneService.getAliasesOfGene(HUGO_GENE_SYMBOL);

        Assert.assertEquals(expectedAliases, result);
    }

    @Test
    public void fetchGenes() throws Exception {

        List<Gene> expectedGeneList1 = new ArrayList<>();
        Gene gene1 = new Gene();
        gene1.setCytoband("12q13.13");
        expectedGeneList1.add(gene1);
        List<Gene> expectedGeneList2 = new ArrayList<>();
        Gene gene2 = new Gene();
        expectedGeneList2.add(gene2);

        List<String> geneIds = new ArrayList<>();
        geneIds.add(ENTREZ_GENE_ID.toString());
        geneIds.add(HUGO_GENE_SYMBOL);

        Mockito.when(geneRepository.fetchGenesByEntrezGeneIds(Arrays.asList(ENTREZ_GENE_ID), PROJECTION))
                .thenReturn(expectedGeneList1);

        Mockito.when(geneRepository.fetchGenesByHugoGeneSymbols(Arrays.asList(HUGO_GENE_SYMBOL), PROJECTION))
                .thenReturn(expectedGeneList2);

        List<Gene> result = geneService.fetchGenes(geneIds, PROJECTION);

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(gene1, result.get(0));
        Assert.assertEquals(gene2, result.get(1));
        Assert.assertEquals("12", result.get(0).getChromosome());
        Assert.assertNull(result.get(1).getChromosome());
    }

    @Test
    public void fetchMetaGenes() throws Exception {

        BaseMeta expectedBaseMeta1 = new BaseMeta();
        expectedBaseMeta1.setTotalCount(1);
        BaseMeta expectedBaseMeta2 = new BaseMeta();
        expectedBaseMeta2.setTotalCount(1);

        List<String> geneIds = new ArrayList<>();
        geneIds.add(ENTREZ_GENE_ID.toString());
        geneIds.add(HUGO_GENE_SYMBOL);

        Mockito.when(geneRepository.fetchMetaGenesByEntrezGeneIds(Arrays.asList(ENTREZ_GENE_ID)))
                .thenReturn(expectedBaseMeta1);

        Mockito.when(geneRepository.fetchMetaGenesByHugoGeneSymbols(Arrays.asList(HUGO_GENE_SYMBOL)))
                .thenReturn(expectedBaseMeta2);

        BaseMeta result = geneService.fetchMetaGenes(geneIds);

        Assert.assertEquals((Integer) 2, result.getTotalCount());
    }
}