package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneticData;
import org.cbioportal.model.GeneticDataSamples;
import org.cbioportal.model.GeneticDataValues;
import org.cbioportal.model.GeneticEntity;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.GeneticProfile.GeneticAlterationType;
import org.cbioportal.model.Sample;
import org.cbioportal.model.GeneticEntity.EntityType;
import org.cbioportal.persistence.GeneticDataRepository;
import org.cbioportal.persistence.GeneticEntityRepository;
import org.cbioportal.persistence.GeneticProfileRepository;
import org.cbioportal.persistence.SampleRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GeneticDataServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private GeneticDataServiceImpl geneticDataService;
    
    @Mock
    private GeneticDataRepository geneticDataRepository;
    
    @Mock
    private GeneticEntityRepository geneticEntityRepository;
    
    @Mock
    private GeneticProfileRepository geneticProfileRepository;
    
    @Mock
    private SampleRepository sampleRepository;

    //test data
    private String geneticProfileStableId = "acc_tcga_mrna";
    private String studyId = "acc_tcga";

	/**
	 * This is executed n times, for each of the n test methods below:
	 * @throws DaoException
	 */
    @Before 
    public void setUp() {
    	//genes in this test
    	Gene gene1 = new Gene();
    	gene1.setEntityId(1);
    	gene1.setEntrezGeneId(1001);
    	Mockito.when(geneticEntityRepository.getGeneticEntity("1001", GeneticEntity.EntityType.GENE)).thenReturn(gene1);
    	Mockito.when(geneticEntityRepository.getGeneticEntity(1, GeneticEntity.EntityType.GENE)).thenReturn(gene1);
    	Gene gene2 = new Gene();
    	gene2.setEntityId(2);
    	gene2.setEntrezGeneId(2002);
    	Mockito.when(geneticEntityRepository.getGeneticEntity("2002", GeneticEntity.EntityType.GENE)).thenReturn(gene2);
    	Mockito.when(geneticEntityRepository.getGeneticEntity(2, GeneticEntity.EntityType.GENE)).thenReturn(gene2);
    	
        //stub for genetic profile
        GeneticProfile dummyGeneticProfile = new GeneticProfile();
        dummyGeneticProfile.setStableId(geneticProfileStableId);
        dummyGeneticProfile.setGeneticProfileId(1);
        dummyGeneticProfile.setCancerStudyIdentifier(studyId);
        dummyGeneticProfile.setGeneticAlterationType(GeneticAlterationType.MRNA_EXPRESSION);
        Mockito.when(geneticProfileRepository.getGeneticProfile(geneticProfileStableId)).thenReturn(dummyGeneticProfile);
        
        //stub for samples
        Sample sample1 = new Sample();
    	sample1.setInternalId(1);
    	sample1.setStableId("SAMPLE_1");
    	Mockito.when(sampleRepository.getSampleInStudy(studyId, "SAMPLE_1")).thenReturn(sample1);
    	Sample sample2 = new Sample();
    	sample2.setInternalId(2);
    	sample2.setStableId("SAMPLE_2");
    	Mockito.when(sampleRepository.getSampleInStudy(studyId, "SAMPLE_2")).thenReturn(sample2);
    	Mockito.when(sampleRepository.fetchSamplesInSameStudyByInternalIds(studyId, Arrays.asList(1,2), null)).thenReturn(
    			Arrays.asList(sample1, sample2));
    	
	}

    @Test
    public void testGetAndFetchGeneticDataInGeneticProfile() throws Exception {
    	
        GeneticProfile geneticProfile = geneticProfileRepository.getGeneticProfile(geneticProfileStableId);
        
        //stub the genet list and values to be returned by repository methods that will be used by the method being tested here:
        List<GeneticDataValues> geneListAndValues = new ArrayList<GeneticDataValues>();
        geneListAndValues.add(new GeneticDataValues());
        geneListAndValues.get(0).setGeneticEntityId(1);
        geneListAndValues.get(0).setGeneticProfileId(geneticProfile.getGeneticProfileId());
        geneListAndValues.get(0).setOrderedValuesList("0.2,34.99");
        geneListAndValues.add(new GeneticDataValues());
        geneListAndValues.get(1).setGeneticEntityId(2);
        geneListAndValues.get(1).setGeneticProfileId(geneticProfile.getGeneticProfileId());
        geneListAndValues.get(1).setOrderedValuesList("0.89,15.09");
        //parameters don't matter much here, we want it to return geneListAndValues:
        Mockito.when(geneticDataRepository.getGeneticDataValuesInGeneticProfile(Mockito.anyString(), Mockito.anyListOf(Integer.class), 
        		Mockito.anyInt(), Mockito.anyInt())).thenReturn(geneListAndValues);
        
        //stub the samples to be returned by repository method: 
        GeneticDataSamples samples = new GeneticDataSamples();
        samples.setGeneticProfileId(geneticProfile.getGeneticProfileId());
        samples.setOrderedSamplesList("1,2"); //these are ids for SAMPLE_1,SAMPLE_2
        //parameters don't matter much here, we want it to return samples list:
        Mockito.when(geneticDataRepository.getGeneticDataSamplesInGeneticProfile(Mockito.anyString(), 
        		Mockito.anyInt(), Mockito.anyInt())).thenReturn(samples);
        
    	//call the method to be tested: getAllGeneticDataInGeneticProfile 
        //and check if it correctly combines GeneticDataSamples and GeneticDataValues
        //into the corresponding list of GeneticData elements:
    	List<GeneticData> result = geneticDataService.getAllGeneticDataInGeneticProfile(geneticProfileStableId,  PROJECTION,  PAGE_SIZE, PAGE_NUMBER);
    	//expect 4 items:
    	Assert.assertEquals(4,  result.size());
    	
    	//what we expect: 2 samples x 2 genetic entities = 4 GeneticData items:
    	//SAMPLE_1:
    	//   1001 value: 0.2
    	//   2002 value: 0.89
    	//SAMPLE_2:
    	//   1001 value: 34.99
    	//   2002 value: 15.09
    	List<GeneticData> expectedGeneticDataList = new ArrayList<GeneticData>();
        expectedGeneticDataList.add(getSimpleFlatGeneticDataItem("SAMPLE_1", "1001", "0.2"));
        expectedGeneticDataList.add(getSimpleFlatGeneticDataItem("SAMPLE_2", "1001", "34.99"));
        expectedGeneticDataList.add(getSimpleFlatGeneticDataItem("SAMPLE_1", "2002", "0.89"));
        expectedGeneticDataList.add(getSimpleFlatGeneticDataItem("SAMPLE_2", "2002", "15.09"));

        Assert.assertEquals(expectedGeneticDataList, result);
        
        //test paging:
    	result = geneticDataService.getAllGeneticDataInGeneticProfile(geneticProfileStableId,  PROJECTION,  2, PAGE_NUMBER);
    	//expect 2 items:
    	Assert.assertEquals(2, result.size());
    	
    	//test samples/fetch:
    	result = geneticDataService.fetchGeneticDataInGeneticProfile(geneticProfileStableId, EntityType.GENE, Arrays.asList("1"), Arrays.asList("SAMPLE_1"),
    			PROJECTION,  PAGE_SIZE, PAGE_NUMBER);
    	//expect 2 items:
    	Assert.assertEquals(2, result.size());
    	//null test
    	result = geneticDataService.fetchGeneticDataInGeneticProfile(geneticProfileStableId, EntityType.GENE, Arrays.asList("1"), Arrays.asList("SAMPLE_3"),
    			PROJECTION,  PAGE_SIZE, PAGE_NUMBER);
    	//expect 0 items:
    	Assert.assertEquals(0, result.size());
    }

    private GeneticData getSimpleFlatGeneticDataItem(String sampleStableId, String entityStableId, String value){
    	GeneticData item = new GeneticData();

    	GeneticEntity geneticEntity = geneticEntityRepository.getGeneticEntity(entityStableId, GeneticEntity.EntityType.GENE);
    	item.setGeneticEntityId(geneticEntity.getEntityId());
    	item.setGeneticEntityStableId(entityStableId);
    	
    	GeneticProfile geneticProfile = geneticProfileRepository.getGeneticProfile(geneticProfileStableId);
    	item.setGeneticProfileId(geneticProfile.getGeneticProfileId());
    	item.setGeneticProfileStableId(geneticProfile.getStableId());
    	
    	Sample sample = sampleRepository.getSampleInStudy(studyId, sampleStableId); //TODO stub this one
    	item.setSampleId(sample.getInternalId());
    	item.setSampleStableId(sampleStableId);
    	
    	item.setValue(value);
    	
    	return item;
    }

}