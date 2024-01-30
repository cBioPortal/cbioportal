package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.AlleleSpecificCopyNumber;
import org.cbioportal.model.Gene;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.service.MutationService;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.MutationFilter;
import org.cbioportal.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.web.parameter.MutationPositionIdentifier;
import org.cbioportal.web.parameter.SampleMolecularIdentifier;
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

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {MutationController.class, MutationCountController.class, TestConfig.class})
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
    private static final String TEST_NCBI_BUILD_1 = "test_ncbi_build_1";
    private static final String TEST_VARIANT_TYPE_1 = "test_variant_type_1";
    private static final String TEST_MUTATION_REFSEQ_MRNA_ID_1 = "test_mutation_refseq_mrna_id_1";
    private static final int TEST_MUTATION_PROTEIN_POS_START_1 = 1;
    private static final int TEST_MUTATION_PROTEIN_POS_END_1 = 1;
    private static final String TEST_KEYWORD_1 = "test_keyword_1";
    private static final String TEST_HUGO_GENE_SYMBOL_1 = "test_hugo_gene_symbol_1";
    private static final String TEST_TYPE_1 = "test_type_1";
    private static final String TEST_CYTOBAND_1 = "test_cytoband_1";
    private static final String TEST_CHROMOSOME_1 = "test_chromosome_1";
    private static final int TEST_ASCN_INTEGER_COPY_NUMBER_1 = 3;
    private static final String TEST_ASCN_METHOD_1 = "FACETS";
    private static final double TEST_CCF_EXPECTED_COPIES_UPPER_1 = 1.25;
    private static final double TEST_CCF_EXPECTED_COPIES_1 = 1.75;
    private static final String TEST_CLONAL_1 = "CLONAL";
    private static final int TEST_MINOR_COPY_NUMBER_1 = 2;
    private static final int TEST_EXPECTED_ALT_COPIES_1 = 1;
    private static final int TEST_TOTAL_COPY_NUMBER_1 = 4;
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
    private static final String TEST_NCBI_BUILD_2 = "test_ncbi_build_2";
    private static final String TEST_VARIANT_TYPE_2 = "test_variant_type_2";
    private static final String TEST_MUTATION_REFSEQ_MRNA_ID_2 = "test_MUTATION_refseq_mrna_id_2";
    private static final int TEST_MUTATION_PROTEIN_POS_START_2 = 2;
    private static final int TEST_MUTATION_PROTEIN_POS_END_2 = 2;
    private static final String TEST_KEYWORD_2 = "test_keyword_2";
    private static final String TEST_HUGO_GENE_SYMBOL_2 = "test_hugo_gene_symbol_2";
    private static final String TEST_TYPE_2 = "test_type_2";
    private static final String TEST_CYTOBAND_2 = "test_cytoband_2";
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
    private static final int TEST_ASCN_INTEGER_COPY_NUMBER_2 = 2;
    private static final String TEST_ASCN_METHOD_2 = "ASCN_METHOD2";
    private static final double TEST_CCF_EXPECTED_COPIES_UPPER_2 = 1.5;
    private static final double TEST_CCF_EXPECTED_COPIES_2 = 1.95;
    private static final String TEST_CLONAL_2 = "SUBCLONAL";
    private static final int TEST_MINOR_COPY_NUMBER_2 = 1;
    private static final int TEST_EXPECTED_ALT_COPIES_2 = 1;
    private static final int TEST_TOTAL_COPY_NUMBER_2 = 2;
    private static final String NAME_SPACE_COLUMNS = "{\"columnName\":{\"fieldName\":\"fieldValue\"}}";

    @MockBean
    private MutationService mutationService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void getMutationsInMolecularProfileBySampleListIdDefaultProjection() throws Exception {

        List<Mutation> mutationList = createExampleMutations();

        Mockito.when(mutationService.getMutationsInMolecularProfileBySampleListId(Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(mutationList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/molecular-profiles/test_molecular_profile_id/mutations")
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].ncbiBuild").value(TEST_NCBI_BUILD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantType").value(TEST_VARIANT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].refseqMrnaId").value(TEST_MUTATION_REFSEQ_MRNA_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosStart").value(TEST_MUTATION_PROTEIN_POS_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosEnd").value(TEST_MUTATION_PROTEIN_POS_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].keyword").value(TEST_KEYWORD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].namespaceColumns.columnName.fieldName").value("fieldValue"))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].ncbiBuild").value(TEST_NCBI_BUILD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantType").value(TEST_VARIANT_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].refseqMrnaId").value(TEST_MUTATION_REFSEQ_MRNA_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosStart").value(TEST_MUTATION_PROTEIN_POS_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosEnd").value(TEST_MUTATION_PROTEIN_POS_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].keyword").value(TEST_KEYWORD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].namespaceColumns.columnName.fieldName").value("fieldValue"));
    }

    @Test
    @WithMockUser
    public void getMutationsInMolecularProfileBySampleListIdDetailedProjection() throws Exception {

        List<Mutation> mutationList = createExampleMutationsWithGeneAndAlleleSpecificCopyNumber();

        Mockito.when(mutationService.getMutationsInMolecularProfileBySampleListId(Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(mutationList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/molecular-profiles/test_molecular_profile_id/mutations")
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].ncbiBuild").value(TEST_NCBI_BUILD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantType").value(TEST_VARIANT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].refseqMrnaId").value(TEST_MUTATION_REFSEQ_MRNA_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosStart").value(TEST_MUTATION_PROTEIN_POS_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosEnd").value(TEST_MUTATION_PROTEIN_POS_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].keyword").value(TEST_KEYWORD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.type").value(TEST_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.ascnIntegerCopyNumber").value(TEST_ASCN_INTEGER_COPY_NUMBER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.ascnMethod").value(TEST_ASCN_METHOD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.ccfExpectedCopiesUpper").value(TEST_CCF_EXPECTED_COPIES_UPPER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.ccfExpectedCopies").value(TEST_CCF_EXPECTED_COPIES_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.clonal").value(TEST_CLONAL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.minorCopyNumber").value(TEST_MINOR_COPY_NUMBER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.expectedAltCopies").value(TEST_EXPECTED_ALT_COPIES_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.totalCopyNumber").value(TEST_TOTAL_COPY_NUMBER_1))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].ncbiBuild").value(TEST_NCBI_BUILD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantType").value(TEST_VARIANT_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].refseqMrnaId").value(TEST_MUTATION_REFSEQ_MRNA_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosStart").value(TEST_MUTATION_PROTEIN_POS_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosEnd").value(TEST_MUTATION_PROTEIN_POS_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].keyword").value(TEST_KEYWORD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.type").value(TEST_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.ascnIntegerCopyNumber").value(TEST_ASCN_INTEGER_COPY_NUMBER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.ascnMethod").value(TEST_ASCN_METHOD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.ccfExpectedCopiesUpper").value(TEST_CCF_EXPECTED_COPIES_UPPER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.ccfExpectedCopies").value(TEST_CCF_EXPECTED_COPIES_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.clonal").value(TEST_CLONAL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.minorCopyNumber").value(TEST_MINOR_COPY_NUMBER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.expectedAltCopies").value(TEST_EXPECTED_ALT_COPIES_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.totalCopyNumber").value(TEST_TOTAL_COPY_NUMBER_2));
    }

    @Test
    @WithMockUser
    public void getMutationsInMolecularProfileBySampleListIdMetaProjection() throws Exception {

        MutationMeta mutationMeta = new MutationMeta();
        mutationMeta.setTotalCount(2);
        mutationMeta.setSampleCount(3);

        Mockito.when(mutationService.getMetaMutationsInMolecularProfileBySampleListId(Mockito.any(),
            Mockito.any(), Mockito.any())).thenReturn(mutationMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/molecular-profiles/test_molecular_profile_id/mutations")
            .param("sampleListId", TEST_SAMPLE_LIST_ID)
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"))
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.SAMPLE_COUNT, "3"));
    }

    @Test
    @WithMockUser
    public void fetchMutationsInMultipleMolecularProfiles() throws Exception {

        List<Mutation> mutationList = createExampleMutations();

        Mockito.when(mutationService.getMutationsInMultipleMolecularProfiles(Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(mutationList);

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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/mutations/fetch").with(csrf())
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].ncbiBuild").value(TEST_NCBI_BUILD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantType").value(TEST_VARIANT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].refseqMrnaId").value(TEST_MUTATION_REFSEQ_MRNA_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosStart").value(TEST_MUTATION_PROTEIN_POS_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosEnd").value(TEST_MUTATION_PROTEIN_POS_END_1))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].ncbiBuild").value(TEST_NCBI_BUILD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantType").value(TEST_VARIANT_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].refseqMrnaId").value(TEST_MUTATION_REFSEQ_MRNA_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosStart").value(TEST_MUTATION_PROTEIN_POS_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosEnd").value(TEST_MUTATION_PROTEIN_POS_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].keyword").value(TEST_KEYWORD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene").doesNotExist());
    }

    @Test
    @WithMockUser
    public void fetchMutationsInMolecularProfileDefaultProjection() throws Exception {

        List<Mutation> mutationList = createExampleMutations();

        Mockito.when(mutationService.fetchMutationsInMolecularProfile(Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.anyBoolean(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(mutationList);

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(TEST_SAMPLE_STABLE_ID_1);
        sampleIds.add(TEST_SAMPLE_STABLE_ID_2);
        MutationFilter mutationFilter = new MutationFilter();
        mutationFilter.setSampleIds(sampleIds);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/molecular-profiles/test_molecular_profile_id/mutations/fetch").with(csrf())
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].ncbiBuild").value(TEST_NCBI_BUILD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantType").value(TEST_VARIANT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].refseqMrnaId").value(TEST_MUTATION_REFSEQ_MRNA_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosStart").value(TEST_MUTATION_PROTEIN_POS_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosEnd").value(TEST_MUTATION_PROTEIN_POS_END_1))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].ncbiBuild").value(TEST_NCBI_BUILD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantType").value(TEST_VARIANT_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].refseqMrnaId").value(TEST_MUTATION_REFSEQ_MRNA_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosStart").value(TEST_MUTATION_PROTEIN_POS_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosEnd").value(TEST_MUTATION_PROTEIN_POS_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].keyword").value(TEST_KEYWORD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene").doesNotExist());
    }

    @Test
    @WithMockUser
    public void fetchMutationsInMolecularProfileDetailedProjection() throws Exception {

        List<Mutation> mutationList = createExampleMutationsWithGeneAndAlleleSpecificCopyNumber();

        Mockito.when(mutationService.fetchMutationsInMolecularProfile(Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.anyBoolean(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(mutationList);

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(TEST_SAMPLE_STABLE_ID_1);
        sampleIds.add(TEST_SAMPLE_STABLE_ID_2);
        MutationFilter mutationFilter = new MutationFilter();
        mutationFilter.setSampleIds(sampleIds);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/molecular-profiles/test_molecular_profile_id/mutations/fetch").with(csrf())
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].ncbiBuild").value(TEST_NCBI_BUILD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantType").value(TEST_VARIANT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].refseqMrnaId").value(TEST_MUTATION_REFSEQ_MRNA_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosStart").value(TEST_MUTATION_PROTEIN_POS_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosEnd").value(TEST_MUTATION_PROTEIN_POS_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].keyword").value(TEST_KEYWORD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.type").value(TEST_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.ascnIntegerCopyNumber").value(TEST_ASCN_INTEGER_COPY_NUMBER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.ascnMethod").value(TEST_ASCN_METHOD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.ccfExpectedCopiesUpper").value(TEST_CCF_EXPECTED_COPIES_UPPER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.ccfExpectedCopies").value(TEST_CCF_EXPECTED_COPIES_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.clonal").value(TEST_CLONAL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.minorCopyNumber").value(TEST_MINOR_COPY_NUMBER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.expectedAltCopies").value(TEST_EXPECTED_ALT_COPIES_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alleleSpecificCopyNumber.totalCopyNumber").value(TEST_TOTAL_COPY_NUMBER_1))
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
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].ncbiBuild").value(TEST_NCBI_BUILD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantType").value(TEST_VARIANT_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].refseqMrnaId").value(TEST_MUTATION_REFSEQ_MRNA_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosStart").value(TEST_MUTATION_PROTEIN_POS_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosEnd").value(TEST_MUTATION_PROTEIN_POS_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].keyword").value(TEST_KEYWORD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene.type").value(TEST_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.ascnIntegerCopyNumber").value(TEST_ASCN_INTEGER_COPY_NUMBER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.ascnMethod").value(TEST_ASCN_METHOD_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.ccfExpectedCopiesUpper").value(TEST_CCF_EXPECTED_COPIES_UPPER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.ccfExpectedCopies").value(TEST_CCF_EXPECTED_COPIES_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.clonal").value(TEST_CLONAL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.minorCopyNumber").value(TEST_MINOR_COPY_NUMBER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.expectedAltCopies").value(TEST_EXPECTED_ALT_COPIES_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alleleSpecificCopyNumber.totalCopyNumber").value(TEST_TOTAL_COPY_NUMBER_2));
    }

    @Test
    @WithMockUser
    public void fetchMutationsInMolecularProfileMetaProjection() throws Exception {

        MutationMeta mutationMeta = new MutationMeta();
        mutationMeta.setTotalCount(2);
        mutationMeta.setSampleCount(3);

        Mockito.when(mutationService.fetchMetaMutationsInMolecularProfile(Mockito.any(),
            Mockito.any(), Mockito.any())).thenReturn(mutationMeta);

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(TEST_SAMPLE_STABLE_ID_1);
        sampleIds.add(TEST_SAMPLE_STABLE_ID_2);
        MutationFilter mutationFilter = new MutationFilter();
        mutationFilter.setSampleIds(sampleIds);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/molecular-profiles/test_molecular_profile_id/mutations/fetch").with(csrf())
            .param("projection", "META")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(mutationFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"))
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.SAMPLE_COUNT, "3"));
    }

    @Test
    @WithMockUser
    public void fetchMutationCountsByPosition() throws Exception {

        List<MutationCountByPosition> mutationCountByPositionList = new ArrayList<>();
        MutationCountByPosition mutationCountByPosition1 = new MutationCountByPosition();
        mutationCountByPosition1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        mutationCountByPosition1.setProteinPosStart(TEST_MUTATION_PROTEIN_POS_START_1);
        mutationCountByPosition1.setProteinPosEnd(TEST_MUTATION_PROTEIN_POS_END_1);
        mutationCountByPosition1.setCount(TEST_MUTATION_COUNT_1);
        mutationCountByPositionList.add(mutationCountByPosition1);
        MutationCountByPosition mutationCountByPosition2 = new MutationCountByPosition();
        mutationCountByPosition2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        mutationCountByPosition2.setProteinPosStart(TEST_MUTATION_PROTEIN_POS_START_2);
        mutationCountByPosition2.setProteinPosEnd(TEST_MUTATION_PROTEIN_POS_END_2);
        mutationCountByPosition2.setCount(TEST_MUTATION_COUNT_2);
        mutationCountByPositionList.add(mutationCountByPosition2);

        Mockito.when(mutationService.fetchMutationCountsByPosition(Mockito.anyList(),
            Mockito.anyList(), Mockito.anyList()))
            .thenReturn(mutationCountByPositionList);

        List<MutationPositionIdentifier> mutationPositionIdentifiers = new ArrayList<>();
        MutationPositionIdentifier mutationPositionIdentifier1 = new MutationPositionIdentifier();
        mutationPositionIdentifier1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        mutationPositionIdentifier1.setProteinPosStart(TEST_MUTATION_PROTEIN_POS_START_1);
        mutationPositionIdentifier1.setProteinPosEnd(TEST_MUTATION_PROTEIN_POS_END_1);
        mutationPositionIdentifiers.add(mutationPositionIdentifier1);
        MutationPositionIdentifier mutationPositionIdentifier2 = new MutationPositionIdentifier();
        mutationPositionIdentifier2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        mutationPositionIdentifier2.setProteinPosStart(TEST_MUTATION_PROTEIN_POS_START_2);
        mutationPositionIdentifier2.setProteinPosEnd(TEST_MUTATION_PROTEIN_POS_END_2);
        mutationPositionIdentifiers.add(mutationPositionIdentifier2);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/mutation-counts-by-position/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(mutationPositionIdentifiers)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosStart").value(TEST_MUTATION_PROTEIN_POS_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinPosEnd").value(TEST_MUTATION_PROTEIN_POS_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value(TEST_MUTATION_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosStart").value(TEST_MUTATION_PROTEIN_POS_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].proteinPosEnd").value(TEST_MUTATION_PROTEIN_POS_END_2))
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
        mutation1.setNcbiBuild(TEST_NCBI_BUILD_1);
        mutation1.setVariantType(TEST_VARIANT_TYPE_1);
        mutation1.setRefseqMrnaId(TEST_MUTATION_REFSEQ_MRNA_ID_1);
        mutation1.setProteinPosStart(TEST_MUTATION_PROTEIN_POS_START_1);
        mutation1.setProteinPosEnd(TEST_MUTATION_PROTEIN_POS_END_1);
        mutation1.setKeyword(TEST_KEYWORD_1);
        mutation1.setAnnotationJSON(NAME_SPACE_COLUMNS);
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
        mutation2.setNcbiBuild(TEST_NCBI_BUILD_2);
        mutation2.setVariantType(TEST_VARIANT_TYPE_2);
        mutation2.setRefseqMrnaId(TEST_MUTATION_REFSEQ_MRNA_ID_2);
        mutation2.setProteinPosStart(TEST_MUTATION_PROTEIN_POS_START_2);
        mutation2.setProteinPosEnd(TEST_MUTATION_PROTEIN_POS_END_2);
        mutation2.setKeyword(TEST_KEYWORD_2);
        mutation2.setAnnotationJSON(NAME_SPACE_COLUMNS);
        mutationList.add(mutation2);

        return mutationList;
    }

    private List<Mutation> createExampleMutationsWithGeneAndAlleleSpecificCopyNumber() {

        List<Mutation> mutationList = createExampleMutations();
        Gene gene1 = new Gene();
        gene1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        gene1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        gene1.setType(TEST_TYPE_1);
        AlleleSpecificCopyNumber alleleSpecificCopyNumber1 = new AlleleSpecificCopyNumber();
        alleleSpecificCopyNumber1.setAscnIntegerCopyNumber(3);
        alleleSpecificCopyNumber1.setAscnMethod("FACETS");
        alleleSpecificCopyNumber1.setCcfExpectedCopiesUpper(1.25f);
        alleleSpecificCopyNumber1.setCcfExpectedCopies(1.75f);
        alleleSpecificCopyNumber1.setClonal("CLONAL");
        alleleSpecificCopyNumber1.setMinorCopyNumber(2);
        alleleSpecificCopyNumber1.setExpectedAltCopies(1);
        alleleSpecificCopyNumber1.setTotalCopyNumber(4);
        mutationList.get(0).setGene(gene1);
        mutationList.get(0).setAlleleSpecificCopyNumber(alleleSpecificCopyNumber1);
        Gene gene2 = new Gene();
        gene2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        gene2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        gene2.setType(TEST_TYPE_2);
        AlleleSpecificCopyNumber alleleSpecificCopyNumber2 = new AlleleSpecificCopyNumber();
        alleleSpecificCopyNumber2.setAscnIntegerCopyNumber(2);
        alleleSpecificCopyNumber2.setAscnMethod("ASCN_METHOD2");
        alleleSpecificCopyNumber2.setCcfExpectedCopiesUpper(1.5f);
        alleleSpecificCopyNumber2.setCcfExpectedCopies(1.95f);
        alleleSpecificCopyNumber2.setClonal("SUBCLONAL");
        alleleSpecificCopyNumber2.setMinorCopyNumber(1);
        alleleSpecificCopyNumber2.setExpectedAltCopies(1);
        alleleSpecificCopyNumber2.setTotalCopyNumber(2);
        mutationList.get(1).setGene(gene2);
        mutationList.get(1).setAlleleSpecificCopyNumber(alleleSpecificCopyNumber2);
        return mutationList;
    }
}
