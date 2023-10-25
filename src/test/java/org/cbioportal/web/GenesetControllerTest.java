package org.cbioportal.web;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.Geneset;
import org.cbioportal.service.GenesetService;
import org.cbioportal.web.config.TestConfig;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {GenesetController.class, TestConfig.class})
public class GenesetControllerTest {

    public static final String GENESET_ID_1 = "geneset_id_1";
    private static final Integer INTERNAL_ID_1 = 1;
    private static final String DESCRIPTION_1 = "description 1";
    private static final String REF_LINK_1 = "http://link1";
    public static final String GENESET_ID_2 = "geneset_id_2";
    private static final Integer INTERNAL_ID_2 = 2;
    private static final String DESCRIPTION_2 = "description 2";
    private static final String REF_LINK_2 = "http://link2";

    @MockBean
    private GenesetService genesetService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void getAllGenesets() throws Exception {

        List<Geneset> genesetList = createGenesetList();
        Mockito.when(genesetService.getAllGenesets(Mockito.anyString(), Mockito.anyInt(),
            Mockito.anyInt())).thenReturn(genesetList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/genesets")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].representativeScore").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].genesetId").value(GENESET_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(GENESET_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].refLink").value(REF_LINK_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].representativeScore").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].genesetId").value(GENESET_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(GENESET_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(DESCRIPTION_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].refLink").value(REF_LINK_2));
    }

    @Test
    @WithMockUser
    public void getGeneset() throws Exception {

        Geneset geneset = createGenesetList().get(0);
        Mockito.when(genesetService.getGeneset(Mockito.anyString())).thenReturn(geneset);

        //test /genesets/{genesetId}
        mockMvc.perform(MockMvcRequestBuilders.get("/api/genesets/test_geneset_id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.genesetId").value(GENESET_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(GENESET_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.refLink").value(REF_LINK_1));
    }

    private List<Geneset> createGenesetList() {
        List<Geneset> genesetList = new ArrayList<>();
        Geneset geneset1 = new Geneset();
        geneset1.setInternalId(INTERNAL_ID_1);
        geneset1.setGenesetId(GENESET_ID_1);
        geneset1.setName(GENESET_ID_1);
        geneset1.setDescription(DESCRIPTION_1);
        geneset1.setRefLink(REF_LINK_1);
        genesetList.add(geneset1);
        Geneset geneset2 = new Geneset();
        geneset2.setInternalId(INTERNAL_ID_2);
        geneset2.setGenesetId(GENESET_ID_2);
        geneset2.setName(GENESET_ID_2);
        geneset2.setDescription(DESCRIPTION_2);
        geneset2.setRefLink(REF_LINK_2);
        genesetList.add(geneset2);
        return genesetList;
    }
}
