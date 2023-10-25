package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.CopyNumberCount;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.Gene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.CopyNumberCountIdentifier;
import org.cbioportal.web.parameter.DiscreteCopyNumberEventType;
import org.cbioportal.web.parameter.DiscreteCopyNumberFilter;
import org.cbioportal.web.parameter.HeaderKeyConstants;
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
@ContextConfiguration(classes = {DiscreteCopyNumberController.class, TestConfig.class})
public class DiscreteCopyNumberControllerTest {

    private static final String TEST_MOLECULAR_PROFILE_STABLE_ID_1 = "test_molecular_profile_stable_id_1";
    private static final String TEST_SAMPLE_STABLE_ID_1 = "test_sample_stable_id_1";
    private static final int TEST_ENTREZ_GENE_ID_1 = 1;
    private static final int TEST_ALTERATION_1 = 1;
    private static final String TEST_ANNOTATION_JSON_1 = "{\"columnName\":{\"fieldName\":\"fieldValue\"}}";
    private static final String TEST_HUGO_GENE_SYMBOL_1 = "test_hugo_gene_symbol_1";
    private static final String TEST_TYPE_1 = "test_type_1";
    private static final String TEST_CYTOBAND_1 = "test_cytoband_1";
    private static final String TEST_CHROMOSOME_1 = "test_chromosome_1";
    private static final String TEST_MOLECULAR_PROFILE_STABLE_ID_2 = "test_molecular_profile_stable_id_2";
    private static final String TEST_SAMPLE_STABLE_ID_2 = "test_sample_stable_id_2";
    private static final int TEST_ENTREZ_GENE_ID_2 = 2;
    private static final int TEST_ALTERATION_2 = 2;
    private static final String TEST_HUGO_GENE_SYMBOL_2 = "test_hugo_gene_symbol_2";
    private static final String TEST_TYPE_2 = "test_type_2";
    private static final String TEST_CYTOBAND_2 = "test_cytoband_2";
    private static final String TEST_CHROMOSOME_2 = "test_chromosome_2";
    private static final String TEST_SAMPLE_LIST_ID = "test_sample_list_id";
    private static final int TEST_NUMBER_OF_SAMPLES_1 = 6;
    private static final int TEST_NUMBER_OF_SAMPLES_WITH_ALTERATION_IN_GENE_1 = 4;
    private static final int TEST_NUMBER_OF_SAMPLES_2 = 10;
    private static final int TEST_NUMBER_OF_SAMPLES_WITH_ALTERATION_IN_GENE_2 = 8;

    @MockBean
    private DiscreteCopyNumberService discreteCopyNumberService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void getDiscreteCopyNumbersInMolecularProfileBySampleListIdDefaultProjection() throws Exception {

        List<DiscreteCopyNumberData> discreteCopyNumberDataList = createExampleDiscreteCopyNumberData();

        Mockito.when(discreteCopyNumberService.getDiscreteCopyNumbersInMolecularProfileBySampleListId(
            Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any())).thenReturn(discreteCopyNumberDataList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/molecular-profiles/test_molecular_profile_id/discrete-copy-number")
            .param("sampleListId", TEST_SAMPLE_LIST_ID)
            .param("discreteCopyNumberEventType", DiscreteCopyNumberEventType.HOMDEL_AND_AMP.name())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alteration").value(TEST_ALTERATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].namespaceColumns.columnName.fieldName").value("fieldValue"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alteration").value(TEST_ALTERATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene").doesNotExist());
    }

    @Test
    @WithMockUser
    public void getDiscreteCopyNumbersWithoutAnnotationJson() throws Exception {

        List<DiscreteCopyNumberData> discreteCopyNumberDataList = createExampleDiscreteCopyNumberData();
        discreteCopyNumberDataList.get(0).setAnnotationJson(null);
        Mockito.when(discreteCopyNumberService.getDiscreteCopyNumbersInMolecularProfileBySampleListId(
            Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any())).thenReturn(discreteCopyNumberDataList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/molecular-profiles/test_molecular_profile_id/discrete-copy-number")
            .param("sampleListId", TEST_SAMPLE_LIST_ID)
            .param("discreteCopyNumberEventType", DiscreteCopyNumberEventType.HOMDEL_AND_AMP.name())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].namespaceColumns").doesNotExist());
    }

    @Test
    @WithMockUser
    public void getDiscreteCopyNumbersInMolecularProfileBySampleListIdDetailedProjection() throws Exception {

        List<DiscreteCopyNumberData> discreteCopyNumberDataList = createExampleDiscreteCopyNumberDataWithGenes();

        Mockito.when(discreteCopyNumberService.getDiscreteCopyNumbersInMolecularProfileBySampleListId(
            Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any())).thenReturn(discreteCopyNumberDataList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/molecular-profiles/test_molecular_profile_id/discrete-copy-number")
            .param("sampleListId", TEST_SAMPLE_LIST_ID)
            .param("discreteCopyNumberEventType", DiscreteCopyNumberEventType.HOMDEL_AND_AMP.name())
            .param("projection", "DETAILED")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alteration").value(TEST_ALTERATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.type").value(TEST_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alteration").value(TEST_ALTERATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.type").value(TEST_TYPE_2));
    }

    @Test
    @WithMockUser
    public void getDiscreteCopyNumbersInMolecularProfileBySampleListIdMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(discreteCopyNumberService.getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(
            Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/molecular-profiles/test_molecular_profile_id/discrete-copy-number")
            .param("sampleListId", TEST_SAMPLE_LIST_ID)
            .param("discreteCopyNumberEventType", DiscreteCopyNumberEventType.HOMDEL_AND_AMP.name())
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    @WithMockUser
    public void fetchDiscreteCopyNumbersInMolecularProfileDefaultProjection() throws Exception {

        List<DiscreteCopyNumberData> discreteCopyNumberDataList = createExampleDiscreteCopyNumberData();

        Mockito.when(discreteCopyNumberService.fetchDiscreteCopyNumbersInMolecularProfile(Mockito.anyString(),
            Mockito.anyList(), Mockito.anyList(), Mockito.anyList(),
            Mockito.anyString())).thenReturn(discreteCopyNumberDataList);

        DiscreteCopyNumberFilter discreteCopyNumberFilter = createDiscreteCopyNumberFilter();

        mockMvc.perform(MockMvcRequestBuilders
            .post("/api/molecular-profiles/test_molecular_profile_id/discrete-copy-number/fetch").with(csrf())
            .param("discreteCopyNumberEventType", DiscreteCopyNumberEventType.HOMDEL_AND_AMP.name())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(discreteCopyNumberFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value(TEST_MOLECULAR_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alteration").value(TEST_ALTERATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId").value(TEST_MOLECULAR_PROFILE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alteration").value(TEST_ALTERATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene").doesNotExist());
    }

    @Test
    @WithMockUser
    public void fetchDiscreteCopyNumbersInMolecularProfileDetailedProjection() throws Exception {

        List<DiscreteCopyNumberData> discreteCopyNumberDataList = createExampleDiscreteCopyNumberDataWithGenes();

        Mockito.when(discreteCopyNumberService.fetchDiscreteCopyNumbersInMolecularProfile(Mockito.anyString(),
            Mockito.anyList(), Mockito.anyList(), Mockito.anyList(),
            Mockito.anyString()))
            .thenReturn(discreteCopyNumberDataList);

        DiscreteCopyNumberFilter discreteCopyNumberFilter = createDiscreteCopyNumberFilter();

        mockMvc.perform(MockMvcRequestBuilders
            .post("/api/molecular-profiles/test_molecular_profile_id/discrete-copy-number/fetch").with(csrf())
            .param("discreteCopyNumberEventType", DiscreteCopyNumberEventType.HOMDEL_AND_AMP.name())
            .param("projection", "DETAILED")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(discreteCopyNumberFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alteration").value(TEST_ALTERATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.type").value(TEST_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alteration").value(TEST_ALTERATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.type").value(TEST_TYPE_2));
    }

    @Test
    @WithMockUser
    public void fetchDiscreteCopyNumbersInMolecularProfileMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(discreteCopyNumberService.fetchMetaDiscreteCopyNumbersInMolecularProfile(Mockito.anyString(),
            Mockito.anyList(), Mockito.anyList(), Mockito.anyList()))
            .thenReturn(baseMeta);

        DiscreteCopyNumberFilter discreteCopyNumberFilter = createDiscreteCopyNumberFilter();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/molecular-profiles/test_molecular_profile_id/discrete-copy-number/fetch").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(discreteCopyNumberFilter))
            .param("discreteCopyNumberEventType", DiscreteCopyNumberEventType.HOMDEL_AND_AMP.name())
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    private DiscreteCopyNumberFilter createDiscreteCopyNumberFilter() {

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(TEST_SAMPLE_STABLE_ID_1);
        sampleIds.add(TEST_SAMPLE_STABLE_ID_2);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(TEST_ENTREZ_GENE_ID_1);
        entrezGeneIds.add(TEST_ENTREZ_GENE_ID_2);

        DiscreteCopyNumberFilter discreteCopyNumberFilter = new DiscreteCopyNumberFilter();
        discreteCopyNumberFilter.setEntrezGeneIds(entrezGeneIds);
        discreteCopyNumberFilter.setSampleIds(sampleIds);
        return discreteCopyNumberFilter;
    }

    private List<DiscreteCopyNumberData> createExampleDiscreteCopyNumberData() {

        List<DiscreteCopyNumberData> discreteCopyNumberDataList = new ArrayList<>();
        DiscreteCopyNumberData discreteCopyNumberData1 = new DiscreteCopyNumberData();
        discreteCopyNumberData1.setMolecularProfileId(TEST_MOLECULAR_PROFILE_STABLE_ID_1);
        discreteCopyNumberData1.setSampleId(TEST_SAMPLE_STABLE_ID_1);
        discreteCopyNumberData1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        discreteCopyNumberData1.setAlteration(TEST_ALTERATION_1);
        discreteCopyNumberDataList.add(discreteCopyNumberData1);
        discreteCopyNumberData1.setAnnotationJson(TEST_ANNOTATION_JSON_1);
        DiscreteCopyNumberData discreteCopyNumberData2 = new DiscreteCopyNumberData();
        discreteCopyNumberData2.setMolecularProfileId(TEST_MOLECULAR_PROFILE_STABLE_ID_2);
        discreteCopyNumberData2.setSampleId(TEST_SAMPLE_STABLE_ID_2);
        discreteCopyNumberData2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        discreteCopyNumberData2.setAlteration(TEST_ALTERATION_2);
        discreteCopyNumberDataList.add(discreteCopyNumberData2);
        return discreteCopyNumberDataList;
    }

    private List<DiscreteCopyNumberData> createExampleDiscreteCopyNumberDataWithGenes() {

        List<DiscreteCopyNumberData> discreteCopyNumberDataList = createExampleDiscreteCopyNumberData();
        Gene gene1 = new Gene();
        gene1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        gene1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        gene1.setType(TEST_TYPE_1);
        discreteCopyNumberDataList.get(0).setGene(gene1);
        Gene gene2 = new Gene();
        gene2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        gene2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        gene2.setType(TEST_TYPE_2);
        discreteCopyNumberDataList.get(1).setGene(gene2);
        return discreteCopyNumberDataList;
    }
}
