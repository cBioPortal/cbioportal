/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
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

package org.cbioportal.weblegacy;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.Gene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Patient;
import org.cbioportal.model.Sample;
import org.mskcc.cbio.portal.model.StructuralVariant;
import org.mskcc.cbio.portal.service.StructuralVariantService;
import org.cbioportal.web.config.CustomObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {StructuralVariantControllerTestConfig.class, CustomObjectMapper.class})
public class StructuralVariantControllerTest {
    @Autowired
    private StructuralVariantService structuralVariantServiceMock;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    private static List<StructuralVariant> structuralVariantDataServiceFullResponseMock;
    private static List<StructuralVariant> structuralVariantDataServiceEmptyResponseMock;

    @Before
    public void setup() {
        Mockito.reset(structuralVariantServiceMock);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void svDataTest1() throws Exception {
        List<StructuralVariant> mockResponse = getStructuralVariantDataServiceFullResponseMock();
        Mockito.when(structuralVariantServiceMock.getStructuralVariant(
                org.mockito.Matchers.anyListOf(String.class),
                org.mockito.Matchers.anyListOf(String.class),
                org.mockito.Matchers.anyListOf(String.class)
        )).thenReturn(mockResponse);
        ResultActions resultActions = this.mockMvc.perform(
                MockMvcRequestBuilders.get("/structuralvariant")
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                .param("geneticProfileStableIds", "7")
                .param("hugoGeneSymbols", "ERBB2,GRB7")
                .param("sampleStableIds", "TCGA-A1-A0SB-01,TCGA-A1-A0SD-01,TCGA-A1-A0SE-01")
        )
        // note: an attempt was made to test the proper parsing of various parameter combinations, but strings are not parsed until the request is performed
        //.andDo(MockMvcResultHandlers.print());
        ;
        testFullResponse(resultActions);
    }

    @Test
    public void svDataTest2() throws Exception {
        List <StructuralVariant> mockResponse = getStructuralVariantDataServiceEmptyResponseMock();
        Mockito.when(structuralVariantServiceMock.getStructuralVariant(
                org.mockito.Matchers.anyListOf(String.class),
                org.mockito.Matchers.anyListOf(String.class),
                org.mockito.Matchers.anyListOf(String.class)
        )).thenReturn(mockResponse);
        ResultActions resultActions = this.mockMvc.perform(
                MockMvcRequestBuilders.get("/structuralvariant")
                .accept("application/json;charset=UTF-8")
                .param("geneticProfileStableIds", "7")
                .param("hugoGeneSymbols", "unrecognized_gene_identifier")
                .param("sampleStableIds", "")
        );
        testEmptyResponse(resultActions);
    }

    private void testFullResponse(ResultActions resultActions) throws Exception {
        resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].breakpointType").value("PRECISE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].annotation").value("n/a"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].comments").value("n/a"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].confidenceClass").value("AUTO_OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].connectionType").value("3to5"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].eventInfo").value("Transcript fusion (ERBB2-GRB7)"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].mapq").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalReadCount").value(7181))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].normalVariantCount").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].pairedEndReadSupport").value(11))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Chrom").value("17"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Desc").value("Intron of ERBB2(+): 51bp after exon 26"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Gene").value("ERBB2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1Pos").value(37883851))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Chrom").value("17"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Desc").value("5-UTR of GRB7(+): 1Kb before coding start"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Gene").value("GRB7"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2Pos").value(37897379))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].splitReadSupport").value(31))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].svClassName").value("DELETION"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].svDesc").value("n/a"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].svLength").value(13528))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorReadCount").value(4389))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].tumorVariantCount").value(6))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].variantStatusName").value("NEW_VARIANT"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene1.hugoGeneSymbol").value("ERBB2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene1.entrezGeneId").value(2064))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene2.hugoGeneSymbol").value("GRB7"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene2.entrezGeneId").value(2886))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sample.stableId").value("TCGA-A1-A0SB-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].breakpointType").value("PRECISE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].annotation").value("n/a"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].comments").value("n/a"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].confidenceClass").value("AUTO_OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].connectionType").value("3to5"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].eventInfo").value("Protein fusion: mid-exon (ERBB2-GRB7)"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].mapq").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].normalReadCount").value(7062))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].normalVariantCount").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].pairedEndReadSupport").value(97))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].site1Chrom").value("17"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].site1Desc").value("Exon 25 of ERBB2(+)"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].site1Gene").value("ERBB2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].site1Pos").value(37883138))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].site2Chrom").value("17"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].site2Desc").value("Intron of GRB7(+): 56bp before exon 10"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].site2Gene").value("GRB7"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].site2Pos").value(37901416))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].splitReadSupport").value(71))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].svClassName").value("DELETION"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].svDesc").value("n/a"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].svLength").value(18278))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].tumorReadCount").value(9849))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].tumorVariantCount").value(60))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].variantStatusName").value("NEW_VARIANT"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene1.hugoGeneSymbol").value("ERBB2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene1.entrezGeneId").value(2064))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene2.hugoGeneSymbol").value("GRB7"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].gene2.entrezGeneId").value(2886))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sample.stableId").value("TCGA-A1-A0SD-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].breakpointType").value("PRECISE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].annotation").value("n/a"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].comments").value("ERBB2 (NM_004448) rearrangement"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].confidenceClass").value("MANUAL_OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].connectionType").value("3to5"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].eventInfo").value("Deletion of 1 exon: in frame"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].mapq").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].normalReadCount").value(7212))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].normalVariantCount").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].pairedEndReadSupport").value(31))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].site1Chrom").value("17"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].site1Desc").value("Intron of ERBB2(+): 46bp after exon 15"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].site1Gene").value("ERBB2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].site1Pos").value(37873779))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].site2Chrom").value("17"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].site2Desc").value("Intron of ERBB2(+): 501bp before exon 17"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].site2Gene").value("ERBB2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].site2Pos").value(37879041))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].splitReadSupport").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].svClassName").value("DELETION"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].svDesc").value("n/a"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].svLength").value(5262))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].tumorReadCount").value(3101))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].tumorVariantCount").value(7))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].variantStatusName").value("NEW_VARIANT"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].gene1.hugoGeneSymbol").value("ERBB2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].gene1.entrezGeneId").value(2064))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].gene2.hugoGeneSymbol").value("ERBB2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].gene2.entrezGeneId").value(2064))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].sample.stableId").value("TCGA-A1-A0SE-01"))
                ;
    }

    private void testEmptyResponse(ResultActions resultActions) throws Exception {
        resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)))
                ;
    }

    private List<StructuralVariant> getStructuralVariantDataServiceFullResponseMock() {
        if(structuralVariantDataServiceFullResponseMock != null) {
            return structuralVariantDataServiceFullResponseMock;
        }
        //data from persistence-mybatis-test test
        structuralVariantDataServiceFullResponseMock = new ArrayList<>();
        CancerStudy cancerStudy1 = new CancerStudy();
        cancerStudy1.setCancerStudyId(1);
        cancerStudy1.setCancerStudyIdentifier("study_tcga_pub");
        cancerStudy1.setTypeOfCancerId("brca");
        cancerStudy1.setName("Breast Invasive Carcinoma (TCGA, Nature 2012)");
        cancerStudy1.setShortName("BRCA (TCGA)");
        cancerStudy1.setDescription("<a href=\"http://cancergenome.nih.gov/\">The Cancer Genome Atlas (TCGA)</a> Breast Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\"http://tcga-data.nci.nih.gov/tcga/\">Raw data via the TCGA Data Portal</a>");
        cancerStudy1.setPublicStudy(true);
        cancerStudy1.setPmid("23000897");
        cancerStudy1.setCitation("TCGA, Nature 2012");
        cancerStudy1.setGroups("PUBLIC");
        cancerStudy1.setStatus(0);
        cancerStudy1.setImportDate(null);
        Patient patient1 = new Patient();
        patient1.setCancerStudy(cancerStudy1);
        patient1.setCancerStudyId(1);
        patient1.setInternalId(1);
        patient1.setStableId("TCGA-A1-A0SB");
        Patient patient2 = new Patient();
        patient2.setCancerStudy(cancerStudy1);
        patient2.setCancerStudyId(1);
        patient2.setInternalId(2);
        patient2.setStableId("TCGA-A1-A0SD");
        Patient patient3 = new Patient();
        patient3.setCancerStudy(cancerStudy1);
        patient3.setCancerStudyId(1);
        patient3.setInternalId(3);
        patient3.setStableId("TCGA-A1-A0SE");
        Sample sample1 = new Sample();
        sample1.setInternalId(1);
        sample1.setStableId("TCGA-A1-A0SB-01");
        sample1.setSampleType(Sample.SampleType.PRIMARY_SOLID_TUMOR);
        sample1.setPatientId(1);
        sample1.setPatient(patient1);
        Sample sample2 = new Sample();
        sample2.setInternalId(2);
        sample2.setStableId("TCGA-A1-A0SD-01");
        sample2.setSampleType(Sample.SampleType.PRIMARY_SOLID_TUMOR);
        sample2.setPatientId(2);
        sample2.setPatient(patient2);
        Sample sample3 = new Sample();
        sample3.setInternalId(3);
        sample3.setStableId("TCGA-A1-A0SE-01");
        sample3.setSampleType(Sample.SampleType.PRIMARY_SOLID_TUMOR);
        sample3.setPatientId(3);
        sample3.setPatient(patient3);
        MolecularProfile geneticProfile1 = new MolecularProfile();
        geneticProfile1.setMolecularProfileId(7);
        geneticProfile1.setStableId("study_tcga_pub_sv");
        geneticProfile1.setCancerStudy(cancerStudy1);
        geneticProfile1.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.STRUCTURAL_VARIANT);
        geneticProfile1.setDatatype("SV");
        geneticProfile1.setName("Structural Variants");
        geneticProfile1.setDescription("Structural Variants detected by Illumina HiSeq sequencing.");
        geneticProfile1.setShowProfileInAnalysisTab(true);
        Gene geneERBB2 = new Gene();
        geneERBB2.setHugoGeneSymbol("ERBB2");
        geneERBB2.setEntrezGeneId(2064);
        geneERBB2.setCytoband("17q12");
        geneERBB2.setLength(10321);
        geneERBB2.setType("protein-coding");
        Gene geneGRB7 = new Gene();
        geneGRB7.setHugoGeneSymbol("GRB7");
        geneGRB7.setEntrezGeneId(2886);
        geneGRB7.setType("protein-coding");
        geneGRB7.setCytoband("17q12");
        geneGRB7.setLength(3597);
        StructuralVariant structuralVariant1 = new StructuralVariant();
        structuralVariant1.setSampleId(1);
        structuralVariant1.setSample(sample1);
        structuralVariant1.setBreakpointType("PRECISE");
        structuralVariant1.setAnnotation("n/a");
        structuralVariant1.setComments("n/a");
        structuralVariant1.setConfidenceClass("AUTO_OK");
        structuralVariant1.setConnectionType("3to5");
        structuralVariant1.setEventInfo("Transcript fusion (ERBB2-GRB7)");
        structuralVariant1.setMapq("0");
        structuralVariant1.setNormalReadCount(7181);
        structuralVariant1.setNormalVariantCount(0);
        structuralVariant1.setPairedEndReadSupport(11);
        structuralVariant1.setSite1Chrom("17");
        structuralVariant1.setSite1Desc("Intron of ERBB2(+): 51bp after exon 26");
        structuralVariant1.setSite1Gene("ERBB2");
        structuralVariant1.setSite1Pos(37883851);
        structuralVariant1.setGene1(geneERBB2);
        structuralVariant1.setSite2Chrom("17");
        structuralVariant1.setSite2Desc("5-UTR of GRB7(+): 1Kb before coding start");
        structuralVariant1.setSite2Gene("GRB7");
        structuralVariant1.setSite2Pos(37897379);
        structuralVariant1.setGene2(geneGRB7);
        structuralVariant1.setSplitReadSupport(31);
        structuralVariant1.setSvClassName("DELETION");
        structuralVariant1.setSvDesc("n/a");
        structuralVariant1.setSvLength(13528);
        structuralVariant1.setTumorReadCount(4389);
        structuralVariant1.setTumorVariantCount(6);
        structuralVariant1.setVariantStatusName("NEW_VARIANT");
        structuralVariant1.setGeneticProfileId(7);
        structuralVariant1.setGeneticProfile(geneticProfile1);
        structuralVariantDataServiceFullResponseMock.add(structuralVariant1);
        StructuralVariant structuralVariant2 = new StructuralVariant();
        structuralVariant2.setSampleId(2);
        structuralVariant2.setSample(sample2);
        structuralVariant2.setBreakpointType("PRECISE");
        structuralVariant2.setAnnotation("n/a");
        structuralVariant2.setComments("n/a");
        structuralVariant2.setConfidenceClass("AUTO_OK");
        structuralVariant2.setConnectionType("3to5");
        structuralVariant2.setEventInfo("Protein fusion: mid-exon (ERBB2-GRB7)");
        structuralVariant2.setMapq("0");
        structuralVariant2.setNormalReadCount(7062);
        structuralVariant2.setNormalVariantCount(0);
        structuralVariant2.setPairedEndReadSupport(97);
        structuralVariant2.setSite1Chrom("17");
        structuralVariant2.setSite1Desc("Exon 25 of ERBB2(+)");
        structuralVariant2.setSite1Gene("ERBB2");
        structuralVariant2.setSite1Pos(37883138);
        structuralVariant2.setGene1(geneERBB2);
        structuralVariant2.setSite2Chrom("17");
        structuralVariant2.setSite2Desc("Intron of GRB7(+): 56bp before exon 10");
        structuralVariant2.setSite2Gene("GRB7");
        structuralVariant2.setSite2Pos(37901416);
        structuralVariant2.setGene2(geneGRB7);
        structuralVariant2.setSplitReadSupport(71);
        structuralVariant2.setSvClassName("DELETION");
        structuralVariant2.setSvDesc("n/a");
        structuralVariant2.setSvLength(18278);
        structuralVariant2.setTumorReadCount(9849);
        structuralVariant2.setTumorVariantCount(60);
        structuralVariant2.setVariantStatusName("NEW_VARIANT");
        structuralVariant2.setGeneticProfileId(7);
        structuralVariant2.setGeneticProfile(geneticProfile1);
        structuralVariantDataServiceFullResponseMock.add(structuralVariant2);
        StructuralVariant structuralVariant3 = new StructuralVariant();
        structuralVariant3.setSampleId(3);
        structuralVariant3.setSample(sample3);
        structuralVariant3.setBreakpointType("PRECISE");
        structuralVariant3.setAnnotation("n/a");
        structuralVariant3.setComments("ERBB2 (NM_004448) rearrangement");
        structuralVariant3.setConfidenceClass("MANUAL_OK");
        structuralVariant3.setConnectionType("3to5");
        structuralVariant3.setEventInfo("Deletion of 1 exon: in frame");
        structuralVariant3.setMapq("0");
        structuralVariant3.setNormalReadCount(7212);
        structuralVariant3.setNormalVariantCount(0);
        structuralVariant3.setPairedEndReadSupport(31);
        structuralVariant3.setSite1Chrom("17");
        structuralVariant3.setSite1Desc("Intron of ERBB2(+): 46bp after exon 15");
        structuralVariant3.setSite1Gene("ERBB2");
        structuralVariant3.setSite1Pos(37873779);
        structuralVariant3.setGene1(geneERBB2);
        structuralVariant3.setSite2Chrom("17");
        structuralVariant3.setSite2Desc("Intron of ERBB2(+): 501bp before exon 17");
        structuralVariant3.setSite2Gene("ERBB2");
        structuralVariant3.setSite2Pos(37879041);
        structuralVariant3.setGene2(geneERBB2);
        structuralVariant3.setSplitReadSupport(10);
        structuralVariant3.setSvClassName("DELETION");
        structuralVariant3.setSvDesc("n/a");
        structuralVariant3.setSvLength(5262);
        structuralVariant3.setTumorReadCount(3101);
        structuralVariant3.setTumorVariantCount(7);
        structuralVariant3.setVariantStatusName("NEW_VARIANT");
        structuralVariant3.setGeneticProfileId(7);
        structuralVariant3.setGeneticProfile(geneticProfile1);
        structuralVariantDataServiceFullResponseMock.add(structuralVariant3);
        //mask out values not normally returned by service layer
        cancerStudy1.setCancerStudyId(null);
        cancerStudy1.setTypeOfCancerId(null);
        cancerStudy1.setCancerStudyIdentifier(null);
        cancerStudy1.setName(null);
        cancerStudy1.setShortName(null);
        cancerStudy1.setDescription(null);
        cancerStudy1.setPublicStudy(null);
        cancerStudy1.setPmid(null);
        cancerStudy1.setCitation(null);
        cancerStudy1.setGroups(null);
        cancerStudy1.setStatus(null);
        cancerStudy1.setImportDate(null);
        patient1.setCancerStudyId(null);
        patient1.setInternalId(null);
        patient1.setStableId(null);
        patient2.setCancerStudyId(null);
        patient2.setInternalId(null);
        patient2.setStableId(null);
        patient3.setCancerStudyId(null);
        patient3.setInternalId(null);
        patient3.setStableId(null);
        sample1.setInternalId(null);
        sample1.setSampleType(null);
        sample1.setPatientId(null);
        sample2.setInternalId(null);
        sample2.setSampleType(null);
        sample2.setPatientId(null);
        sample3.setInternalId(null);
        sample3.setSampleType(null);
        sample3.setPatientId(null);
        geneticProfile1.setMolecularProfileId(null);
        geneticProfile1.setStableId(null);
        geneticProfile1.setMolecularAlterationType(null);
        geneticProfile1.setDatatype(null);
        geneticProfile1.setName(null);
        geneticProfile1.setDescription(null);
        geneticProfile1.setShowProfileInAnalysisTab(null);
        geneERBB2.setCytoband(null);
        geneERBB2.setLength(null);
        geneERBB2.setType(null);
        geneGRB7.setType(null);
        geneGRB7.setCytoband(null);
        geneGRB7.setLength(null);
        structuralVariant1.setGeneticProfileId(null);
        structuralVariant2.setGeneticProfileId(null);
        structuralVariant3.setGeneticProfileId(null);
        return structuralVariantDataServiceFullResponseMock;
    }

    private List<StructuralVariant> getStructuralVariantDataServiceEmptyResponseMock() {
        if(structuralVariantDataServiceEmptyResponseMock != null) {
            return structuralVariantDataServiceEmptyResponseMock;
        }
        structuralVariantDataServiceEmptyResponseMock = new ArrayList<>();
        return structuralVariantDataServiceEmptyResponseMock;
    }
}
