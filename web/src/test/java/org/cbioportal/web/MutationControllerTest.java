package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.Gene;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.service.MutationService;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.MutationFilter;
import org.cbioportal.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.web.parameter.MutationPositionIdentifier;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web.xml")
@Configuration
public class MutationControllerTest {

    private static final String TEST_MOLECULAR_PROFILE_STABLE_ID_1 = "test_molecular_profile_stable_id_1";
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
    private static final String TEST_FUNCTIONAL_IMPACT_SCORE_1 = "test_functional_impact_score_1";
    private static final BigDecimal TEST_FIS_VALUE_1 = new BigDecimal(0.1);
    private static final String TEST_LINK_XVAR_1 = "test_link_xvar_1";
    private static final String TEST_LINK_PDB_1 = "test_link_pdb_1";
    private static final String TEST_LINK_MSA_1 = "test_link_msa_1";
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
    private static final int TEST_MOLECULAR_PROFILE_ID_2 = 2;
    private static final String TEST_MOLECULAR_PROFILE_STABLE_ID_2 = "test_molecular_profile_stable_id_2";
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
    private static final String TEST_FUNCTIONAL_IMPACT_SCORE_2 = "test_functional_impact_score_2";
    private static final BigDecimal TEST_FIS_VALUE_2 = new BigDecimal(0.2);
    private static final String TEST_LINK_XVAR_2 = "test_link_xvar_2";
    private static final String TEST_LINK_PDB_2 = "test_link_pdb_2";
    private static final String TEST_LINK_MSA_2 = "test_link_msa_2";
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
    private static final int TEST_MUTATION_COUNT_1 = 100;
    private static final int TEST_MUTATION_COUNT_2 = 200;
    private static final String TEST_SAMPLE_LIST_ID = "test_sample_list_id";
    private static final String TEST_DRIVER_FILTER_1 = "test_driver_filter_1";
    private static final String TEST_DRIVER_FILTER_ANNOTATION_1 = "test_driver_filter_annotation_1";
    private static final String TEST_DRIVER_TIERS_FILTER_1 = "test_driver_tiers_filter_1";
    private static final String TEST_DRIVER_TIERS_FILTER_ANNOTATION_1 = "test_driver_tiers_filter_annotation_1";
    private static final String TEST_DRIVER_FILTER_2 = "test_driver_filter_2";
    private static final String TEST_DRIVER_FILTER_ANNOTATION_2 = "test_driver_filter_annotation_2";
    private static final String TEST_DRIVER_TIERS_FILTER_2 = "test_driver_tiers_filter_2";
    private static final String TEST_DRIVER_TIERS_FILTER_ANNOTATION_2 = "test_driver_tiers_filter_annotation_2";

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
    public void getMutationsInMolecularProfileBySampleListIdDefaultProjection() throws Exception {

        List<Mutation> mutationList = createExampleMutations();
        
        Mockito.when(mutationService.getMutationsInMolecularProfileBySampleListId(Mockito.anyString(), 
            Mockito.anyString(), Mockito.anyListOf(Integer.class), Mockito.anyBoolean(), Mockito.anyString(), 
            Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(mutationList);

        mockMvc.perform(MockMvcRequestBuilders.get("/molecular-profiles/test_molecular_profile_id/mutations")
            .param("sampleListId", TEST_SAMPLE_LIST_ID)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_1))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilter").value(TEST_DRIVER_FILTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilterAnnotation").value(TEST_DRIVER_FILTER_ANNOTATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilter").value(TEST_DRIVER_TIERS_FILTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilterAnnotation").value(TEST_DRIVER_TIERS_FILTER_ANNOTATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].functionalImpactScore")
                .value(TEST_FUNCTIONAL_IMPACT_SCORE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].fisValue").value(TEST_FIS_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].linkXvar").value(TEST_LINK_XVAR_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].linkPdb").value(TEST_LINK_PDB_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].linkMsa").value(TEST_LINK_MSA_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].ncbiBuild").value(TEST_NCBI_BUILD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantType").value(TEST_VARIANT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].refseqMrnaId").value(TEST_ONCOTATOR_REFSEQ_MRNA_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].keyword").value(TEST_KEYWORD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_2))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverFilter").value(TEST_DRIVER_FILTER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverFilterAnnotation").value(TEST_DRIVER_FILTER_ANNOTATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverTiersFilter").value(TEST_DRIVER_TIERS_FILTER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverTiersFilterAnnotation").value(TEST_DRIVER_TIERS_FILTER_ANNOTATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].functionalImpactScore")
                .value(TEST_FUNCTIONAL_IMPACT_SCORE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].fisValue").value(TEST_FIS_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].linkXvar").value(TEST_LINK_XVAR_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].linkPdb").value(TEST_LINK_PDB_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].linkMsa").value(TEST_LINK_MSA_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].ncbiBuild").value(TEST_NCBI_BUILD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantType").value(TEST_VARIANT_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].refseqMrnaId").value(TEST_ONCOTATOR_REFSEQ_MRNA_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].keyword").value(TEST_KEYWORD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene").doesNotExist());
    }

    @Test
    public void getMutationsInMolecularProfileBySampleListIdDetailedProjection() throws Exception {

        List<Mutation> mutationList = createExampleMutationsWithGene();

        Mockito.when(mutationService.getMutationsInMolecularProfileBySampleListId(Mockito.anyString(), 
            Mockito.anyString(), Mockito.anyListOf(Integer.class), Mockito.anyBoolean(), Mockito.anyString(), 
            Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(mutationList);

        mockMvc.perform(MockMvcRequestBuilders.get("/molecular-profiles/test_molecular_profile_id/mutations")
            .param("sampleListId", TEST_SAMPLE_LIST_ID)
            .param("projection", "DETAILED")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_1))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilter").value(TEST_DRIVER_FILTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilterAnnotation").value(TEST_DRIVER_FILTER_ANNOTATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilter").value(TEST_DRIVER_TIERS_FILTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilterAnnotation").value(TEST_DRIVER_TIERS_FILTER_ANNOTATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].functionalImpactScore")
                .value(TEST_FUNCTIONAL_IMPACT_SCORE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].fisValue").value(TEST_FIS_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].linkXvar").value(TEST_LINK_XVAR_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].linkPdb").value(TEST_LINK_PDB_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].linkMsa").value(TEST_LINK_MSA_1))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId").value(TEST_MOLECULAR_PROFILE_STABLE_ID_2))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverFilter").value(TEST_DRIVER_FILTER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverFilterAnnotation").value(TEST_DRIVER_FILTER_ANNOTATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverTiersFilter").value(TEST_DRIVER_TIERS_FILTER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverTiersFilterAnnotation").value(TEST_DRIVER_TIERS_FILTER_ANNOTATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].functionalImpactScore")
                .value(TEST_FUNCTIONAL_IMPACT_SCORE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].fisValue").value(TEST_FIS_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].linkXvar").value(TEST_LINK_XVAR_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].linkPdb").value(TEST_LINK_PDB_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].linkMsa").value(TEST_LINK_MSA_2))
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
    public void getMutationsInMolecularProfileBySampleListIdMetaProjection() throws Exception {

        MutationMeta mutationMeta = new MutationMeta();
        mutationMeta.setTotalCount(2);
        mutationMeta.setSampleCount(3);

        Mockito.when(mutationService.getMetaMutationsInMolecularProfileBySampleListId(Mockito.anyString(), 
            Mockito.anyString(), Mockito.anyListOf(Integer.class))).thenReturn(mutationMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/molecular-profiles/test_molecular_profile_id/mutations")
            .param("sampleListId", TEST_SAMPLE_LIST_ID)
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"))
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.SAMPLE_COUNT, "3"));
    }

    @Test
    public void fetchMutationsInMultipleMolecularProfiles() throws Exception {

        List<Mutation> mutationList = createExampleMutations();

        Mockito.when(mutationService.getMutationsInMultipleMolecularProfiles(Mockito.anyListOf(String.class),
            Mockito.anyListOf(String.class), Mockito.anyListOf(Integer.class), Mockito.anyString(), Mockito.anyInt(),
            Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(mutationList);

        List<SampleMolecularIdentifier> sampleMolecularIdentifiers = new ArrayList<>();
        SampleMolecularIdentifier sampleMolecularIdentifier1 = new SampleMolecularIdentifier();
        sampleMolecularIdentifier1.setMolecularProfileId(TEST_MOLECULAR_PROFILE_STABLE_ID_1);
        sampleMolecularIdentifier1.setSampleId(TEST_SAMPLE_STABLE_ID_1);
        sampleMolecularIdentifiers.add(sampleMolecularIdentifier1);
        SampleMolecularIdentifier sampleMolecularIdentifier2 = new SampleMolecularIdentifier();
        sampleMolecularIdentifier2.setMolecularProfileId(TEST_MOLECULAR_PROFILE_STABLE_ID_2);
        sampleMolecularIdentifier2.setSampleId(TEST_SAMPLE_STABLE_ID_2);
        sampleMolecularIdentifiers.add(sampleMolecularIdentifier2);
        MutationMultipleStudyFilter mutationMultipleStudyFilter = new MutationMultipleStudyFilter();
        mutationMultipleStudyFilter.setSampleMolecularIdentifiers(sampleMolecularIdentifiers);

        mockMvc.perform(MockMvcRequestBuilders.post("/mutations/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(mutationMultipleStudyFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_1))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilter").value(TEST_DRIVER_FILTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilterAnnotation").value(TEST_DRIVER_FILTER_ANNOTATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilter").value(TEST_DRIVER_TIERS_FILTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilterAnnotation").value(TEST_DRIVER_TIERS_FILTER_ANNOTATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].functionalImpactScore")
                .value(TEST_FUNCTIONAL_IMPACT_SCORE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].fisValue").value(TEST_FIS_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].linkXvar").value(TEST_LINK_XVAR_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].linkPdb").value(TEST_LINK_PDB_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].linkMsa").value(TEST_LINK_MSA_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].ncbiBuild").value(TEST_NCBI_BUILD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantType").value(TEST_VARIANT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].refseqMrnaId").value(TEST_ONCOTATOR_REFSEQ_MRNA_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].keyword").value(TEST_KEYWORD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_2))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverFilter").value(TEST_DRIVER_FILTER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverFilterAnnotation").value(TEST_DRIVER_FILTER_ANNOTATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverTiersFilter").value(TEST_DRIVER_TIERS_FILTER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverTiersFilterAnnotation").value(TEST_DRIVER_TIERS_FILTER_ANNOTATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].functionalImpactScore")
                .value(TEST_FUNCTIONAL_IMPACT_SCORE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].fisValue").value(TEST_FIS_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].linkXvar").value(TEST_LINK_XVAR_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].linkPdb").value(TEST_LINK_PDB_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].linkMsa").value(TEST_LINK_MSA_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].ncbiBuild").value(TEST_NCBI_BUILD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantType").value(TEST_VARIANT_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].refseqMrnaId").value(TEST_ONCOTATOR_REFSEQ_MRNA_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].keyword").value(TEST_KEYWORD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene").doesNotExist());
    }

    @Test
    public void fetchMutationsInMolecularProfileDefaultProjection() throws Exception {

        List<Mutation> mutationList = createExampleMutations();

        Mockito.when(mutationService.fetchMutationsInMolecularProfile(Mockito.anyString(), 
            Mockito.anyListOf(String.class), Mockito.anyListOf(Integer.class), Mockito.anyBoolean(), 
            Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(mutationList);
        
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(TEST_SAMPLE_STABLE_ID_1);
        sampleIds.add(TEST_SAMPLE_STABLE_ID_2);
        MutationFilter mutationFilter = new MutationFilter();
        mutationFilter.setSampleIds(sampleIds);

        mockMvc.perform(MockMvcRequestBuilders.post("/molecular-profiles/test_molecular_profile_id/mutations/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(mutationFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_1))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilter").value(TEST_DRIVER_FILTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilterAnnotation").value(TEST_DRIVER_FILTER_ANNOTATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilter").value(TEST_DRIVER_TIERS_FILTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilterAnnotation").value(TEST_DRIVER_TIERS_FILTER_ANNOTATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].functionalImpactScore")
                .value(TEST_FUNCTIONAL_IMPACT_SCORE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].fisValue").value(TEST_FIS_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].linkXvar").value(TEST_LINK_XVAR_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].linkPdb").value(TEST_LINK_PDB_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].linkMsa").value(TEST_LINK_MSA_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].ncbiBuild").value(TEST_NCBI_BUILD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantType").value(TEST_VARIANT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].refseqMrnaId").value(TEST_ONCOTATOR_REFSEQ_MRNA_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].keyword").value(TEST_KEYWORD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_2))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverFilter").value(TEST_DRIVER_FILTER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverFilterAnnotation").value(TEST_DRIVER_FILTER_ANNOTATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverTiersFilter").value(TEST_DRIVER_TIERS_FILTER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverTiersFilterAnnotation").value(TEST_DRIVER_TIERS_FILTER_ANNOTATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].functionalImpactScore")
                .value(TEST_FUNCTIONAL_IMPACT_SCORE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].fisValue").value(TEST_FIS_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].linkXvar").value(TEST_LINK_XVAR_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].linkPdb").value(TEST_LINK_PDB_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].linkMsa").value(TEST_LINK_MSA_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].ncbiBuild").value(TEST_NCBI_BUILD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantType").value(TEST_VARIANT_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].refseqMrnaId").value(TEST_ONCOTATOR_REFSEQ_MRNA_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].keyword").value(TEST_KEYWORD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene").doesNotExist());
    }

    @Test
    public void fetchMutationsInMolecularProfileDetailedProjection() throws Exception {

        List<Mutation> mutationList = createExampleMutationsWithGene();

        Mockito.when(mutationService.fetchMutationsInMolecularProfile(Mockito.anyString(),
            Mockito.anyListOf(String.class), Mockito.anyListOf(Integer.class), Mockito.anyBoolean(), 
            Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(mutationList);

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(TEST_SAMPLE_STABLE_ID_1);
        sampleIds.add(TEST_SAMPLE_STABLE_ID_2);
        MutationFilter mutationFilter = new MutationFilter();
        mutationFilter.setSampleIds(sampleIds);

        mockMvc.perform(MockMvcRequestBuilders.post("/molecular-profiles/test_molecular_profile_id/mutations/fetch")
            .param("projection", "DETAILED")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(mutationFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_1))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilter").value(TEST_DRIVER_FILTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilterAnnotation").value(TEST_DRIVER_FILTER_ANNOTATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilter").value(TEST_DRIVER_TIERS_FILTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilterAnnotation").value(TEST_DRIVER_TIERS_FILTER_ANNOTATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].functionalImpactScore")
                .value(TEST_FUNCTIONAL_IMPACT_SCORE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].fisValue").value(TEST_FIS_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].linkXvar").value(TEST_LINK_XVAR_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].linkPdb").value(TEST_LINK_PDB_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].linkMsa").value(TEST_LINK_MSA_1))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId")
                .value(TEST_MOLECULAR_PROFILE_STABLE_ID_2))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverFilter").value(TEST_DRIVER_FILTER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverFilterAnnotation").value(TEST_DRIVER_FILTER_ANNOTATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverTiersFilter").value(TEST_DRIVER_TIERS_FILTER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].driverTiersFilterAnnotation").value(TEST_DRIVER_TIERS_FILTER_ANNOTATION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].functionalImpactScore")
                .value(TEST_FUNCTIONAL_IMPACT_SCORE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].fisValue").value(TEST_FIS_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].linkXvar").value(TEST_LINK_XVAR_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].linkPdb").value(TEST_LINK_PDB_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].linkMsa").value(TEST_LINK_MSA_2))
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
    public void fetchMutationsInMolecularProfileMetaProjection() throws Exception {

        MutationMeta mutationMeta = new MutationMeta();
        mutationMeta.setTotalCount(2);
        mutationMeta.setSampleCount(3);

        Mockito.when(mutationService.fetchMetaMutationsInMolecularProfile(Mockito.anyString(), 
            Mockito.anyListOf(String.class), Mockito.anyListOf(Integer.class))).thenReturn(mutationMeta);

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(TEST_SAMPLE_STABLE_ID_1);
        sampleIds.add(TEST_SAMPLE_STABLE_ID_2);
        MutationFilter mutationFilter = new MutationFilter();
        mutationFilter.setSampleIds(sampleIds);

        mockMvc.perform(MockMvcRequestBuilders.post("/molecular-profiles/test_molecular_profile_id/mutations/fetch")
            .param("projection", "META")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(mutationFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"))
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.SAMPLE_COUNT, "3"));
    }

    @Test
    public void fetchMutationCountsByPosition() throws Exception {

        List<MutationCountByPosition> mutationCountByPositionList = new ArrayList<>();
        MutationCountByPosition mutationCountByPosition1 = new MutationCountByPosition();
        mutationCountByPosition1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        mutationCountByPosition1.setProteinPosStart(TEST_ONCOTATOR_PROTEIN_POS_START_1);
        mutationCountByPosition1.setProteinPosEnd(TEST_ONCOTATOR_PROTEIN_POS_END_1);
        mutationCountByPosition1.setCount(TEST_MUTATION_COUNT_1);
        mutationCountByPositionList.add(mutationCountByPosition1);
        MutationCountByPosition mutationCountByPosition2 = new MutationCountByPosition();
        mutationCountByPosition2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        mutationCountByPosition2.setProteinPosStart(TEST_ONCOTATOR_PROTEIN_POS_START_2);
        mutationCountByPosition2.setProteinPosEnd(TEST_ONCOTATOR_PROTEIN_POS_END_2);
        mutationCountByPosition2.setCount(TEST_MUTATION_COUNT_2);
        mutationCountByPositionList.add(mutationCountByPosition2);

        Mockito.when(mutationService.fetchMutationCountsByPosition(Mockito.anyListOf(Integer.class), 
            Mockito.anyListOf(Integer.class), Mockito.anyListOf(Integer.class)))
            .thenReturn(mutationCountByPositionList);

        List<MutationPositionIdentifier> mutationPositionIdentifiers = new ArrayList<>();
        MutationPositionIdentifier mutationPositionIdentifier1 = new MutationPositionIdentifier();
        mutationPositionIdentifier1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        mutationPositionIdentifier1.setProteinPosStart(TEST_ONCOTATOR_PROTEIN_POS_START_1);
        mutationPositionIdentifier1.setProteinPosEnd(TEST_ONCOTATOR_PROTEIN_POS_END_1);
        mutationPositionIdentifiers.add(mutationPositionIdentifier1);
        MutationPositionIdentifier mutationPositionIdentifier2 = new MutationPositionIdentifier();
        mutationPositionIdentifier2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        mutationPositionIdentifier2.setProteinPosStart(TEST_ONCOTATOR_PROTEIN_POS_START_2);
        mutationPositionIdentifier2.setProteinPosEnd(TEST_ONCOTATOR_PROTEIN_POS_END_2);
        mutationPositionIdentifiers.add(mutationPositionIdentifier2);

        mockMvc.perform(MockMvcRequestBuilders.post("/mutation-counts-by-position/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(mutationPositionIdentifiers)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value(TEST_MUTATION_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosStart").value(TEST_ONCOTATOR_PROTEIN_POS_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosEnd").value(TEST_ONCOTATOR_PROTEIN_POS_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].count").value(TEST_MUTATION_COUNT_2));
    }

    private List<Mutation> createExampleMutations() {

        List<Mutation> mutationList = new ArrayList<>();
        Mutation mutation1 = new Mutation();
        mutation1.setMolecularProfileId(TEST_MOLECULAR_PROFILE_STABLE_ID_1);
        mutation1.setSampleId(TEST_SAMPLE_STABLE_ID_1);
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
        mutation1.setDriverFilter(TEST_DRIVER_FILTER_1);
        mutation1.setDriverFilterAnnotation(TEST_DRIVER_FILTER_ANNOTATION_1);
        mutation1.setDriverTiersFilter(TEST_DRIVER_TIERS_FILTER_1);
        mutation1.setDriverTiersFilterAnnotation(TEST_DRIVER_TIERS_FILTER_ANNOTATION_1);
        mutation1.setFunctionalImpactScore(TEST_FUNCTIONAL_IMPACT_SCORE_1);
        mutation1.setFisValue(TEST_FIS_VALUE_1);
        mutation1.setLinkXvar(TEST_LINK_XVAR_1);
        mutation1.setLinkPdb(TEST_LINK_PDB_1);
        mutation1.setLinkMsa(TEST_LINK_MSA_1);
        mutation1.setNcbiBuild(TEST_NCBI_BUILD_1);
        mutation1.setVariantType(TEST_VARIANT_TYPE_1);
        mutation1.setOncotatorRefseqMrnaId(TEST_ONCOTATOR_REFSEQ_MRNA_ID_1);
        mutation1.setOncotatorProteinPosStart(TEST_ONCOTATOR_PROTEIN_POS_START_1);
        mutation1.setOncotatorProteinPosEnd(TEST_ONCOTATOR_PROTEIN_POS_END_1);
        mutation1.setKeyword(TEST_KEYWORD_1);
        mutationList.add(mutation1);
        Mutation mutation2 = new Mutation();
        mutation2.setMolecularProfileId(TEST_MOLECULAR_PROFILE_STABLE_ID_2);
        mutation2.setSampleId(TEST_SAMPLE_STABLE_ID_2);
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
        mutation2.setDriverFilter(TEST_DRIVER_FILTER_2);
        mutation2.setDriverFilterAnnotation(TEST_DRIVER_FILTER_ANNOTATION_2);
        mutation2.setDriverTiersFilter(TEST_DRIVER_TIERS_FILTER_2);
        mutation2.setDriverTiersFilterAnnotation(TEST_DRIVER_TIERS_FILTER_ANNOTATION_2);
        mutation2.setFunctionalImpactScore(TEST_FUNCTIONAL_IMPACT_SCORE_2);
        mutation2.setFisValue(TEST_FIS_VALUE_2);
        mutation2.setLinkXvar(TEST_LINK_XVAR_2);
        mutation2.setLinkPdb(TEST_LINK_PDB_2);
        mutation2.setLinkMsa(TEST_LINK_MSA_2);
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
