package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.cbioportal.model.CopyNumberSampleCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneticData;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.DiscreteCopyNumberRepository;
import org.cbioportal.service.GeneticDataService;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
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
public class DiscreteCopyNumberServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private DiscreteCopyNumberServiceImpl discreteCopyNumberService;
    
    @Mock
    private DiscreteCopyNumberRepository discreteCopyNumberRepository;
    @Mock
    private GeneticDataService geneticDataService;
    @Mock
    private GeneticProfileService geneticProfileService;
    
    @Test
    public void getDiscreteCopyNumbersInGeneticProfileHomdelOrAmp() throws Exception {

        createGeneticProfile();

        List<DiscreteCopyNumberData> expectedDiscreteCopyNumberDataList = new ArrayList<>();
        DiscreteCopyNumberData discreteCopyNumberData = new DiscreteCopyNumberData();
        expectedDiscreteCopyNumberDataList.add(discreteCopyNumberData);
        
        List<Integer> alterations = new ArrayList<>();
        alterations.add(-2);
        
        Mockito.when(discreteCopyNumberRepository.getDiscreteCopyNumbersInGeneticProfile(GENETIC_PROFILE_ID, SAMPLE_ID, 
            alterations, PROJECTION)).thenReturn(expectedDiscreteCopyNumberDataList);
        
        List<DiscreteCopyNumberData> result = discreteCopyNumberService.getDiscreteCopyNumbersInGeneticProfile(
            GENETIC_PROFILE_ID, SAMPLE_ID, alterations, PROJECTION);

        Assert.assertEquals(expectedDiscreteCopyNumberDataList, result);
    }

    @Test
    public void getDiscreteCopyNumbersInGeneticProfileNonHomdelOrAmp() throws Exception {

        createGeneticProfile();
        
        List<GeneticData> expectedGeneticDataList = new ArrayList<>();
        GeneticData geneticData = new GeneticData();
        geneticData.setValue("-1");
        geneticData.setGeneticProfileId(GENETIC_PROFILE_ID);
        geneticData.setSampleId(SAMPLE_ID);
        geneticData.setEntrezGeneId(ENTREZ_GENE_ID);
        Gene gene = new Gene(); 
        geneticData.setGene(gene);
        expectedGeneticDataList.add(geneticData);
        
        Mockito.when(geneticDataService.getGeneticData(GENETIC_PROFILE_ID, SAMPLE_ID, null, PROJECTION))
            .thenReturn(expectedGeneticDataList);

        List<Integer> alterations = new ArrayList<>();
        alterations.add(-1);
        
        List<DiscreteCopyNumberData> result = discreteCopyNumberService.getDiscreteCopyNumbersInGeneticProfile(
            GENETIC_PROFILE_ID, SAMPLE_ID, alterations, PROJECTION);
        
        Assert.assertEquals(1, result.size());
        DiscreteCopyNumberData discreteCopyNumberData = result.get(0);
        Assert.assertEquals((Integer) (-1), discreteCopyNumberData.getAlteration());
        Assert.assertEquals(GENETIC_PROFILE_ID, discreteCopyNumberData.getGeneticProfileId());
        Assert.assertEquals(SAMPLE_ID, discreteCopyNumberData.getSampleId());
        Assert.assertEquals(ENTREZ_GENE_ID, discreteCopyNumberData.getEntrezGeneId());
        Assert.assertEquals(gene, discreteCopyNumberData.getGene());
    }

    @Test
    public void getMetaDiscreteCopyNumbersInGeneticProfileHomdelOrAmp() throws Exception {

        createGeneticProfile();

        List<Integer> alterations = new ArrayList<>();
        alterations.add(-2);

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(discreteCopyNumberRepository.getMetaDiscreteCopyNumbersInGeneticProfile(GENETIC_PROFILE_ID, 
            SAMPLE_ID, alterations)).thenReturn(expectedBaseMeta);

        BaseMeta result = discreteCopyNumberService.getMetaDiscreteCopyNumbersInGeneticProfile(
            GENETIC_PROFILE_ID, SAMPLE_ID, alterations);
        
        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void getMetaDiscreteCopyNumbersInGeneticProfileNonHomdelOrAmp() throws Exception {

        createGeneticProfile();

        List<GeneticData> expectedGeneticDataList = new ArrayList<>();
        GeneticData geneticData = new GeneticData();
        geneticData.setValue("-1");
        expectedGeneticDataList.add(geneticData);

        Mockito.when(geneticDataService.getGeneticData(GENETIC_PROFILE_ID, SAMPLE_ID, null, "ID"))
            .thenReturn(expectedGeneticDataList);

        List<Integer> alterations = new ArrayList<>();
        alterations.add(-1);

        BaseMeta result = discreteCopyNumberService.getMetaDiscreteCopyNumbersInGeneticProfile(
            GENETIC_PROFILE_ID, SAMPLE_ID, alterations);
        
        Assert.assertEquals((Integer) 1, result.getTotalCount());
    }

    @Test
    public void fetchDiscreteCopyNumbersInGeneticProfileHomdelOrAmp() throws Exception {

        createGeneticProfile();

        List<DiscreteCopyNumberData> expectedDiscreteCopyNumberDataList = new ArrayList<>();
        DiscreteCopyNumberData discreteCopyNumberData = new DiscreteCopyNumberData();
        expectedDiscreteCopyNumberDataList.add(discreteCopyNumberData);

        List<Integer> alterations = new ArrayList<>();
        alterations.add(-2);

        Mockito.when(discreteCopyNumberRepository.fetchDiscreteCopyNumbersInGeneticProfile(GENETIC_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID), alterations, PROJECTION)).thenReturn(expectedDiscreteCopyNumberDataList);

        List<DiscreteCopyNumberData> result = discreteCopyNumberService.fetchDiscreteCopyNumbersInGeneticProfile(
            GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID), alterations, PROJECTION);

        Assert.assertEquals(expectedDiscreteCopyNumberDataList, result);
    }

    @Test
    public void fetchDiscreteCopyNumbersInGeneticProfileNonHomdelOrAmp() throws Exception {

        createGeneticProfile();

        List<GeneticData> expectedGeneticDataList = new ArrayList<>();
        GeneticData geneticData = new GeneticData();
        geneticData.setValue("-1");
        geneticData.setGeneticProfileId(GENETIC_PROFILE_ID);
        geneticData.setSampleId(SAMPLE_ID);
        geneticData.setEntrezGeneId(ENTREZ_GENE_ID);
        Gene gene = new Gene();
        geneticData.setGene(gene);
        expectedGeneticDataList.add(geneticData);

        Mockito.when(geneticDataService.fetchGeneticData(GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID), null, 
            PROJECTION)).thenReturn(expectedGeneticDataList);

        List<Integer> alterations = new ArrayList<>();
        alterations.add(-1);

        List<DiscreteCopyNumberData> result = discreteCopyNumberService.fetchDiscreteCopyNumbersInGeneticProfile(
            GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID), alterations, PROJECTION);

        Assert.assertEquals(1, result.size());
        DiscreteCopyNumberData discreteCopyNumberData = result.get(0);
        Assert.assertEquals((Integer) (-1), discreteCopyNumberData.getAlteration());
        Assert.assertEquals(GENETIC_PROFILE_ID, discreteCopyNumberData.getGeneticProfileId());
        Assert.assertEquals(SAMPLE_ID, discreteCopyNumberData.getSampleId());
        Assert.assertEquals(ENTREZ_GENE_ID, discreteCopyNumberData.getEntrezGeneId());
        Assert.assertEquals(gene, discreteCopyNumberData.getGene());
    }

    @Test
    public void fetchMetaDiscreteCopyNumbersInGeneticProfileHomdelOrAmp() throws Exception {

        createGeneticProfile();

        List<Integer> alterations = new ArrayList<>();
        alterations.add(-2);

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(discreteCopyNumberRepository.fetchMetaDiscreteCopyNumbersInGeneticProfile(GENETIC_PROFILE_ID,
            Arrays.asList(SAMPLE_ID), alterations)).thenReturn(expectedBaseMeta);

        BaseMeta result = discreteCopyNumberService.fetchMetaDiscreteCopyNumbersInGeneticProfile(
            GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID), alterations);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void fetchMetaDiscreteCopyNumbersInGeneticProfileNonHomdelOrAmp() throws Exception {

        createGeneticProfile();

        List<GeneticData> expectedGeneticDataList = new ArrayList<>();
        GeneticData geneticData = new GeneticData();
        geneticData.setValue("-1");
        expectedGeneticDataList.add(geneticData);

        Mockito.when(geneticDataService.fetchGeneticData(GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID), null, "ID"))
            .thenReturn(expectedGeneticDataList);

        List<Integer> alterations = new ArrayList<>();
        alterations.add(-1);

        BaseMeta result = discreteCopyNumberService.fetchMetaDiscreteCopyNumbersInGeneticProfile(
            GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID), alterations);

        Assert.assertEquals((Integer) 1, result.getTotalCount());
    }

    @Test
    public void getSampleCountByGeneAndAlteration() throws Exception {
        
        List<CopyNumberSampleCountByGene> expectedCopyNumberSampleCountByGeneList = new ArrayList<>();
        expectedCopyNumberSampleCountByGeneList.add(new CopyNumberSampleCountByGene());

        Mockito.when(discreteCopyNumberRepository.getSampleCountByGeneAndAlteration(GENETIC_PROFILE_ID, 
            Arrays.asList(ENTREZ_GENE_ID), Arrays.asList(-2))).thenReturn(expectedCopyNumberSampleCountByGeneList);
        
        List<CopyNumberSampleCountByGene> result = discreteCopyNumberService.getSampleCountByGeneAndAlteration(
            GENETIC_PROFILE_ID, Arrays.asList(ENTREZ_GENE_ID), Arrays.asList(-2));
        
        Assert.assertEquals(expectedCopyNumberSampleCountByGeneList, result);
    }

    private void createGeneticProfile() throws GeneticProfileNotFoundException {
        
        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticAlterationType(GeneticProfile.GeneticAlterationType.COPY_NUMBER_ALTERATION);
        geneticProfile.setDatatype("DISCRETE");
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(geneticProfile);
    }
}