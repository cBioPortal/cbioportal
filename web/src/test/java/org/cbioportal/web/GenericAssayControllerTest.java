package org.cbioportal.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.cbioportal.model.GenericAssayData;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.service.GenericAssayService;
import org.cbioportal.web.parameter.GenericAssayDataFilter;
import org.cbioportal.web.parameter.GenericAssayDataMultipleStudyFilter;
import org.cbioportal.web.parameter.GenericAssayMetaFilter;
import org.cbioportal.web.parameter.SampleMolecularIdentifier;
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
public class GenericAssayControllerTest {

    private static final String PROF_ID = "test_prof_id";
    private static final String ENTITY_TYPE = "test_type";
    public static final String GENERIC_ASSAY_STABLE_ID_1 = "genericAssayStableId1";
    public static final String GENERIC_ASSAY_STABLE_ID_2 = "genericAssayStableId2";
    private static final String SAMPLE_ID = "test_sample_stable_id_1";
    private static final String VALUE_1 = "0.25";
    private static final String VALUE_2 = "-0.75";
    private static final String TEST_NAME = "name";
    private static final String TEST_NAME_VALUE = "test_name";
    private static final String TEST_DESCRIPTION = "description";
    private static final String TEST_DESCRIPTION_VALUE = "test_description";
    private static final HashMap<String, String> GENERIC_ENTITY_META_PROPERTIES = new HashMap<String, String>() {{
        put(TEST_NAME,TEST_NAME_VALUE);
        put(TEST_DESCRIPTION,TEST_DESCRIPTION_VALUE);
    }};

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private GenericAssayService genericAssayService;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public GenericAssayService genericAssayService() {
        return Mockito.mock(GenericAssayService.class);
    }
    
    @Before
    public void setUp() throws Exception {

        Mockito.reset(genericAssayService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test	
    public void testGenericAssayDataFetch() throws Exception {	
        List<GenericAssayData> genericAssayDataItems = createGenericAssayDataItemsList();	
        Mockito.when(genericAssayService.fetchGenericAssayData(Mockito.anyString(), Mockito.anyList(),	
            Mockito.anyList(), Mockito.anyString())).thenReturn(genericAssayDataItems);	

        GenericAssayDataFilter genericAssayDataFilter = new GenericAssayDataFilter();	
        genericAssayDataFilter.setSampleIds(Arrays.asList(SAMPLE_ID));	
        genericAssayDataFilter.setGenericAssayStableIds(Arrays.asList(GENERIC_ASSAY_STABLE_ID_1, GENERIC_ASSAY_STABLE_ID_2));	

        mockMvc.perform(MockMvcRequestBuilders.post("/generic_assay_data/" + PROF_ID + "/fetch")	
                .accept(MediaType.APPLICATION_JSON)	
                .contentType(MediaType.APPLICATION_JSON)	
                .content(objectMapper.writeValueAsString(genericAssayDataFilter)))	
                .andExpect(MockMvcResultMatchers.status().isOk())	
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))	
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))	
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value(PROF_ID))	
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].genericAssayStableId").value(GENERIC_ASSAY_STABLE_ID_1))	
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(SAMPLE_ID))	
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(VALUE_1))	
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId").value(PROF_ID))	
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].genericAssayStableId").value(GENERIC_ASSAY_STABLE_ID_2))	
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(SAMPLE_ID))	
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(VALUE_2));	
    }

    @Test
    public void testGenericAssayDataFetchInMultipleMolecularProfiles() throws Exception {
        List<GenericAssayData> genericAssayDataItems = createGenericAssayDataItemsList();
        GenericAssayDataMultipleStudyFilter genericAssayDataMultipleStudyFilter = new GenericAssayDataMultipleStudyFilter();
        genericAssayDataMultipleStudyFilter.setSampleMolecularIdentifiers(createSampleMolecularIdentifiers());
        
        Mockito.when(genericAssayService.fetchGenericAssayData(Mockito.anyList(), Mockito.any(),
            Mockito.any(), Mockito.any())).thenReturn(genericAssayDataItems);

        mockMvc.perform(MockMvcRequestBuilders.post("/generic_assay_data/fetch")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(genericAssayDataMultipleStudyFilter)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value(PROF_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].genericAssayStableId").value(GENERIC_ASSAY_STABLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(SAMPLE_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(VALUE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId").value(PROF_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].genericAssayStableId").value(GENERIC_ASSAY_STABLE_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(SAMPLE_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(VALUE_2));        
    }

    @Test
    public void testGenericAssayMetaDataFetch() throws Exception {
        List<GenericAssayMeta> genericAssayMetaItems = createGenericAssayMetaItemsList();
        List<String> genericAssayStableIds = Arrays.asList(GENERIC_ASSAY_STABLE_ID_1, GENERIC_ASSAY_STABLE_ID_2);
        GenericAssayMetaFilter genericAssayMetaFilter = new GenericAssayMetaFilter();
        genericAssayMetaFilter.setGenericAssayStableIds(genericAssayStableIds);
        
        Mockito.when(genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(genericAssayMetaItems);

        mockMvc.perform(MockMvcRequestBuilders.post("/generic_assay_meta/fetch")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(genericAssayMetaFilter)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].entityType").value(ENTITY_TYPE))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").value(GENERIC_ASSAY_STABLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].entityType").value(ENTITY_TYPE))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].stableId").value(GENERIC_ASSAY_STABLE_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].genericEntityMetaProperties", Matchers.hasKey(TEST_NAME)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].genericEntityMetaProperties", Matchers.hasValue(TEST_NAME_VALUE)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].genericEntityMetaProperties", Matchers.hasKey(TEST_DESCRIPTION)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].genericEntityMetaProperties", Matchers.hasValue(TEST_DESCRIPTION_VALUE)));      
    }

    private List<GenericAssayMeta> createGenericAssayMetaItemsList() {

        List<GenericAssayMeta> genericAssayMetaItems = new ArrayList<>();

        GenericAssayMeta item1 = new GenericAssayMeta(ENTITY_TYPE, GENERIC_ASSAY_STABLE_ID_1);
        genericAssayMetaItems.add(item1);

        GenericAssayMeta item2 = new GenericAssayMeta(ENTITY_TYPE, GENERIC_ASSAY_STABLE_ID_2, GENERIC_ENTITY_META_PROPERTIES);
        genericAssayMetaItems.add(item2);

        return genericAssayMetaItems;
    }

    private List<GenericAssayData> createGenericAssayDataItemsList() {

        List<GenericAssayData> genericAssayDataItems = new ArrayList<>();

        GenericAssayData item1 = new GenericAssayData();
        item1.setGenericAssayStableId(GENERIC_ASSAY_STABLE_ID_1);
        item1.setMolecularProfileId(PROF_ID);
        item1.setSampleId(SAMPLE_ID);
        item1.setValue(VALUE_1);
        genericAssayDataItems.add(item1);

        GenericAssayData item2 = new GenericAssayData();
        item2.setGenericAssayStableId(GENERIC_ASSAY_STABLE_ID_2);
        item2.setMolecularProfileId(PROF_ID);
        item2.setSampleId(SAMPLE_ID);
        item2.setValue(VALUE_2);
        genericAssayDataItems.add(item2);

        return genericAssayDataItems;
    }

    private List<SampleMolecularIdentifier> createSampleMolecularIdentifiers() {

        List<SampleMolecularIdentifier> sampleMolecularIdentifiers = new ArrayList<>();

        SampleMolecularIdentifier identifier1 = new SampleMolecularIdentifier();
        identifier1.setSampleId(SAMPLE_ID);
        identifier1.setMolecularProfileId(GENERIC_ASSAY_STABLE_ID_1);
        sampleMolecularIdentifiers.add(identifier1);

        SampleMolecularIdentifier identifier2 = new SampleMolecularIdentifier();
        identifier1.setSampleId(SAMPLE_ID);
        identifier1.setMolecularProfileId(GENERIC_ASSAY_STABLE_ID_2);
        sampleMolecularIdentifiers.add(identifier2);

        return sampleMolecularIdentifiers;
    }

}