package org.cbioportal.service.impl;

import org.cbioportal.model.Gene;
import org.cbioportal.model.ReferenceGenome;
import org.cbioportal.model.ReferenceGenomeGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ReferenceGenomeGeneRepository;
import org.cbioportal.service.exception.GeneNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceGenomeGeneServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private ReferenceGenomeGeneServiceImpl geneService;

    @Mock
    private ReferenceGenomeGeneRepository geneRepository;

    @Test
    public void getAllGenesByGenomeName() throws Exception {

        List<ReferenceGenomeGene> expectedGeneList = new ArrayList<>();
        ReferenceGenomeGene gene = new ReferenceGenomeGene();
        gene.setEntrezGeneId(ENTREZ_GENE_ID_2);
        gene.setReferenceGenomeId(REFERENCE_GENOME_ID);
        expectedGeneList.add(gene);

        Mockito.when(geneRepository.getAllGenesByGenomeName(ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME))
            .thenReturn(expectedGeneList);

        List<ReferenceGenomeGene> result = geneService.fetchAllReferenceGenomeGenes(ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME);

        Assert.assertEquals(expectedGeneList, result);
    }

    @Test
    public void getGenesByGenomeName() throws Exception {

        List<ReferenceGenomeGene> expectedGeneList = new ArrayList<>();
        ReferenceGenomeGene gene1 = new ReferenceGenomeGene();
        gene1.setEntrezGeneId(ENTREZ_GENE_ID_1);
        gene1.setReferenceGenomeId(REFERENCE_GENOME_ID);
        expectedGeneList.add(gene1);
        ReferenceGenomeGene gene2 = new ReferenceGenomeGene();
        gene2.setEntrezGeneId(ENTREZ_GENE_ID_2);
        gene2.setReferenceGenomeId(REFERENCE_GENOME_ID);
        expectedGeneList.add(gene2);
        List<Integer> geneIds = new ArrayList<>();
        geneIds.add(ENTREZ_GENE_ID_1);
        geneIds.add(ENTREZ_GENE_ID_2);
        Mockito.when(geneRepository.getGenesByGenomeName(geneIds,ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME))
            .thenReturn(expectedGeneList);

        List<ReferenceGenomeGene> result = geneService.fetchGenesByGenomeName(geneIds,ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME);

        Assert.assertEquals(expectedGeneList, result);
    }
    
    @Test
    public void getReferenceGenomeGene() throws Exception {

        Gene gene = new Gene();
        gene.setEntrezGeneId(ENTREZ_GENE_ID_1);
        gene.setHugoGeneSymbol("HUGO2");
        gene.setGeneticEntityId(GENETIC_ENTITY_ID_1);
        ReferenceGenomeGene expectedGene = new ReferenceGenomeGene();
        expectedGene.setEntrezGeneId(ENTREZ_GENE_ID_1);
        expectedGene.setReferenceGenomeId(REFERENCE_GENOME_ID);
        Mockito.when(geneRepository.getReferenceGenomeGene(gene.getEntrezGeneId(), 
                ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME)).thenReturn(expectedGene);

        ReferenceGenomeGene result = geneService.getReferenceGenomeGene(gene.getEntrezGeneId(),
                    ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME);

        Assert.assertEquals(expectedGene, result);
    }
    
    @Test
    public void getReferenceGenomeGeneByEntityId() throws Exception {

        ReferenceGenomeGene expectedGene = new ReferenceGenomeGene();
        expectedGene.setEntrezGeneId(ENTREZ_GENE_ID_1);
        expectedGene.setReferenceGenomeId(REFERENCE_GENOME_ID);
        Mockito.when(geneRepository.getReferenceGenomeGeneByEntityId(GENETIC_ENTITY_ID_1, 
            ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME)).thenReturn(expectedGene);

        ReferenceGenomeGene result = geneService.getReferenceGenomeGeneByEntityId(GENETIC_ENTITY_ID_1,
                    ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME);

        Assert.assertEquals(expectedGene, result);
    }
    
}
