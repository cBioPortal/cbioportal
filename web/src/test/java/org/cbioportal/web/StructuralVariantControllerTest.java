/*
 * Copyright (c) 2018 The Hyve B.V.
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

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.StructuralVariant;
import org.cbioportal.service.StructuralVariantService;
import org.cbioportal.web.parameter.SampleMolecularIdentifier;
import org.cbioportal.web.parameter.StructuralVariantFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class StructuralVariantControllerTest {

    private static final String TEST_GENETIC_PROFILE_STABLE_ID_1 = "test_genetic_profile_stable_id_1";
    private static final long TEST_STRUCTURAL_VARIANT_ID_1 = 1L;
    private static final int TEST_SAMPLE_ID_INTERNAL_1 = 1;
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
    private static final Integer TEST_SITE1_EXON_1 = 1;
    private static final String TEST_SITE1_CHROMOSOME_1 = "test_site1_chromosome_1";
    private static final Integer TEST_SITE1_POSITION_1 = 1;
    private static final String TEST_SITE1_DESCRIPTION_1 = "test_site1_description_1";
    private static final Integer TEST_SITE2_ENTREZ_GENE_ID_1 = 1;
    private static final String TEST_SITE2_HUGO_SYMBOL_1 = "test_site2_hugo_symbol_1";
    private static final String TEST_SITE2_ENSEMBL_TRANSCRIPT_ID_1 = "test_site2_ensembl_transcript_id_1";
    private static final Integer TEST_SITE2_EXON_1 = 1;
    private static final String TEST_SITE2_CHROMOSOME_1 = "test_site2_chromosome_1";
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
    private static final String TEST_CENTER_1 = "test_center_1";
    private static final String TEST_CONNECTION_TYPE_1 = "test_connection_type_1";
    private static final String TEST_EVENT_INFO_1 = "test_event_info_1";
    private static final String TEST_VARIANT_CLASS_1 = "test_variant_class_1";
    private static final Integer TEST_LENGTH_1 = 1;
    private static final String TEST_COMMENTS_1 = "test_comments_1";
    private static final String TEST_EXTERNAL_ANNOTATION_1 = "test_external_annotation_1";
    private static final String TEST_DRIVER_FILTER_1 = "test_driver_filter_1";
    private static final String TEST_DRIVER_FILTER_ANN_1 = "test_driver_filter_ann_1";
    private static final String TEST_DRIVER_TIERS_FILTER_1 = "test_driver_tiers_filter_1";
    private static final String TEST_DRIVER_TIERS_FILTER_ANN_1 = "test_driver_tiers_filter_ann_1";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private StructuralVariantService structuralVariantService;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public StructuralVariantService structuralVariantService() {
        return Mockito.mock(StructuralVariantService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(structuralVariantService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void fetchStructuralVariantsMolecularProfileId() throws Exception {

        List<StructuralVariant> structuralVariant = createExampleStructuralVariant();

        Mockito.when(structuralVariantService.fetchStructuralVariants(Mockito.anyList(),
                Mockito.anyList(), Mockito.anyList())).thenReturn(structuralVariant);

        StructuralVariantFilter structuralVariantFilter = createStructuralVariantFilterMolecularProfileId();

        mockMvc.perform(MockMvcRequestBuilders.post("/structuralvariant/fetch")
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(structuralVariantFilter)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].structuralVariantId").value((int) TEST_STRUCTURAL_VARIANT_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleIdInternal").value(TEST_SAMPLE_ID_INTERNAL_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_PATIENT_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].uniqueSampleKey").value(TEST_UNIQUE_SAMPLE_KEY_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].uniquePatientKey").value(TEST_UNIQUE_PATIENT_KEY_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1EntrezGeneId").value((int) TEST_SITE1_ENTREZ_GENE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1HugoSymbol").value(TEST_SITE1_HUGO_SYMBOL_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1EnsemblTranscriptId").value(TEST_SITE1_ENSEMBL_TRANSCRIPT_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Exon").value(TEST_SITE1_EXON_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Chromosome").value(TEST_SITE1_CHROMOSOME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Position").value(TEST_SITE1_POSITION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Description").value(TEST_SITE1_DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2EntrezGeneId").value((int) TEST_SITE2_ENTREZ_GENE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2HugoSymbol").value(TEST_SITE2_HUGO_SYMBOL_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2EnsemblTranscriptId").value(TEST_SITE2_ENSEMBL_TRANSCRIPT_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Exon").value(TEST_SITE2_EXON_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Chromosome").value(TEST_SITE2_CHROMOSOME_1))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].center").value(TEST_CENTER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].connectionType").value(TEST_CONNECTION_TYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].eventInfo").value(TEST_EVENT_INFO_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantClass").value(TEST_VARIANT_CLASS_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].length").value(TEST_LENGTH_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].comments").value(TEST_COMMENTS_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].externalAnnotation").value(TEST_EXTERNAL_ANNOTATION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilter").value(TEST_DRIVER_FILTER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilterAnn").value(TEST_DRIVER_FILTER_ANN_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilter").value(TEST_DRIVER_TIERS_FILTER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilterAnn").value(TEST_DRIVER_TIERS_FILTER_ANN_1));
    }

    @Test
    public void fetchStructuralVariantsSampleMolecularIdentifier() throws Exception {

        List<StructuralVariant> structuralVariant = createExampleStructuralVariant();

        Mockito.when(structuralVariantService.fetchStructuralVariants(Mockito.anyList(),
                Mockito.anyList(), Mockito.anyList())).thenReturn(structuralVariant);

        StructuralVariantFilter structuralVariantFilter = createStructuralVariantFilterSampleMolecularIdentifier();

        mockMvc.perform(MockMvcRequestBuilders.post("/structuralvariant/fetch")
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(structuralVariantFilter)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value(TEST_GENETIC_PROFILE_STABLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].structuralVariantId").value((int) TEST_STRUCTURAL_VARIANT_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleIdInternal").value(TEST_SAMPLE_ID_INTERNAL_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_PATIENT_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].uniqueSampleKey").value(TEST_UNIQUE_SAMPLE_KEY_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].uniquePatientKey").value(TEST_UNIQUE_PATIENT_KEY_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1EntrezGeneId").value((int) TEST_SITE1_ENTREZ_GENE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1HugoSymbol").value(TEST_SITE1_HUGO_SYMBOL_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1EnsemblTranscriptId").value(TEST_SITE1_ENSEMBL_TRANSCRIPT_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Exon").value(TEST_SITE1_EXON_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Chromosome").value(TEST_SITE1_CHROMOSOME_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Position").value(TEST_SITE1_POSITION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Description").value(TEST_SITE1_DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2EntrezGeneId").value((int) TEST_SITE2_ENTREZ_GENE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2HugoSymbol").value(TEST_SITE2_HUGO_SYMBOL_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2EnsemblTranscriptId").value(TEST_SITE2_ENSEMBL_TRANSCRIPT_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Exon").value(TEST_SITE2_EXON_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Chromosome").value(TEST_SITE2_CHROMOSOME_1))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].center").value(TEST_CENTER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].connectionType").value(TEST_CONNECTION_TYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].eventInfo").value(TEST_EVENT_INFO_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantClass").value(TEST_VARIANT_CLASS_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].length").value(TEST_LENGTH_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].comments").value(TEST_COMMENTS_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].externalAnnotation").value(TEST_EXTERNAL_ANNOTATION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilter").value(TEST_DRIVER_FILTER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverFilterAnn").value(TEST_DRIVER_FILTER_ANN_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilter").value(TEST_DRIVER_TIERS_FILTER_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].driverTiersFilterAnn").value(TEST_DRIVER_TIERS_FILTER_ANN_1));
    }

    @Test
    public void fetchStructuralVariantsBothMolecularProfileIdAndSampleMolecularIdentifier() throws Exception {

        List<StructuralVariant> structuralVariant = createExampleStructuralVariant();

        Mockito.when(structuralVariantService.fetchStructuralVariants(Mockito.anyList(),
                Mockito.anyList(), Mockito.anyList())).thenReturn(structuralVariant);

        StructuralVariantFilter structuralVariantFilter = createStructuralVariantFilterMolecularProfileIdAndSampleMolecularIdentifier();

        mockMvc.perform(MockMvcRequestBuilders.post("/structuralvariant/fetch")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(structuralVariantFilter)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("interceptedStructuralVariantFilter must be true"));
    }

    private List<StructuralVariant> createExampleStructuralVariant() {

        List<StructuralVariant> structuralVariantList = new ArrayList<>();
        StructuralVariant structuralVariant1 = new StructuralVariant();
        structuralVariant1.setMolecularProfileId(TEST_GENETIC_PROFILE_STABLE_ID_1);
        structuralVariant1.setStructuralVariantId(TEST_STRUCTURAL_VARIANT_ID_1);
        structuralVariant1.setSampleIdInternal(TEST_SAMPLE_ID_INTERNAL_1);
        structuralVariant1.setSampleId(TEST_SAMPLE_ID_1);
        structuralVariant1.setPatientId(TEST_PATIENT_ID_1);
        structuralVariant1.setStudyId(TEST_STUDY_ID_1);
        structuralVariant1.setUniqueSampleKey(TEST_UNIQUE_SAMPLE_KEY_1);
        structuralVariant1.setUniquePatientKey(TEST_UNIQUE_PATIENT_KEY_1);
        structuralVariant1.setSite1EntrezGeneId(TEST_SITE1_ENTREZ_GENE_ID_1);
        structuralVariant1.setSite1HugoSymbol(TEST_SITE1_HUGO_SYMBOL_1);
        structuralVariant1.setSite1EnsemblTranscriptId(TEST_SITE1_ENSEMBL_TRANSCRIPT_ID_1);
        structuralVariant1.setSite1Exon(TEST_SITE1_EXON_1);
        structuralVariant1.setSite1Chromosome(TEST_SITE1_CHROMOSOME_1);
        structuralVariant1.setSite1Position(TEST_SITE1_POSITION_1);
        structuralVariant1.setSite1Description(TEST_SITE1_DESCRIPTION_1);
        structuralVariant1.setSite2EntrezGeneId(TEST_SITE2_ENTREZ_GENE_ID_1);
        structuralVariant1.setSite2HugoSymbol(TEST_SITE2_HUGO_SYMBOL_1);
        structuralVariant1.setSite2EnsemblTranscriptId(TEST_SITE2_ENSEMBL_TRANSCRIPT_ID_1);
        structuralVariant1.setSite2Exon(TEST_SITE2_EXON_1);
        structuralVariant1.setSite2Chromosome(TEST_SITE2_CHROMOSOME_1);
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
        structuralVariant1.setCenter(TEST_CENTER_1);
        structuralVariant1.setConnectionType(TEST_CONNECTION_TYPE_1);
        structuralVariant1.setEventInfo(TEST_EVENT_INFO_1);
        structuralVariant1.setVariantClass(TEST_VARIANT_CLASS_1);
        structuralVariant1.setLength(TEST_LENGTH_1);
        structuralVariant1.setComments(TEST_COMMENTS_1);
        structuralVariant1.setExternalAnnotation(TEST_EXTERNAL_ANNOTATION_1);
        structuralVariant1.setDriverFilter(TEST_DRIVER_FILTER_1);
        structuralVariant1.setDriverFilterAnn(TEST_DRIVER_FILTER_ANN_1);
        structuralVariant1.setDriverTiersFilter(TEST_DRIVER_TIERS_FILTER_1);
        structuralVariant1.setDriverTiersFilterAnn(TEST_DRIVER_TIERS_FILTER_ANN_1);
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
}
