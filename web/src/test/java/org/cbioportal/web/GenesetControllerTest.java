package org.cbioportal.web;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.Geneset;
import org.cbioportal.service.GenesetService;
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
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class GenesetControllerTest {
    public static final String GENESET_ID_1 = "geneset_id_1";
    private static final Integer INTERNAL_ID_1 = 1;
    private static final String DESCRIPTION_1 = "description 1";
    private static final String REF_LINK_1 = "http://link1";
    public static final String GENESET_ID_2 = "geneset_id_2";
    private static final Integer INTERNAL_ID_2 = 2;
    private static final String DESCRIPTION_2 = "description 2";
    private static final String REF_LINK_2 = "http://link2";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private GenesetService genesetService;

    private MockMvc mockMvc;

    @Bean
    public GenesetService genesetService() {
        return Mockito.mock(GenesetService.class);
    }

    @Before
    public void setUp() throws Exception {
        Mockito.reset(genesetService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getAllGenesets() throws Exception {
        List<Geneset> genesetList = createGenesetList();
        Mockito
            .when(
                genesetService.getAllGenesets(
                    Mockito.anyString(),
                    Mockito.anyInt(),
                    Mockito.anyInt()
                )
            )
            .thenReturn(genesetList);

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/genesets")
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(
                MockMvcResultMatchers
                    .content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist()
            )
            .andExpect(
                MockMvcResultMatchers
                    .jsonPath("$[0].representativeScore")
                    .doesNotExist()
            )
            .andExpect(
                MockMvcResultMatchers
                    .jsonPath("$[0].genesetId")
                    .value(GENESET_ID_1)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$[0].name").value(GENESET_ID_1)
            )
            .andExpect(
                MockMvcResultMatchers
                    .jsonPath("$[0].description")
                    .value(DESCRIPTION_1)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$[0].refLink").value(REF_LINK_1)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist()
            )
            .andExpect(
                MockMvcResultMatchers
                    .jsonPath("$[1].representativeScore")
                    .doesNotExist()
            )
            .andExpect(
                MockMvcResultMatchers
                    .jsonPath("$[1].genesetId")
                    .value(GENESET_ID_2)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$[1].name").value(GENESET_ID_2)
            )
            .andExpect(
                MockMvcResultMatchers
                    .jsonPath("$[1].description")
                    .value(DESCRIPTION_2)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$[1].refLink").value(REF_LINK_2)
            );
    }

    @Test
    public void getGeneset() throws Exception {
        Geneset geneset = createGenesetList().get(0);
        Mockito
            .when(genesetService.getGeneset(Mockito.anyString()))
            .thenReturn(geneset);

        //test /genesets/{genesetId}
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/genesets/test_geneset_id")
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(
                MockMvcResultMatchers
                    .content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.internalId").doesNotExist()
            )
            .andExpect(
                MockMvcResultMatchers
                    .jsonPath("$.genesetId")
                    .value(GENESET_ID_1)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value(GENESET_ID_1)
            )
            .andExpect(
                MockMvcResultMatchers
                    .jsonPath("$.description")
                    .value(DESCRIPTION_1)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.refLink").value(REF_LINK_1)
            );
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
