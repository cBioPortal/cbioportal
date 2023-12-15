package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetHierarchyInfo;
import org.cbioportal.service.GenesetHierarchyService;
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
@ContextConfiguration(classes = {GenesetHierarchyController.class, TestConfig.class})
public class GenesetHierarchyControllerTest {

    private static final String PROF_ID = "test_prof_id";
    public static final String GENESET_ID1 = "geneset_id1";
    public static final String GENESET_ID2 = "geneset_id2";

    @MockBean
    private GenesetHierarchyService genesetHierarchyService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void fetchGenesetHierarchyInfo() throws Exception {
        List<GenesetHierarchyInfo> genesetHierarchyInfoList = createGenesetHierarchyInfoList();
        Mockito.when(genesetHierarchyService.fetchGenesetHierarchyInfo(Mockito.anyString(), Mockito.anyInt(),
            Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(genesetHierarchyInfoList);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/geneset-hierarchy/fetch").with(csrf())
                .param("geneticProfileId", PROF_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].nodeId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].nodeName").value("Root node"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].parentNodeName").doesNotExist())

                .andExpect(MockMvcResultMatchers.jsonPath("$[1].nodeId").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].nodeName").value("sub node A"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].parentNodeName").value("Root node"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].genesets").doesNotExist())

                .andExpect(MockMvcResultMatchers.jsonPath("$[2].nodeId").value(4))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].nodeName").value("parent node 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].parentNodeName").value("sub node A"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].genesets", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].genesets[0].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].genesets[0].genesetId").value(GENESET_ID1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].genesets[0].representativeScore").value(0.1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].genesets[0].representativePvalue").value(0.054))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].genesets[1].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].genesets[1].genesetId").value(GENESET_ID2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].genesets[1].representativeScore").value(0.8))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].genesets[1].representativePvalue").value(0.04))

                .andExpect(MockMvcResultMatchers.jsonPath("$[3].nodeId").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].nodeName").value("parent node 2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].parentNodeName").value("sub node A"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].genesets", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].genesets[0].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].genesets[0].genesetId").value(GENESET_ID2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].genesets[0].representativeScore").value(0.8))
                .andExpect(MockMvcResultMatchers.jsonPath("$[3].genesets[0].representativePvalue").value(0.04));
    }

    private List<GenesetHierarchyInfo> createGenesetHierarchyInfoList() {

        List<GenesetHierarchyInfo> genesetHierarchyInfoList = new ArrayList<>();

        //hierarchy nodes, parents and genesets:
        List<GenesetHierarchyInfo> hierarchySuperNodes = new ArrayList<GenesetHierarchyInfo>();
        GenesetHierarchyInfo node1 = new GenesetHierarchyInfo();
        node1.setNodeId(1);
        node1.setNodeName("Root node");
        hierarchySuperNodes.add(node1);
        GenesetHierarchyInfo node2 = new GenesetHierarchyInfo();
        node2.setNodeId(2);
        node2.setNodeName("sub node A");
        node2.setParentId(1);
        node2.setParentNodeName(node1.getNodeName());
        hierarchySuperNodes.add(node2);

        Geneset geneset1 = new Geneset();
        geneset1.setGenesetId(GENESET_ID1);
        geneset1.setDescription(GENESET_ID1);
        geneset1.setName(GENESET_ID1);
        geneset1.setRepresentativeScore(0.1);
        geneset1.setRepresentativePvalue(0.054);
        Geneset geneset2 = new Geneset();
        geneset2.setGenesetId(GENESET_ID2);
        geneset2.setDescription(GENESET_ID2);
        geneset2.setName(GENESET_ID2);
        geneset2.setRepresentativeScore(0.8);
        geneset2.setRepresentativePvalue(0.04);

        List<GenesetHierarchyInfo> hierarchyParents = new ArrayList<GenesetHierarchyInfo>();
        GenesetHierarchyInfo parentNode1 = new GenesetHierarchyInfo();
        parentNode1.setNodeId(4);
        parentNode1.setNodeName("parent node 1");
        parentNode1.setParentId(2);
        parentNode1.setParentNodeName(node2.getNodeName());
        parentNode1.setGenesets(Arrays.asList(geneset1, geneset2));
        hierarchyParents.add(parentNode1);
        GenesetHierarchyInfo parentNode2 = new GenesetHierarchyInfo();
        parentNode2.setNodeId(5);
        parentNode2.setNodeName("parent node 2");
        parentNode2.setParentId(2);
        parentNode2.setParentNodeName(node2.getNodeName());
        parentNode2.setGenesets(Arrays.asList(geneset2));
        hierarchyParents.add(parentNode2);

        genesetHierarchyInfoList.addAll(hierarchySuperNodes);
        genesetHierarchyInfoList.addAll(hierarchyParents);
        return genesetHierarchyInfoList;
    }

}
