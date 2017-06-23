package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.cbioportal.model.CopyNumberCount;
import org.cbioportal.model.CopyNumberSampleCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneGeneticData;
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
    public void getDiscreteCopyNumbersInGeneticProfileBySampleListIdHomdelOrAmp() throws Exception {

        createGeneticProfile();

        List<DiscreteCopyNumberData> expectedDiscreteCopyNumberDataList = new ArrayList<>();
        DiscreteCopyNumberData discreteCopyNumberData = new DiscreteCopyNumberData();
        expectedDiscreteCopyNumberDataList.add(discreteCopyNumberData);
        
        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-2);
        
        Mockito.when(discreteCopyNumberRepository.getDiscreteCopyNumbersInGeneticProfileBySampleListId(
            GENETIC_PROFILE_ID, SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID), alterationTypes, PROJECTION))
            .thenReturn(expectedDiscreteCopyNumberDataList);
        
        List<DiscreteCopyNumberData> result = discreteCopyNumberService
            .getDiscreteCopyNumbersInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, SAMPLE_LIST_ID, 
                Arrays.asList(ENTREZ_GENE_ID), alterationTypes, PROJECTION);

        Assert.assertEquals(expectedDiscreteCopyNumberDataList, result);
    }

    @Test
    public void getDiscreteCopyNumbersInGeneticProfileBySampleListIdNonHomdelOrAmp() throws Exception {

        createGeneticProfile();
        
        List<GeneGeneticData> expectedGeneticDataList = new ArrayList<>();
        GeneGeneticData geneticData = new GeneGeneticData();
        geneticData.setValue("-1");
        geneticData.setGeneticProfileId(GENETIC_PROFILE_ID);
        geneticData.setSampleId(SAMPLE_ID1);
        geneticData.setEntrezGeneId(ENTREZ_GENE_ID);
        Gene gene = new Gene(); 
        geneticData.setGene(gene);
        expectedGeneticDataList.add(geneticData);
        
        Mockito.when(geneticDataService.getGeneticData(GENETIC_PROFILE_ID, SAMPLE_LIST_ID, 
            Arrays.asList(ENTREZ_GENE_ID), PROJECTION)).thenReturn(expectedGeneticDataList);

        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-1);
        
        List<DiscreteCopyNumberData> result = discreteCopyNumberService
            .getDiscreteCopyNumbersInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, SAMPLE_LIST_ID, 
                Arrays.asList(ENTREZ_GENE_ID), alterationTypes, PROJECTION);
        
        Assert.assertEquals(1, result.size());
        DiscreteCopyNumberData discreteCopyNumberData = result.get(0);
        Assert.assertEquals((Integer) (-1), discreteCopyNumberData.getAlteration());
        Assert.assertEquals(GENETIC_PROFILE_ID, discreteCopyNumberData.getGeneticProfileId());
        Assert.assertEquals(SAMPLE_ID1, discreteCopyNumberData.getSampleId());
        Assert.assertEquals(ENTREZ_GENE_ID, discreteCopyNumberData.getEntrezGeneId());
        Assert.assertEquals(gene, discreteCopyNumberData.getGene());
    }

    @Test
    public void getMetaDiscreteCopyNumbersInGeneticProfileBySampleListIdHomdelOrAmp() throws Exception {

        createGeneticProfile();

        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-2);

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(discreteCopyNumberRepository.getMetaDiscreteCopyNumbersInGeneticProfileBySampleListId(
            GENETIC_PROFILE_ID, SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID), alterationTypes))
            .thenReturn(expectedBaseMeta);

        BaseMeta result = discreteCopyNumberService.getMetaDiscreteCopyNumbersInGeneticProfileBySampleListId(
            GENETIC_PROFILE_ID, SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID), alterationTypes);
        
        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void getMetaDiscreteCopyNumbersInGeneticProfileBySampleListIdNonHomdelOrAmp() throws Exception {

        createGeneticProfile();

        List<GeneGeneticData> expectedGeneticDataList = new ArrayList<>();
        GeneGeneticData geneticData = new GeneGeneticData();
        geneticData.setValue("-1");
        expectedGeneticDataList.add(geneticData);

        Mockito.when(geneticDataService.getGeneticData(GENETIC_PROFILE_ID, SAMPLE_LIST_ID, 
            Arrays.asList(ENTREZ_GENE_ID), "ID")).thenReturn(expectedGeneticDataList);

        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-1);

        BaseMeta result = discreteCopyNumberService.getMetaDiscreteCopyNumbersInGeneticProfileBySampleListId(
            GENETIC_PROFILE_ID, SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID), alterationTypes);
        
        Assert.assertEquals((Integer) 1, result.getTotalCount());
    }

    @Test
    public void fetchDiscreteCopyNumbersInGeneticProfileHomdelOrAmp() throws Exception {

        createGeneticProfile();

        List<DiscreteCopyNumberData> expectedDiscreteCopyNumberDataList = new ArrayList<>();
        DiscreteCopyNumberData discreteCopyNumberData = new DiscreteCopyNumberData();
        expectedDiscreteCopyNumberDataList.add(discreteCopyNumberData);

        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-2);

        Mockito.when(discreteCopyNumberRepository.fetchDiscreteCopyNumbersInGeneticProfile(GENETIC_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID), alterationTypes, PROJECTION))
            .thenReturn(expectedDiscreteCopyNumberDataList);

        List<DiscreteCopyNumberData> result = discreteCopyNumberService.fetchDiscreteCopyNumbersInGeneticProfile(
            GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID), alterationTypes, PROJECTION);

        Assert.assertEquals(expectedDiscreteCopyNumberDataList, result);
    }

    @Test
    public void fetchDiscreteCopyNumbersInGeneticProfileNonHomdelOrAmp() throws Exception {

        createGeneticProfile();

        List<GeneGeneticData> expectedGeneticDataList = new ArrayList<>();
        GeneGeneticData geneticData = new GeneGeneticData();
        geneticData.setValue("-1");
        geneticData.setGeneticProfileId(GENETIC_PROFILE_ID);
        geneticData.setSampleId(SAMPLE_ID1);
        geneticData.setEntrezGeneId(ENTREZ_GENE_ID);
        Gene gene = new Gene();
        geneticData.setGene(gene);
        expectedGeneticDataList.add(geneticData);

        Mockito.when(geneticDataService.fetchGeneticData(GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID1), 
            Arrays.asList(ENTREZ_GENE_ID), PROJECTION)).thenReturn(expectedGeneticDataList);

        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-1);

        List<DiscreteCopyNumberData> result = discreteCopyNumberService.fetchDiscreteCopyNumbersInGeneticProfile(
            GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID), alterationTypes, PROJECTION);

        Assert.assertEquals(1, result.size());
        DiscreteCopyNumberData discreteCopyNumberData = result.get(0);
        Assert.assertEquals((Integer) (-1), discreteCopyNumberData.getAlteration());
        Assert.assertEquals(GENETIC_PROFILE_ID, discreteCopyNumberData.getGeneticProfileId());
        Assert.assertEquals(SAMPLE_ID1, discreteCopyNumberData.getSampleId());
        Assert.assertEquals(ENTREZ_GENE_ID, discreteCopyNumberData.getEntrezGeneId());
        Assert.assertEquals(gene, discreteCopyNumberData.getGene());
    }

    @Test
    public void fetchMetaDiscreteCopyNumbersInGeneticProfileHomdelOrAmp() throws Exception {

        createGeneticProfile();

        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-2);

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(discreteCopyNumberRepository.fetchMetaDiscreteCopyNumbersInGeneticProfile(GENETIC_PROFILE_ID,
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID), alterationTypes)).thenReturn(expectedBaseMeta);

        BaseMeta result = discreteCopyNumberService.fetchMetaDiscreteCopyNumbersInGeneticProfile(
            GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID), alterationTypes);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void fetchMetaDiscreteCopyNumbersInGeneticProfileNonHomdelOrAmp() throws Exception {

        createGeneticProfile();

        List<GeneGeneticData> expectedGeneticDataList = new ArrayList<>();
        GeneGeneticData geneticData = new GeneGeneticData();
        geneticData.setValue("-1");
        expectedGeneticDataList.add(geneticData);

        Mockito.when(geneticDataService.fetchGeneticData(GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID1), 
            Arrays.asList(ENTREZ_GENE_ID), "ID")).thenReturn(expectedGeneticDataList);

        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-1);

        BaseMeta result = discreteCopyNumberService.fetchMetaDiscreteCopyNumbersInGeneticProfile(
            GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID), alterationTypes);

        Assert.assertEquals((Integer) 1, result.getTotalCount());
    }

    @Test
    public void getSampleCountByGeneAndAlteration() throws Exception {
        
        List<CopyNumberSampleCountByGene> expectedCopyNumberSampleCountByGeneList = new ArrayList<>();
        expectedCopyNumberSampleCountByGeneList.add(new CopyNumberSampleCountByGene());

        Mockito.when(discreteCopyNumberRepository.getSampleCountByGeneAndAlterationAndSampleIds(GENETIC_PROFILE_ID, 
            null, Arrays.asList(ENTREZ_GENE_ID), Arrays.asList(-2)))
            .thenReturn(expectedCopyNumberSampleCountByGeneList);
        
        List<CopyNumberSampleCountByGene> result = discreteCopyNumberService
            .getSampleCountByGeneAndAlterationAndSampleIds(GENETIC_PROFILE_ID, null, Arrays.asList(ENTREZ_GENE_ID), 
                Arrays.asList(-2));
        
        Assert.assertEquals(expectedCopyNumberSampleCountByGeneList, result);
    }

    private void createGeneticProfile() throws GeneticProfileNotFoundException {
        
        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticAlterationType(GeneticProfile.GeneticAlterationType.COPY_NUMBER_ALTERATION);
        geneticProfile.setDatatype("DISCRETE");
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(geneticProfile);
    }

    @Test
    public void fetchCopyNumberCounts() throws Exception {

        List<CopyNumberSampleCountByGene> copyNumberSampleCountByGeneList = new ArrayList<>();
        CopyNumberSampleCountByGene copyNumberSampleCountByGene = new CopyNumberSampleCountByGene();
        copyNumberSampleCountByGene.setEntrezGeneId(ENTREZ_GENE_ID);
        copyNumberSampleCountByGene.setAlteration(-2);
        copyNumberSampleCountByGene.setSampleCount(1);
        copyNumberSampleCountByGeneList.add(copyNumberSampleCountByGene);

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticAlterationType(GeneticProfile.GeneticAlterationType.COPY_NUMBER_ALTERATION);
        geneticProfile.setDatatype("DISCRETE");
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(geneticProfile);

        Mockito.when(geneticDataService.getNumberOfSamplesInGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(2);

        Mockito.when(discreteCopyNumberService.getSampleCountByGeneAndAlterationAndSampleIds(GENETIC_PROFILE_ID, null,
            Arrays.asList(ENTREZ_GENE_ID), Arrays.asList(-2))).thenReturn(copyNumberSampleCountByGeneList);

        List<CopyNumberCount> result = discreteCopyNumberService.fetchCopyNumberCounts(GENETIC_PROFILE_ID,
            Arrays.asList(ENTREZ_GENE_ID), Arrays.asList(-2));

        Assert.assertEquals(1, result.size());
        CopyNumberCount copyNumberCount = result.get(0);
        Assert.assertEquals(GENETIC_PROFILE_ID, copyNumberCount.getGeneticProfileId());
        Assert.assertEquals(ENTREZ_GENE_ID, copyNumberCount.getEntrezGeneId());
        Assert.assertEquals((Integer) (-2), copyNumberCount.getAlteration());
        Assert.assertEquals((Integer) 2, copyNumberCount.getNumberOfSamples());
        Assert.assertEquals((Integer) 1, copyNumberCount.getNumberOfSamplesWithAlterationInGene());
    }
}