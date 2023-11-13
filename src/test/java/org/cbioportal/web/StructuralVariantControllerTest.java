/*
 * Copyright (c) 2018 - 2022 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cbioportal.model.StructuralVariant;
import org.cbioportal.model.StructuralVariantQuery;
import org.cbioportal.model.StructuralVariantSpecialValue;
import org.cbioportal.service.StructuralVariantService;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.SampleMolecularIdentifier;
import org.cbioportal.web.parameter.StructuralVariantFilter;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {StructuralVariantController.class, TestConfig.class})
public class StructuralVariantControllerTest {

    private static final String TEST_GENETIC_PROFILE_STABLE_ID_1 = "test_genetic_profile_stable_id_1";
    private static final String TEST_SAMPLE_ID_1 = "test_sample_id_1";
    private static final String TEST_PATIENT_ID_1 = "test_patient_id_1";
    private static final String TEST_STUDY_ID_1 = "test_study_id_1";
    // Sample and Patient key are retrieved after response is given back
    // TEST_UNIQUE_SAMPLE_KEY_1 decoded: test_sample_id_1:test_study_id_1
    private static final String TEST_UNIQUE_SAMPLE_KEY_1 = "dGVzdF9zYW1wbGVfaWRfMTp0ZXN0X3N0dWR5X2lkXzE";
    // TEST_UNIQUE_PATIENT_KEY_1 decoded: test_patient_id_1:test_study_id_1
    private static final String TEST_UNIQUE_PATIENT_KEY_1 = "dGVzdF9wYXRpZW50X2lkXzE6dGVzdF9zdHVkeV9pZF8x";
    private static final Integer TEST_SITE1_ENTREZ_GENE_ID_1 = 1;
    private static final String TEST_SITE1_HUGO_SYMBOL_1 = "test_site1_hugo_symbol_1";
    private static final String TEST_SITE1_ENSEMBL_TRANSCRIPT_ID_1 = "test_site1_ensembl_transcript_id_1";
    private static final String TEST_SITE1_CHROMOSOME_1 = "test_site1_chromosome_1";
    private static final String TEST_SITE1_REGION = "exon";
    private static final Integer TEST_SITE1_REGION_NUMBER = -1;
    private static final String TEST_SITE1_CONTIG = "q13.4";
    private static final Integer TEST_SITE1_POSITION_1 = 1;
    private static final String TEST_SITE1_DESCRIPTION_1 = "test_site1_description_1";
    private static final Integer TEST_SITE2_ENTREZ_GENE_ID_1 = 1;
    private static final String TEST_SITE2_HUGO_SYMBOL_1 = "test_site2_hugo_symbol_1";
    private static final String TEST_SITE2_ENSEMBL_TRANSCRIPT_ID_1 = "test_site2_ensembl_transcript_id_1";
    private static final String TEST_SITE2_CHROMOSOME_1 = "test_site2_chromosome_1";
    private static final String TEST_SITE2_REGION = "intron";
    private static final Integer TEST_SITE2_REGION_NUMBER = -1;
    private static final String TEST_SITE2_CONTIG = "p13.1";
    private static final Integer TEST_SITE2_POSITION_1 = 1;
    private static final String TEST_SITE2_DESCRIPTION_1 = "test_site2_description_1";
    private static final String TEST_SITE2_EFFECT_ON_FRAME_1 = "test_site2_effect_on_frame_1";
    private static final String TEST_NCBI_BUILD_1 = "test_ncbi_build_1";
    private static final String TEST_DNA_SUPPORT_1 = "test_dna_support_1";
    private static final String TEST_RNA_SUPPORT_1 = "test_rna_support_1";
    private static final Integer TEST_NORMAL_READ_COUNT_1 = 1;
    private static final Integer TEST_TUMOR_READ_COUNT_1 = 1;
    private static final Integer TEST_NORMAL_VARIANT_COUNT_1 = 1;
    private static final Integer TEST_TUMOR_VARIANT_COUNT_1 = 1;
    private static final Integer TEST_NORMAL_PAIRED_END_READ_COUNT_1 = 1;
    private static final Integer TEST_TUMOR_PAIRED_END_READ_COUNT_1 = 1;
    private static final Integer TEST_NORMAL_SPLIT_READ_COUNT_1 = 1;
    private static final Integer TEST_TUMOR_SPLIT_READ_COUNT_1 = 1;
    private static final String TEST_ANNOTATION_1 = "test_annotation_1";
    private static final String TEST_BREAKPOINT_TYPE_1 = "test_breakpoint_type_1";
    private static final String TEST_CONNECTION_TYPE_1 = "test_connection_type_1";
    private static final String TEST_EVENT_INFO_1 = "test_event_info_1";
    private static final String TEST_VARIANT_CLASS_1 = "test_variant_class_1";
    private static final Integer TEST_LENGTH_1 = 1;
    private static final String TEST_COMMENTS_1 = "test_comments_1";
    private static final String TEST_DRIVER_FILTER_1 = "test_driver_filter_1";
    private static final String TEST_DRIVER_FILTER_ANN_1 = "test_driver_filter_ann_1";
    private static final String TEST_DRIVER_TIERS_FILTER_1 = "test_driver_tiers_filter_1";
    private static final String TEST_DRIVER_TIERS_FILTER_ANN_1 = "test_driver_tiers_filter_ann_1";
    private static final String TEST_SV_STATUS = "SOMATIC";
    private static final String TEST_ANNOTATION_JSON_1 = "{\"columnName\":{\"fieldName\":\"fieldValue\"}}";

    @MockBean
    private StructuralVariantService structuralVariantService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    public void fetchStructuralVariantsMolecularProfileId() throws Exception {

        List<StructuralVariant> structuralVariant = createExampleStructuralVariant();

        Mockito.when(structuralVariantService.fetchStructuralVariants(Mockito.anyList(),
                Mockito.anyList(), Mockito.anyList(), Mockito.any())).thenReturn(structuralVariant);

        StructuralVariantFilter structuralVariantFilter = createStructuralVariantFilterMolecularProfileId();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/structural-variant/fetch").with(csrf())
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(structuralVariantFilter)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_PATIENT_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].uniqueSampleKey").value(TEST_UNIQUE_SAMPLE_KEY_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].uniquePatientKey").value(TEST_UNIQUE_PATIENT_KEY_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1EntrezGeneId").value((int) TEST_SITE1_ENTREZ_GENE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1HugoSymbol").value(TEST_SITE1_HUGO_SYMBOL_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1EnsemblTranscriptId").value(TEST_SITE1_ENSEMBL_TRANSCRIPT_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Chromosome").value(TEST_SITE1_CHROMOSOME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Region").value(TEST_SITE1_REGION))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1RegionNumber").value(TEST_SITE1_REGION_NUMBER))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Contig").value(TEST_SITE1_CONTIG))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Position").value(TEST_SITE1_POSITION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Description").value(TEST_SITE1_DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2EntrezGeneId").value((int) TEST_SITE2_ENTREZ_GENE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2HugoSymbol").value(TEST_SITE2_HUGO_SYMBOL_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2EnsemblTranscriptId").value(TEST_SITE2_ENSEMBL_TRANSCRIPT_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Chromosome").value(TEST_SITE2_CHROMOSOME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Region").value(TEST_SITE2_REGION))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2RegionNumber").value(TEST_SITE2_REGION_NUMBER))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Contig").value(TEST_SITE2_CONTIG))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Position").value(TEST_SITE2_POSITION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Description").value(TEST_SITE2_DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2EffectOnFrame").value(TEST_SITE2_EFFECT_ON_FRAME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].ncbiBuild").value(TEST_NCBI_BUILD_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].dnaSupport").value(TEST_DNA_SUPPORT_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].rnaSupport").value(TEST_RNA_SUPPORT_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalReadCount").value(TEST_NORMAL_READ_COUNT_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorReadCount").value(TEST_TUMOR_READ_COUNT_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalVariantCount").value(TEST_NORMAL_VARIANT_COUNT_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorVariantCount").value(TEST_TUMOR_VARIANT_COUNT_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalPairedEndReadCount").value(TEST_NORMAL_PAIRED_END_READ_COUNT_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorPairedEndReadCount").value(TEST_TUMOR_PAIRED_END_READ_COUNT_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalSplitReadCount").value(TEST_NORMAL_SPLIT_READ_COUNT_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorSplitReadCount").value(TEST_TUMOR_SPLIT_READ_COUNT_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].annotation").value(TEST_ANNOTATION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].breakpointType").value(TEST_BREAKPOINT_TYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].connectionType").value(TEST_CONNECTION_TYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].eventInfo").value(TEST_EVENT_INFO_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantClass").value(TEST_VARIANT_CLASS_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].length").value(TEST_LENGTH_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].comments").value(TEST_COMMENTS_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilter").value(TEST_DRIVER_FILTER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilterAnn").value(TEST_DRIVER_FILTER_ANN_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilter").value(TEST_DRIVER_TIERS_FILTER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilterAnn").value(TEST_DRIVER_TIERS_FILTER_ANN_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].svStatus").value(TEST_SV_STATUS));
    }

    @Test
    @WithMockUser
    public void fetchStructuralVariantsSampleMolecularIdentifier() throws Exception {

        List<StructuralVariant> structuralVariant = createExampleStructuralVariant();

        Mockito.when(structuralVariantService.fetchStructuralVariants(Mockito.anyList(),
                Mockito.anyList(), Mockito.anyList(), Mockito.any())).thenReturn(structuralVariant);

        StructuralVariantFilter structuralVariantFilter = createStructuralVariantFilterSampleMolecularIdentifier();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/structural-variant/fetch").with(csrf())
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(structuralVariantFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_PATIENT_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].uniqueSampleKey").value(TEST_UNIQUE_SAMPLE_KEY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].uniquePatientKey").value(TEST_UNIQUE_PATIENT_KEY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1EntrezGeneId").value((int) TEST_SITE1_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1HugoSymbol").value(TEST_SITE1_HUGO_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1EnsemblTranscriptId").value(TEST_SITE1_ENSEMBL_TRANSCRIPT_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Chromosome").value(TEST_SITE1_CHROMOSOME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Region").value(TEST_SITE1_REGION))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1RegionNumber").value(TEST_SITE1_REGION_NUMBER))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Contig").value(TEST_SITE1_CONTIG))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Position").value(TEST_SITE1_POSITION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Description").value(TEST_SITE1_DESCRIPTION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2EntrezGeneId").value((int) TEST_SITE2_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2HugoSymbol").value(TEST_SITE2_HUGO_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2EnsemblTranscriptId").value(TEST_SITE2_ENSEMBL_TRANSCRIPT_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Chromosome").value(TEST_SITE2_CHROMOSOME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Region").value(TEST_SITE2_REGION))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2RegionNumber").value(TEST_SITE2_REGION_NUMBER))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Contig").value(TEST_SITE2_CONTIG))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Position").value(TEST_SITE2_POSITION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Description").value(TEST_SITE2_DESCRIPTION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2EffectOnFrame").value(TEST_SITE2_EFFECT_ON_FRAME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].ncbiBuild").value(TEST_NCBI_BUILD_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].dnaSupport").value(TEST_DNA_SUPPORT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].rnaSupport").value(TEST_RNA_SUPPORT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalReadCount").value(TEST_NORMAL_READ_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorReadCount").value(TEST_TUMOR_READ_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalVariantCount").value(TEST_NORMAL_VARIANT_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorVariantCount").value(TEST_TUMOR_VARIANT_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalPairedEndReadCount").value(TEST_NORMAL_PAIRED_END_READ_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorPairedEndReadCount").value(TEST_TUMOR_PAIRED_END_READ_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalSplitReadCount").value(TEST_NORMAL_SPLIT_READ_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorSplitReadCount").value(TEST_TUMOR_SPLIT_READ_COUNT_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].annotation").value(TEST_ANNOTATION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].breakpointType").value(TEST_BREAKPOINT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].connectionType").value(TEST_CONNECTION_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].eventInfo").value(TEST_EVENT_INFO_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantClass").value(TEST_VARIANT_CLASS_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].length").value(TEST_LENGTH_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].comments").value(TEST_COMMENTS_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilter").value(TEST_DRIVER_FILTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilterAnn").value(TEST_DRIVER_FILTER_ANN_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilter").value(TEST_DRIVER_TIERS_FILTER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilterAnn").value(TEST_DRIVER_TIERS_FILTER_ANN_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].svStatus").value(TEST_SV_STATUS))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].namespaceColumns.columnName.fieldName").value("fieldValue"));
    }

    @Test
    @WithMockUser
    public void fetchStructuralVariantsBothMolecularProfileIdAndSampleMolecularIdentifier() throws Exception {

        List<StructuralVariant> structuralVariant = createExampleStructuralVariant();

        Mockito.when(structuralVariantService.fetchStructuralVariants(Mockito.anyList(),
                Mockito.anyList(), Mockito.anyList(), Mockito.any())).thenReturn(structuralVariant);

        StructuralVariantFilter structuralVariantFilter = createStructuralVariantFilterMolecularProfileIdAndSampleMolecularIdentifier();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/structural-variant/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(structuralVariantFilter)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("interceptedStructuralVariantFilter must be true"));
    }
    
    @Test
    @WithMockUser
    public void fetchStructuralVariantsWithBothEntrezIdsAndStructVariantIdsReturnsStatusOk() throws Exception {
        String structuralVariantFilter = createStructuralVariantFilterWithEntrezIdAndStructuralVariantJson();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/structural-variant/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(structuralVariantFilter))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    @Test
    @WithMockUser
    public void fetchStructuralVariantsWithStructuralVariantSpecialQueryValues() throws Exception {

        List<StructuralVariant> structuralVariant = createExampleStructuralVariant();

        ArgumentCaptor<ArrayList<StructuralVariantQuery>> structVarIdCaptor = ArgumentCaptor.forClass(ArrayList.class);
        Mockito.when(structuralVariantService.fetchStructuralVariants(
            Mockito.anyList(), Mockito.anyList(), Mockito.any(), Mockito.any()
        )).thenReturn(structuralVariant);

        String structuralVariantFilterJson = createStructuralVariantFilterWithStructuralVariantWildcard();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/structural-variant/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(structuralVariantFilterJson))
            .andExpect(MockMvcResultMatchers.status().isOk());
        
        Mockito.verify(structuralVariantService).fetchStructuralVariants(
            Mockito.anyList(), Mockito.anyList(), Mockito.any(), structVarIdCaptor.capture()
        );

        List<StructuralVariantQuery> capturedStructVarIds = structVarIdCaptor.getValue();
        Assert.assertEquals(capturedStructVarIds.size(), 2);

        Assert.assertEquals(capturedStructVarIds.get(0).getGene1().getSpecialValue(), StructuralVariantSpecialValue.ANY_GENE);
        Assert.assertNull(capturedStructVarIds.get(0).getGene1().getEntrezId());
        Assert.assertEquals(capturedStructVarIds.get(0).getGene2().getEntrezId(), (Integer) 2);
        Assert.assertNull(capturedStructVarIds.get(0).getGene2().getSpecialValue());

        Assert.assertNull(capturedStructVarIds.get(1).getGene1().getSpecialValue());
        Assert.assertEquals(capturedStructVarIds.get(1).getGene1().getEntrezId(), (Integer) 1);
        Assert.assertNull(capturedStructVarIds.get(1).getGene2().getEntrezId());
        Assert.assertEquals(capturedStructVarIds.get(1).getGene2().getSpecialValue(), StructuralVariantSpecialValue.NO_GENE);
    }

    @Test
    @WithMockUser
    public void fetchStructuralVariantsWithStructuralVariantIdWithNoIdAndSpecialValueReturnsBadRequest() throws Exception {

        List<StructuralVariant> structuralVariant = createExampleStructuralVariant();

        Mockito.when(structuralVariantService.fetchStructuralVariants(
            Mockito.anyList(), Mockito.anyList(), Mockito.any(), Mockito.any()
        )).thenReturn(structuralVariant);

        String structuralVariantFilterJson = createStructuralVariantFilterWithEmptyStructuralVariantId();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/structural-variant/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(structuralVariantFilterJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("interceptedStructuralVariantFilter " +
                            "Should contain only one EntrezId, hugoSymbol or specialValue."));
        
    }

    private List<StructuralVariant> createExampleStructuralVariant() {

        List<StructuralVariant> structuralVariantList = new ArrayList<>();
        StructuralVariant structuralVariant1 = new StructuralVariant();
        structuralVariant1.setMolecularProfileId(TEST_GENETIC_PROFILE_STABLE_ID_1);
        structuralVariant1.setSampleId(TEST_SAMPLE_ID_1);
        structuralVariant1.setPatientId(TEST_PATIENT_ID_1);
        structuralVariant1.setStudyId(TEST_STUDY_ID_1);
        structuralVariant1.setUniqueSampleKey(TEST_UNIQUE_SAMPLE_KEY_1);
        structuralVariant1.setUniquePatientKey(TEST_UNIQUE_PATIENT_KEY_1);
        structuralVariant1.setSite1EntrezGeneId(TEST_SITE1_ENTREZ_GENE_ID_1);
        structuralVariant1.setSite1HugoSymbol(TEST_SITE1_HUGO_SYMBOL_1);
        structuralVariant1.setSite1EnsemblTranscriptId(TEST_SITE1_ENSEMBL_TRANSCRIPT_ID_1);
        structuralVariant1.setSite1Chromosome(TEST_SITE1_CHROMOSOME_1);
        structuralVariant1.setSite1Region(TEST_SITE1_REGION);
        structuralVariant1.setSite1RegionNumber(TEST_SITE1_REGION_NUMBER);
        structuralVariant1.setSite1Contig(TEST_SITE1_CONTIG);
        structuralVariant1.setSite1Position(TEST_SITE1_POSITION_1);
        structuralVariant1.setSite1Description(TEST_SITE1_DESCRIPTION_1);
        structuralVariant1.setSite2EntrezGeneId(TEST_SITE2_ENTREZ_GENE_ID_1);
        structuralVariant1.setSite2HugoSymbol(TEST_SITE2_HUGO_SYMBOL_1);
        structuralVariant1.setSite2EnsemblTranscriptId(TEST_SITE2_ENSEMBL_TRANSCRIPT_ID_1);
        structuralVariant1.setSite2Chromosome(TEST_SITE2_CHROMOSOME_1);
        structuralVariant1.setSite2Region(TEST_SITE2_REGION);
        structuralVariant1.setSite2RegionNumber(TEST_SITE2_REGION_NUMBER);
        structuralVariant1.setSite2Contig(TEST_SITE2_CONTIG);
        structuralVariant1.setSite2Position(TEST_SITE2_POSITION_1);
        structuralVariant1.setSite2Description(TEST_SITE2_DESCRIPTION_1);
        structuralVariant1.setSite2EffectOnFrame(TEST_SITE2_EFFECT_ON_FRAME_1);
        structuralVariant1.setNcbiBuild(TEST_NCBI_BUILD_1);
        structuralVariant1.setDnaSupport(TEST_DNA_SUPPORT_1);
        structuralVariant1.setRnaSupport(TEST_RNA_SUPPORT_1);
        structuralVariant1.setNormalReadCount(TEST_NORMAL_READ_COUNT_1);
        structuralVariant1.setTumorReadCount(TEST_TUMOR_READ_COUNT_1);
        structuralVariant1.setNormalVariantCount(TEST_NORMAL_VARIANT_COUNT_1);
        structuralVariant1.setTumorVariantCount(TEST_TUMOR_VARIANT_COUNT_1);
        structuralVariant1.setNormalPairedEndReadCount(TEST_NORMAL_PAIRED_END_READ_COUNT_1);
        structuralVariant1.setTumorPairedEndReadCount(TEST_TUMOR_PAIRED_END_READ_COUNT_1);
        structuralVariant1.setNormalSplitReadCount(TEST_NORMAL_SPLIT_READ_COUNT_1);
        structuralVariant1.setTumorSplitReadCount(TEST_TUMOR_SPLIT_READ_COUNT_1);
        structuralVariant1.setAnnotation(TEST_ANNOTATION_1);
        structuralVariant1.setBreakpointType(TEST_BREAKPOINT_TYPE_1);
        structuralVariant1.setConnectionType(TEST_CONNECTION_TYPE_1);
        structuralVariant1.setEventInfo(TEST_EVENT_INFO_1);
        structuralVariant1.setVariantClass(TEST_VARIANT_CLASS_1);
        structuralVariant1.setLength(TEST_LENGTH_1);
        structuralVariant1.setComments(TEST_COMMENTS_1);
        structuralVariant1.setDriverFilter(TEST_DRIVER_FILTER_1);
        structuralVariant1.setDriverFilterAnn(TEST_DRIVER_FILTER_ANN_1);
        structuralVariant1.setDriverTiersFilter(TEST_DRIVER_TIERS_FILTER_1);
        structuralVariant1.setDriverTiersFilterAnn(TEST_DRIVER_TIERS_FILTER_ANN_1);
        structuralVariant1.setSvStatus(TEST_SV_STATUS);
        structuralVariant1.setAnnotationJson(TEST_ANNOTATION_JSON_1);
        structuralVariantList.add(structuralVariant1);
        return structuralVariantList;
    }

    private StructuralVariantFilter createStructuralVariantFilterMolecularProfileId( ) {

        StructuralVariantFilter structuralVariantFilter = new StructuralVariantFilter();

        List<String> molecularProfileIds = new ArrayList<>();
        List<Integer> entrezGeneIds = new ArrayList<>();
        molecularProfileIds.add(TEST_GENETIC_PROFILE_STABLE_ID_1);
        entrezGeneIds.add(TEST_SITE1_ENTREZ_GENE_ID_1);

        structuralVariantFilter.setMolecularProfileIds(molecularProfileIds);
        structuralVariantFilter.setEntrezGeneIds(entrezGeneIds);
        return structuralVariantFilter;
    }

    private StructuralVariantFilter createStructuralVariantFilterSampleMolecularIdentifier( ) {

        StructuralVariantFilter structuralVariantFilter = new StructuralVariantFilter();

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(TEST_SITE1_ENTREZ_GENE_ID_1);

        List<SampleMolecularIdentifier> sampleMolecularIdentifierList = new ArrayList<>();
        SampleMolecularIdentifier sampleMolecularIdentifier1 = new SampleMolecularIdentifier();
        sampleMolecularIdentifier1.setSampleId(TEST_SAMPLE_ID_1);
        sampleMolecularIdentifier1.setMolecularProfileId(TEST_GENETIC_PROFILE_STABLE_ID_1);
        sampleMolecularIdentifierList.add(sampleMolecularIdentifier1);

        structuralVariantFilter.setEntrezGeneIds(entrezGeneIds);
        structuralVariantFilter.setSampleMolecularIdentifiers(sampleMolecularIdentifierList);
        return structuralVariantFilter;
    }

    private StructuralVariantFilter createStructuralVariantFilterMolecularProfileIdAndSampleMolecularIdentifier( ) {

        StructuralVariantFilter structuralVariantFilter = new StructuralVariantFilter();

        List<String> molecularProfileIds = new ArrayList<>();
        List<Integer> entrezGeneIds = new ArrayList<>();
        molecularProfileIds.add(TEST_GENETIC_PROFILE_STABLE_ID_1);
        entrezGeneIds.add(TEST_SITE1_ENTREZ_GENE_ID_1);

        List<SampleMolecularIdentifier> sampleMolecularIdentifierList = new ArrayList<>();
        SampleMolecularIdentifier sampleMolecularIdentifier1 = new SampleMolecularIdentifier();
        sampleMolecularIdentifier1.setSampleId(TEST_SAMPLE_ID_1);
        sampleMolecularIdentifier1.setMolecularProfileId(TEST_GENETIC_PROFILE_STABLE_ID_1);
        sampleMolecularIdentifierList.add(sampleMolecularIdentifier1);

        structuralVariantFilter.setMolecularProfileIds(molecularProfileIds);
        structuralVariantFilter.setEntrezGeneIds(entrezGeneIds);
        structuralVariantFilter.setSampleMolecularIdentifiers(sampleMolecularIdentifierList);
        return structuralVariantFilter;
    }

    private String createStructuralVariantFilterWithEntrezIdAndStructuralVariantJson( ) throws JsonProcessingException {

        StructuralVariantFilter structuralVariantFilter = new StructuralVariantFilter();
        
        List<SampleMolecularIdentifier> sampleMolecularIdentifierList = new ArrayList<>();
        structuralVariantFilter.setSampleMolecularIdentifiers(sampleMolecularIdentifierList);
        SampleMolecularIdentifier sampleMolecularIdentifier1 = new SampleMolecularIdentifier();
        sampleMolecularIdentifier1.setSampleId(TEST_SAMPLE_ID_1);
        sampleMolecularIdentifier1.setMolecularProfileId(TEST_GENETIC_PROFILE_STABLE_ID_1);
        sampleMolecularIdentifierList.add(sampleMolecularIdentifier1);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(TEST_SITE1_ENTREZ_GENE_ID_1);
        structuralVariantFilter.setEntrezGeneIds(entrezGeneIds);

        ObjectNode jsonTree = objectMapper.valueToTree(structuralVariantFilter);
        // Dummy entrez gene IDs:
        jsonTree.put("structuralVariantQueries", objectMapper.readTree(
            "[{\"gene1\": {\"entrezId\": 1},\"gene2\": {\"entrezId\":2}}]"
        ));
        return jsonTree.toString();
    }
    
    private String createStructuralVariantFilterWithEmptyStructuralVariantId( ) throws JsonProcessingException {

        StructuralVariantFilter structuralVariantFilter = new StructuralVariantFilter();
        
        List<SampleMolecularIdentifier> sampleMolecularIdentifierList = new ArrayList<>();
        structuralVariantFilter.setSampleMolecularIdentifiers(sampleMolecularIdentifierList);
        SampleMolecularIdentifier sampleMolecularIdentifier1 = new SampleMolecularIdentifier();
        sampleMolecularIdentifier1.setSampleId(TEST_SAMPLE_ID_1);
        sampleMolecularIdentifier1.setMolecularProfileId(TEST_GENETIC_PROFILE_STABLE_ID_1);
        sampleMolecularIdentifierList.add(sampleMolecularIdentifier1);

        ObjectNode jsonTree = objectMapper.valueToTree(structuralVariantFilter);
        // All struct var query fields empty:
        jsonTree.put("structuralVariantQueries", objectMapper.readTree(
            "[{\"gene1\": {\"entrezId\": null},\"gene2\": {\"entrezId\": null}}]"
        ));
        return jsonTree.toString();
    }

    private String createStructuralVariantFilterWithStructuralVariantWildcard( ) throws JsonProcessingException {

        StructuralVariantFilter structuralVariantFilter = new StructuralVariantFilter();
        
        List<SampleMolecularIdentifier> sampleMolecularIdentifierList = new ArrayList<>();
        structuralVariantFilter.setSampleMolecularIdentifiers(sampleMolecularIdentifierList);
        SampleMolecularIdentifier sampleMolecularIdentifier1 = new SampleMolecularIdentifier();
        sampleMolecularIdentifier1.setSampleId(TEST_SAMPLE_ID_1);
        sampleMolecularIdentifier1.setMolecularProfileId(TEST_GENETIC_PROFILE_STABLE_ID_1);
        sampleMolecularIdentifierList.add(sampleMolecularIdentifier1);
        
        ObjectNode jsonTree = objectMapper.valueToTree(structuralVariantFilter);
        // Replace with special wildcard and null oql values:
        jsonTree.put("structuralVariantQueries", objectMapper.readTree(
            "[" +
                "{\"gene1\": {\"specialValue\": \"ANY_GENE\"},\"gene2\": {\"entrezId\":2}}," +
                "{\"gene1\": {\"entrezId\":1}, \"gene2\": {\"specialValue\":\"NO_GENE\"}}" +
                "]"
        ));
        return jsonTree.toString();
    }
}
