package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.cbioportal.model.GeneticAlteration;
import org.cbioportal.model.GeneticData;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.GeneticDataRepository;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.SampleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class GeneticDataServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private GeneticDataServiceImpl geneticDataService;

    @Mock
    private GeneticDataRepository geneticDataRepository;
    @Mock
    private SampleService sampleService;
    @Mock
    private GeneticProfileService geneticProfileService;

    @Test
    public void getGeneticData() throws Exception {
        
        Mockito.when(geneticDataRepository.getCommaSeparatedSampleIdsOfGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(
            "1,2,");

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setCancerStudyIdentifier(STUDY_ID);
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(geneticProfile);
        
        List<Sample> sampleList = new ArrayList<>();
        Sample sample = new Sample();
        sample.setInternalId(1);
        sample.setStableId(SAMPLE_ID);
        sampleList.add(sample);
        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID), "ID"))
            .thenReturn(sampleList);

        List<GeneticAlteration> geneticAlterationList = new ArrayList<>();
        GeneticAlteration geneticAlteration = new GeneticAlteration();
        geneticAlteration.setEntrezGeneId(ENTREZ_GENE_ID);
        geneticAlteration.setValues("0.4674,-0.3456");
        geneticAlterationList.add(geneticAlteration);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(ENTREZ_GENE_ID);
        Mockito.when(geneticDataRepository.getGeneticAlterations(GENETIC_PROFILE_ID, entrezGeneIds, PROJECTION))
            .thenReturn(geneticAlterationList);

        List<GeneticData> result = geneticDataService.getGeneticData(GENETIC_PROFILE_ID, SAMPLE_ID, entrezGeneIds, 
            PROJECTION);

        Assert.assertEquals(1, result.size());
        GeneticData geneticData = result.get(0);
        Assert.assertEquals(ENTREZ_GENE_ID, geneticData.getEntrezGeneId());
        Assert.assertEquals(GENETIC_PROFILE_ID, geneticData.getGeneticProfileId());
        Assert.assertEquals(SAMPLE_ID, geneticData.getSampleId());
        Assert.assertEquals("0.4674", geneticData.getValue());
    }

    @Test
    public void getGeneticDataOfAllSamplesOfGeneticProfile() throws Exception {

        Mockito.when(geneticDataRepository.getCommaSeparatedSampleIdsOfGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(
            "1,2,");

        List<GeneticAlteration> geneticAlterationList = new ArrayList<>();
        GeneticAlteration geneticAlteration = new GeneticAlteration();
        geneticAlteration.setEntrezGeneId(ENTREZ_GENE_ID);
        geneticAlteration.setValues("0.4674,-0.3456");
        geneticAlterationList.add(geneticAlteration);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(ENTREZ_GENE_ID);
        Mockito.when(geneticDataRepository.getGeneticAlterations(GENETIC_PROFILE_ID, entrezGeneIds, PROJECTION))
            .thenReturn(geneticAlterationList);
        
        List<Integer> internalIds = new ArrayList<>();
        internalIds.add(1);
        internalIds.add(2);
        
        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setInternalId(1);
        sample1.setStableId(SAMPLE_ID);
        samples.add(sample1);
        Sample sample2 = new Sample();
        sample2.setInternalId(2);
        sample2.setStableId("sample_id_2");
        samples.add(sample2);
        Mockito.when(sampleService.getSamplesByInternalIds(internalIds)).thenReturn(samples);

        List<GeneticData> result = geneticDataService.fetchGeneticData(GENETIC_PROFILE_ID, null, entrezGeneIds, 
            PROJECTION);

        Assert.assertEquals(2, result.size());
        GeneticData geneticData1 = result.get(0);
        Assert.assertEquals(ENTREZ_GENE_ID, geneticData1.getEntrezGeneId());
        Assert.assertEquals(GENETIC_PROFILE_ID, geneticData1.getGeneticProfileId());
        Assert.assertEquals(SAMPLE_ID, geneticData1.getSampleId());
        Assert.assertEquals("0.4674", geneticData1.getValue());
        GeneticData geneticData2 = result.get(1);
        Assert.assertEquals(ENTREZ_GENE_ID, geneticData2.getEntrezGeneId());
        Assert.assertEquals(GENETIC_PROFILE_ID, geneticData2.getGeneticProfileId());
        Assert.assertEquals("sample_id_2", geneticData2.getSampleId());
        Assert.assertEquals("-0.3456", geneticData2.getValue());
    }
}