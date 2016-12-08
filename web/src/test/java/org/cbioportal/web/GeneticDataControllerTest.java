package org.cbioportal.web;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneticData;
import org.cbioportal.service.GeneticDataService;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web.xml")
@Configuration
public class GeneticDataControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private GeneticDataService geneticDataService;
    private MockMvc mockMvc;

    @Bean
    public GeneticDataService geneticDataService() {
        return Mockito.mock(GeneticDataService.class);
    }

    //test data
    private int geneticProfileId = 1;
    private String geneticProfileStableId = "acc_tcga_mrna";
    
    @Before
    public void setUp() throws Exception {

        Mockito.reset(geneticDataService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void  getAllGenesDefaultProjection() throws Exception {

    	List<GeneticData> geneticDataList = createGeneticDataList();

        Mockito.when(geneticDataService.getAllGeneticDataInGeneticProfile(Mockito.anyString(), 
        		Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(geneticDataList);

        mockMvc.perform(MockMvcRequestBuilders.get("/genetic-profiles/"+ geneticProfileStableId + "/genetic-data")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticEntityId").value("1001")); 

    }

	private List<GeneticData> createGeneticDataList() {
		List<GeneticData> expectedGeneticDataList = new ArrayList<GeneticData>();
        expectedGeneticDataList.add(getSimpleFlatGeneticDataItem(1, "SAMPLE_1", 1001, 1, "0.2"));
        expectedGeneticDataList.add(getSimpleFlatGeneticDataItem(2, "SAMPLE_2", 1001, 1, "34.99"));
        expectedGeneticDataList.add(getSimpleFlatGeneticDataItem(1, "SAMPLE_1", 2002, 2, "0.89"));
        expectedGeneticDataList.add(getSimpleFlatGeneticDataItem(2, "SAMPLE_2", 2002, 2, "15.09"));
        
		return expectedGeneticDataList;
	}

	private GeneticData getSimpleFlatGeneticDataItem( 
			int sampleId, String sampleStableId, 
			int entrezGeneId, int entityId,
			String value){
		GeneticData item = new GeneticData();
	
		item.setGeneticEntityId(entityId);
		item.setGeneticEntityStableId(entrezGeneId+"");
		
		item.setGeneticProfileId(geneticProfileId);
		item.setGeneticProfileStableId(geneticProfileStableId);
		
		item.setSampleId(sampleId);
		item.setSampleStableId(sampleStableId);
		
		item.setValue(value);
		
		return item;
	}
    

}
