package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.Gene;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.service.MutationService;
import org.cbioportal.web.parameter.HeaderKeyConstants;
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

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web.xml")
@Configuration
public class MutationControllerTest {

    private static final int TEST_GENETIC_PROFILE_ID_1 = 1;
    private static final String TEST_GENETIC_PROFILE_STABLE_ID_1 = "test_genetic_profile_stable_id_1";
    private static final int TEST_SAMPLE_ID_1 = 1;
    private static final String TEST_SAMPLE_STABLE_ID_1 = "test_sample_stable_id_1";
    private static final int TEST_ENTREZ_GENE_ID_1 = 1;
    private static final String TEST_CENTER_1 = "test_center_1";
    private static final String TEST_MUTATION_STATUS_1 = "test_mutation_status_1";
    private static final String TEST_VALIDATION_STATUS_1 = "test_validation_status_1";
    private static final int TEST_TUMOR_ALT_COUNT_1 = 1;
    private static final int TEST_TUMOR_REF_COUNT_1 = 1;
    private static final int TEST_NORMAL_ALT_COUNT_1 = 1;
    private static final int TEST_NORMAL_REF_COUNT_1 = 1;
    private static final String TEST_AMINO_ACID_CHANGE_1 = "test_amino_acid_change_1";
    private static final long TEST_START_POSITION_1 = 1L;
    private static final long TEST_END_POSITION_1 = 1L;
    private static final String TEST_REFERENCE_ALLELE_1 = "test_reference_allele_1";
    private static final String TEST_TUMOR_SEQ_ALLELE_1 = "test_tumor_seq_allele_1";
    private static final String TEST_PROTEIN_CHANGE_1 = "test_protein_change_1";
    private static final String TEST_MUTATION_TYPE_1 = "test_mutation_type_1";
    private static final String TEST_NCBI_BUILD_1 = "test_ncbi_build_1";
    private static final String TEST_VARIANT_TYPE_1 = "test_variant_type_1";
    private static final String TEST_ONCOTATOR_REFSEQ_MRNA_ID_1 = "test_oncotator_refseq_mrna_id_1";
    private static final int TEST_ONCOTATOR_PROTEIN_POS_START_1 = 1;
    private static final int TEST_ONCOTATOR_PROTEIN_POS_END_1 = 1;
    private static final String TEST_KEYWORD_1 = "test_keyword_1";
    private static final String TEST_HUGO_GENE_SYMBOL_1 = "test_hugo_gene_symbol_1";
    private static final String TEST_TYPE_1 = "test_type_1";
    private static final String TEST_CYTOBAND_1 = "test_cytoband_1";
    private static final int TEST_LENGTH_1 = 100;
    private static final String TEST_CHROMOSOME_1 = "test_chromosome_1";
    private static final int TEST_GENETIC_PROFILE_ID_2 = 2;
    private static final String TEST_GENETIC_PROFILE_STABLE_ID_2 = "test_genetic_profile_stable_id_2";
    private static final int TEST_SAMPLE_ID_2 = 2;
    private static final String TEST_SAMPLE_STABLE_ID_2 = "test_sample_stable_id_2";
    private static final int TEST_ENTREZ_GENE_ID_2 = 2;
    private static final String TEST_CENTER_2 = "test_center_2";
    private static final String TEST_MUTATION_STATUS_2 = "test_mutation_status_2";
    private static final String TEST_VALIDATION_STATUS_2 = "test_validation_status_2";
    private static final int TEST_TUMOR_ALT_COUNT_2 = 2;
    private static final int TEST_TUMOR_REF_COUNT_2 = 2;
    private static final int TEST_NORMAL_ALT_COUNT_2 = 2;
    private static final int TEST_NORMAL_REF_COUNT_2 = 2;
    private static final String TEST_AMINO_ACID_CHANGE_2 = "test_amino_acid_change_2";
    private static final long TEST_START_POSITION_2 = 2L;
    private static final long TEST_END_POSITION_2 = 2L;
    private static final String TEST_REFERENCE_ALLELE_2 = "test_reference_allele_2";
    private static final String TEST_TUMOR_SEQ_ALLELE_2 = "test_tumor_seq_allele_2";
    private static final String TEST_PROTEIN_CHANGE_2 = "test_protein_change_2";
    private static final String TEST_MUTATION_TYPE_2 = "test_mutation_type_2";
    private static final String TEST_NCBI_BUILD_2 = "test_ncbi_build_2";
    private static final String TEST_VARIANT_TYPE_2 = "test_variant_type_2";
    private static final String TEST_ONCOTATOR_REFSEQ_MRNA_ID_2 = "test_oncotator_refseq_mrna_id_2";
    private static final int TEST_ONCOTATOR_PROTEIN_POS_START_2 = 2;
    private static final int TEST_ONCOTATOR_PROTEIN_POS_END_2 = 2;
    private static final String TEST_KEYWORD_2 = "test_keyword_2";
    private static final String TEST_HUGO_GENE_SYMBOL_2 = "test_hugo_gene_symbol_2";
    private static final String TEST_TYPE_2 = "test_type_2";
    private static final String TEST_CYTOBAND_2 = "test_cytoband_2";
    private static final int TEST_LENGTH_2 = 200;
    private static final String TEST_CHROMOSOME_2 = "test_chromosome_2";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MutationService mutationService;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public MutationService mutationService() {
        return Mockito.mock(MutationService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(mutationService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getMutationsInGeneticProfileDefaultProjection() throws Exception {

        List<Mutation> mutationList = createExampleMutations();
        
        Mockito.when(mutationService.getMutationsInGeneticProfile(Mockito.anyString(), Mockito.anyString(), 
            Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(mutationList);

        mockMvc.perform(MockMvcRequestBuilders.get("/genetic-profiles/test_genetic_profile_id/mutations")
            .param("sampleId", TEST_SAMPLE_STABLE_ID_1)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].center").value(TEST_CENTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutationStatus").value(TEST_MUTATION_STATUS_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].validationStatus").value(TEST_VALIDATION_STATUS_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorAltCount").value(TEST_TUMOR_ALT_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorRefCount").value(TEST_TUMOR_REF_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalAltCount").value(TEST_NORMAL_ALT_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalRefCount").value(TEST_NORMAL_REF_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].aminoAcidChange").value(TEST_AMINO_ACID_CHANGE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].startPosition").value((int) TEST_START_POSITION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].endPosition").value((int) TEST_END_POSITION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].referenceAllele").value(TEST_REFERENCE_ALLELE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantAllele").value(TEST_TUMOR_SEQ_ALLELE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinChange").value(TEST_PROTEIN_CHANGE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutationType").value(TEST_MUTATION_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].ncbiBuild").value(TEST_NCBI_BUILD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantType").value(TEST_VARIANT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].refseqMrnaId").value(TEST_ONCOTATOR_REFSEQ_MRNA_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].keyword").value(TEST_KEYWORD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].geneticProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].center").value(TEST_CENTER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutationStatus").value(TEST_MUTATION_STATUS_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].validationStatus").value(TEST_VALIDATION_STATUS_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].tumorAltCount").value(TEST_TUMOR_ALT_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].tumorRefCount").value(TEST_TUMOR_REF_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].normalAltCount").value(TEST_NORMAL_ALT_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].normalRefCount").value(TEST_NORMAL_REF_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].aminoAcidChange").value(TEST_AMINO_ACID_CHANGE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].startPosition").value((int) TEST_START_POSITION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].endPosition").value((int) TEST_END_POSITION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].referenceAllele").value(TEST_REFERENCE_ALLELE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantAllele").value(TEST_TUMOR_SEQ_ALLELE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinChange").value(TEST_PROTEIN_CHANGE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutationType").value(TEST_MUTATION_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].ncbiBuild").value(TEST_NCBI_BUILD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantType").value(TEST_VARIANT_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].refseqMrnaId").value(TEST_ONCOTATOR_REFSEQ_MRNA_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].keyword").value(TEST_KEYWORD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene").doesNotExist());
    }

    @Test
    public void getMutationsInGeneticProfileDetailedProjection() throws Exception {

        List<Mutation> mutationList = createExampleMutationsWithGene();

        Mockito.when(mutationService.getMutationsInGeneticProfile(Mockito.anyString(), Mockito.anyString(),
            Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(mutationList);

        mockMvc.perform(MockMvcRequestBuilders.get("/genetic-profiles/test_genetic_profile_id/mutations")
            .param("sampleId", TEST_SAMPLE_STABLE_ID_1)
            .param("projection", "DETAILED")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].center").value(TEST_CENTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutationStatus").value(TEST_MUTATION_STATUS_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].validationStatus").value(TEST_VALIDATION_STATUS_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorAltCount").value(TEST_TUMOR_ALT_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorRefCount").value(TEST_TUMOR_REF_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalAltCount").value(TEST_NORMAL_ALT_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalRefCount").value(TEST_NORMAL_REF_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].aminoAcidChange").value(TEST_AMINO_ACID_CHANGE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].startPosition").value((int) TEST_START_POSITION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].endPosition").value((int) TEST_END_POSITION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].referenceAllele").value(TEST_REFERENCE_ALLELE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantAllele").value(TEST_TUMOR_SEQ_ALLELE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinChange").value(TEST_PROTEIN_CHANGE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutationType").value(TEST_MUTATION_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].ncbiBuild").value(TEST_NCBI_BUILD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantType").value(TEST_VARIANT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].refseqMrnaId").value(TEST_ONCOTATOR_REFSEQ_MRNA_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].keyword").value(TEST_KEYWORD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.type").value(TEST_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.cytoband").value(TEST_CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.length").value(TEST_LENGTH_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.chromosome").value(TEST_CHROMOSOME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].geneticProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].center").value(TEST_CENTER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutationStatus").value(TEST_MUTATION_STATUS_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].validationStatus").value(TEST_VALIDATION_STATUS_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].tumorAltCount").value(TEST_TUMOR_ALT_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].tumorRefCount").value(TEST_TUMOR_REF_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].normalAltCount").value(TEST_NORMAL_ALT_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].normalRefCount").value(TEST_NORMAL_REF_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].aminoAcidChange").value(TEST_AMINO_ACID_CHANGE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].startPosition").value((int) TEST_START_POSITION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].endPosition").value((int) TEST_END_POSITION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].referenceAllele").value(TEST_REFERENCE_ALLELE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantAllele").value(TEST_TUMOR_SEQ_ALLELE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinChange").value(TEST_PROTEIN_CHANGE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutationType").value(TEST_MUTATION_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].ncbiBuild").value(TEST_NCBI_BUILD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantType").value(TEST_VARIANT_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].refseqMrnaId").value(TEST_ONCOTATOR_REFSEQ_MRNA_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].keyword").value(TEST_KEYWORD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.type").value(TEST_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.cytoband").value(TEST_CYTOBAND_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.length").value(TEST_LENGTH_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.chromosome").value(TEST_CHROMOSOME_2));
    }

    @Test
    public void getMutationsInGeneticProfileMetaProjection() throws Exception {

        MutationMeta mutationMeta = new MutationMeta();
        mutationMeta.setTotalCount(2);

        Mockito.when(mutationService.getMetaMutationsInGeneticProfile(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(mutationMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/genetic-profiles/test_genetic_profile_id/mutations")
            .param("sampleId", TEST_SAMPLE_STABLE_ID_1)
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void fetchMutationsInGeneticProfileDefaultProjection() throws Exception {

        List<Mutation> mutationList = createExampleMutations();

        Mockito.when(mutationService.fetchMutationsInGeneticProfile(Mockito.anyString(), 
            Mockito.anyListOf(String.class), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), 
            Mockito.anyString(), Mockito.anyString())).thenReturn(mutationList);
        
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(TEST_SAMPLE_STABLE_ID_1);
        sampleIds.add(TEST_SAMPLE_STABLE_ID_2);

        mockMvc.perform(MockMvcRequestBuilders.post("/genetic-profiles/test_genetic_profile_id/mutations/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleIds)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].center").value(TEST_CENTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutationStatus").value(TEST_MUTATION_STATUS_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].validationStatus").value(TEST_VALIDATION_STATUS_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorAltCount").value(TEST_TUMOR_ALT_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorRefCount").value(TEST_TUMOR_REF_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalAltCount").value(TEST_NORMAL_ALT_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalRefCount").value(TEST_NORMAL_REF_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].aminoAcidChange").value(TEST_AMINO_ACID_CHANGE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].startPosition").value((int) TEST_START_POSITION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].endPosition").value((int) TEST_END_POSITION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].referenceAllele").value(TEST_REFERENCE_ALLELE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantAllele").value(TEST_TUMOR_SEQ_ALLELE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinChange").value(TEST_PROTEIN_CHANGE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutationType").value(TEST_MUTATION_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].ncbiBuild").value(TEST_NCBI_BUILD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantType").value(TEST_VARIANT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].refseqMrnaId").value(TEST_ONCOTATOR_REFSEQ_MRNA_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].keyword").value(TEST_KEYWORD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].geneticProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].center").value(TEST_CENTER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutationStatus").value(TEST_MUTATION_STATUS_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].validationStatus").value(TEST_VALIDATION_STATUS_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].tumorAltCount").value(TEST_TUMOR_ALT_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].tumorRefCount").value(TEST_TUMOR_REF_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].normalAltCount").value(TEST_NORMAL_ALT_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].normalRefCount").value(TEST_NORMAL_REF_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].aminoAcidChange").value(TEST_AMINO_ACID_CHANGE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].startPosition").value((int) TEST_START_POSITION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].endPosition").value((int) TEST_END_POSITION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].referenceAllele").value(TEST_REFERENCE_ALLELE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantAllele").value(TEST_TUMOR_SEQ_ALLELE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinChange").value(TEST_PROTEIN_CHANGE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutationType").value(TEST_MUTATION_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].ncbiBuild").value(TEST_NCBI_BUILD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantType").value(TEST_VARIANT_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].refseqMrnaId").value(TEST_ONCOTATOR_REFSEQ_MRNA_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].keyword").value(TEST_KEYWORD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene").doesNotExist());
    }

    @Test
    public void fetchMutationsInGeneticProfileDetailedProjection() throws Exception {

        List<Mutation> mutationList = createExampleMutationsWithGene();

        Mockito.when(mutationService.fetchMutationsInGeneticProfile(Mockito.anyString(),
            Mockito.anyListOf(String.class), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
            Mockito.anyString(), Mockito.anyString())).thenReturn(mutationList);

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(TEST_SAMPLE_STABLE_ID_1);
        sampleIds.add(TEST_SAMPLE_STABLE_ID_2);

        mockMvc.perform(MockMvcRequestBuilders.post("/genetic-profiles/test_genetic_profile_id/mutations/fetch")
            .param("projection", "DETAILED")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleIds)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].center").value(TEST_CENTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutationStatus").value(TEST_MUTATION_STATUS_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].validationStatus").value(TEST_VALIDATION_STATUS_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorAltCount").value(TEST_TUMOR_ALT_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorRefCount").value(TEST_TUMOR_REF_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalAltCount").value(TEST_NORMAL_ALT_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalRefCount").value(TEST_NORMAL_REF_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].aminoAcidChange").value(TEST_AMINO_ACID_CHANGE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].startPosition").value((int) TEST_START_POSITION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].endPosition").value((int) TEST_END_POSITION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].referenceAllele").value(TEST_REFERENCE_ALLELE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantAllele").value(TEST_TUMOR_SEQ_ALLELE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinChange").value(TEST_PROTEIN_CHANGE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutationType").value(TEST_MUTATION_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].ncbiBuild").value(TEST_NCBI_BUILD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantType").value(TEST_VARIANT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].refseqMrnaId").value(TEST_ONCOTATOR_REFSEQ_MRNA_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].keyword").value(TEST_KEYWORD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.type").value(TEST_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.cytoband").value(TEST_CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.length").value(TEST_LENGTH_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.chromosome").value(TEST_CHROMOSOME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].geneticProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].center").value(TEST_CENTER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutationStatus").value(TEST_MUTATION_STATUS_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].validationStatus").value(TEST_VALIDATION_STATUS_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].tumorAltCount").value(TEST_TUMOR_ALT_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].tumorRefCount").value(TEST_TUMOR_REF_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].normalAltCount").value(TEST_NORMAL_ALT_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].normalRefCount").value(TEST_NORMAL_REF_COUNT_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].aminoAcidChange").value(TEST_AMINO_ACID_CHANGE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].startPosition").value((int) TEST_START_POSITION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].endPosition").value((int) TEST_END_POSITION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].referenceAllele").value(TEST_REFERENCE_ALLELE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantAllele").value(TEST_TUMOR_SEQ_ALLELE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinChange").value(TEST_PROTEIN_CHANGE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutationType").value(TEST_MUTATION_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].ncbiBuild").value(TEST_NCBI_BUILD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantType").value(TEST_VARIANT_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].refseqMrnaId").value(TEST_ONCOTATOR_REFSEQ_MRNA_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].keyword").value(TEST_KEYWORD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.type").value(TEST_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.cytoband").value(TEST_CYTOBAND_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.length").value(TEST_LENGTH_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.chromosome").value(TEST_CHROMOSOME_2));
    }

    @Test
    public void fetchMutationsInGeneticProfileMetaProjection() throws Exception {

        MutationMeta mutationMeta = new MutationMeta();
        mutationMeta.setTotalCount(2);

        Mockito.when(mutationService.fetchMetaMutationsInGeneticProfile(Mockito.anyString(), 
            Mockito.anyListOf(String.class))).thenReturn(mutationMeta);

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(TEST_SAMPLE_STABLE_ID_1);
        sampleIds.add(TEST_SAMPLE_STABLE_ID_2);

        mockMvc.perform(MockMvcRequestBuilders.post("/genetic-profiles/test_genetic_profile_id/mutations/fetch")
            .param("projection", "META")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleIds)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    private List<Mutation> createExampleMutations() {

        List<Mutation> mutationList = new ArrayList<>();
        Mutation mutation1 = new Mutation();
        mutation1.setGeneticProfileId(TEST_GENETIC_PROFILE_ID_1);
        mutation1.setGeneticProfileStableId(TEST_GENETIC_PROFILE_STABLE_ID_1);
        mutation1.setSampleId(TEST_SAMPLE_ID_1);
        mutation1.setSampleStableId(TEST_SAMPLE_STABLE_ID_1);
        mutation1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        mutation1.setCenter(TEST_CENTER_1);
        mutation1.setMutationStatus(TEST_MUTATION_STATUS_1);
        mutation1.setValidationStatus(TEST_VALIDATION_STATUS_1);
        mutation1.setTumorAltCount(TEST_TUMOR_ALT_COUNT_1);
        mutation1.setTumorRefCount(TEST_TUMOR_REF_COUNT_1);
        mutation1.setNormalAltCount(TEST_NORMAL_ALT_COUNT_1);
        mutation1.setNormalRefCount(TEST_NORMAL_REF_COUNT_1);
        mutation1.setAminoAcidChange(TEST_AMINO_ACID_CHANGE_1);
        mutation1.setStartPosition(TEST_START_POSITION_1);
        mutation1.setEndPosition(TEST_END_POSITION_1);
        mutation1.setReferenceAllele(TEST_REFERENCE_ALLELE_1);
        mutation1.setTumorSeqAllele(TEST_TUMOR_SEQ_ALLELE_1);
        mutation1.setProteinChange(TEST_PROTEIN_CHANGE_1);
        mutation1.setMutationType(TEST_MUTATION_TYPE_1);
        mutation1.setNcbiBuild(TEST_NCBI_BUILD_1);
        mutation1.setVariantType(TEST_VARIANT_TYPE_1);
        mutation1.setOncotatorRefseqMrnaId(TEST_ONCOTATOR_REFSEQ_MRNA_ID_1);
        mutation1.setOncotatorProteinPosStart(TEST_ONCOTATOR_PROTEIN_POS_START_1);
        mutation1.setOncotatorProteinPosEnd(TEST_ONCOTATOR_PROTEIN_POS_END_1);
        mutation1.setKeyword(TEST_KEYWORD_1);
        mutationList.add(mutation1);
        Mutation mutation2 = new Mutation();
        mutation2.setGeneticProfileId(TEST_GENETIC_PROFILE_ID_2);
        mutation2.setGeneticProfileStableId(TEST_GENETIC_PROFILE_STABLE_ID_2);
        mutation2.setSampleId(TEST_SAMPLE_ID_2);
        mutation2.setSampleStableId(TEST_SAMPLE_STABLE_ID_2);
        mutation2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        mutation2.setCenter(TEST_CENTER_2);
        mutation2.setMutationStatus(TEST_MUTATION_STATUS_2);
        mutation2.setValidationStatus(TEST_VALIDATION_STATUS_2);
        mutation2.setTumorAltCount(TEST_TUMOR_ALT_COUNT_2);
        mutation2.setTumorRefCount(TEST_TUMOR_REF_COUNT_2);
        mutation2.setNormalAltCount(TEST_NORMAL_ALT_COUNT_2);
        mutation2.setNormalRefCount(TEST_NORMAL_REF_COUNT_2);
        mutation2.setAminoAcidChange(TEST_AMINO_ACID_CHANGE_2);
        mutation2.setStartPosition(TEST_START_POSITION_2);
        mutation2.setEndPosition(TEST_END_POSITION_2);
        mutation2.setReferenceAllele(TEST_REFERENCE_ALLELE_2);
        mutation2.setTumorSeqAllele(TEST_TUMOR_SEQ_ALLELE_2);
        mutation2.setProteinChange(TEST_PROTEIN_CHANGE_2);
        mutation2.setMutationType(TEST_MUTATION_TYPE_2);
        mutation2.setNcbiBuild(TEST_NCBI_BUILD_2);
        mutation2.setVariantType(TEST_VARIANT_TYPE_2);
        mutation2.setOncotatorRefseqMrnaId(TEST_ONCOTATOR_REFSEQ_MRNA_ID_2);
        mutation2.setOncotatorProteinPosStart(TEST_ONCOTATOR_PROTEIN_POS_START_2);
        mutation2.setOncotatorProteinPosEnd(TEST_ONCOTATOR_PROTEIN_POS_END_2);
        mutation2.setKeyword(TEST_KEYWORD_2);
        mutationList.add(mutation2);
        
        return mutationList;
    }

    private List<Mutation> createExampleMutationsWithGene() {

        List<Mutation> mutationList = createExampleMutations();
        Gene gene1 = new Gene();
        gene1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        gene1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        gene1.setType(TEST_TYPE_1);
        gene1.setCytoband(TEST_CYTOBAND_1);
        gene1.setLength(TEST_LENGTH_1);
        gene1.setChromosome(TEST_CHROMOSOME_1);
        mutationList.get(0).setGene(gene1);
        Gene gene2 = new Gene();
        gene2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        gene2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        gene2.setType(TEST_TYPE_2);
        gene2.setCytoband(TEST_CYTOBAND_2);
        gene2.setLength(TEST_LENGTH_2);
        gene2.setChromosome(TEST_CHROMOSOME_2);
        mutationList.get(1).setGene(gene2);
        
        return mutationList;
    }
}
