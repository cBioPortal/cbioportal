package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.Gene;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenesetRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GenesetNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GenesetServiceImplTest extends BaseServiceImplTest {

    public static final String GENESET_ID_1 = "geneset_id_1";
    private static final Integer INTERNAL_ID_1 = 1;
    public static final String GENESET_ID_2 = "geneset_id_2";
    private static final Integer INTERNAL_ID_2 = 2;

    @InjectMocks
    private GenesetServiceImpl genesetService;

    @Mock
    private GenesetRepository genesetRepository;
    @Mock
    private SampleService sampleService;
    @Mock
    private MolecularProfileService geneticProfileService;

    @Test
    public void getAllGenesets() {

        List<Geneset> genesetList = createGenesetList();
        Mockito.when(genesetRepository.getAllGenesets(PROJECTION, PAGE_SIZE, PAGE_NUMBER))
            .thenReturn(genesetList);

        List<Geneset> result = genesetService.getAllGenesets(PROJECTION, PAGE_SIZE, PAGE_NUMBER);

        Assert.assertEquals(genesetList, result);
    }

    @Test
    public void getMetaGenesets() {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(genesetRepository.getMetaGenesets()).thenReturn(expectedBaseMeta);
        BaseMeta result = genesetService.getMetaGenesets();

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void getGeneset() throws GenesetNotFoundException {

        Geneset geneset = createGenesetList().get(0);
        Mockito.when(genesetRepository.getGeneset(GENESET_ID_1))
            .thenReturn(geneset);

        Geneset result = genesetService.getGeneset(GENESET_ID_1);
        Assert.assertEquals(geneset, result);
    }
    
    @Test(expected = GenesetNotFoundException.class)
    public void getGeneByEntrezGeneIdNotFound() throws GenesetNotFoundException {

        Geneset geneset = createGenesetList().get(0);
        Mockito.when(genesetRepository.getGeneset(GENESET_ID_1))
            .thenReturn(geneset);
        //expect GenesetNotFoundException here:
        genesetService.getGeneset("wrongId");
    }
    
    @Test
    public void getGenesByGenesetId() throws GenesetNotFoundException {

        List<Gene> genes = createGeneList();
        Mockito.when(genesetRepository.getGenesByGenesetId(GENESET_ID_2))
            .thenReturn(genes);
        
        Geneset geneset = createGenesetList().get(1);
        Mockito.when(genesetRepository.getGeneset(GENESET_ID_2))
            .thenReturn(geneset);

        List<Gene> result = genesetService.getGenesByGenesetId(GENESET_ID_2);
        Assert.assertEquals(genes, result);
    }

    private List<Geneset> createGenesetList() {
        List<Geneset> genesetList = new ArrayList<>();
        Geneset geneset1 = new Geneset();
        geneset1.setInternalId(INTERNAL_ID_1);
        geneset1.setGenesetId(GENESET_ID_1);
        genesetList.add(geneset1);
        Geneset geneset2 = new Geneset();
        geneset2.setInternalId(INTERNAL_ID_2);
        geneset2.setGenesetId(GENESET_ID_2);
        genesetList.add(geneset2);
        return genesetList;
    }

    private List<Gene> createGeneList() {
        List<Gene> geneList = new ArrayList<>();
        Gene gene1 = new Gene();
        gene1.setEntrezGeneId(1);
        geneList.add(gene1);
        Gene gene2 = new Gene();
        gene2.setEntrezGeneId(2);
        geneList.add(gene2);
        return geneList;
    }
}
