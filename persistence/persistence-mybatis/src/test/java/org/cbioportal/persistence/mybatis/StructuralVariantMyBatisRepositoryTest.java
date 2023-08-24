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

package org.cbioportal.persistence.mybatis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.StructuralVariantFilterQuery;
import org.cbioportal.model.StructuralVariant;
import org.cbioportal.model.StructuralVariantGeneSubQuery;
import org.cbioportal.model.StructuralVariantQuery;
import org.cbioportal.model.StructuralVariantSpecialValue;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.cbioportal.persistence.mybatis.util.MolecularProfileCaseIdentifierUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {StructuralVariantMyBatisRepository.class, StructuralVariantMapper.class,
    MolecularProfileCaseIdentifierUtil.class, TestConfig.class})
public class StructuralVariantMyBatisRepositoryTest {

    //    struct var events in testSql.sql
    //        SAMPLE_ID,        ENTREZ_GENE_ID, HUGO_GENE_SYMBOL, GENETIC_PROFILE_ID, GENETIC PRODFILE, TYPE, MUTATION_TYPE, DRIVER_FILTER, DRIVER_TIERS_FILTER, PATIENT_ID, MUTATION_TYPE
    //        1     27436-238   EML4-ALK    7   study_tcga_pub_sv   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    germline
    //        2     27436-238   EML4-ALK    7   study_tcga_pub_sv   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SD    somatic
    //        1     57670-673   KIAA..-BRAF 7   study_tcga_pub_sv   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    somatic
    //        2     57670-673   KIAA..-BRAF 7   study_tcga_pub_sv   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SD    germline
    //        2     57670-673   KIAA..-BRAF 7   study_tcga_pub_sv   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SD    somatic
    //       15     57670-673   KIAA..-BRAF 13  acc_tcga_sv         SV          Fusion              <noi>               <noi>   TCGA-A1-A0SD    somatic
    //        1     8031-5979   NCOA4-RET   7   study_tcga_pub_sv   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    somatic
    //       15     8031-5979   NCOA4-RET   13  acc_tcga_sv         SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    somatic
    //        1     7113-2078   TMPRSS2-ERG 7   study_tcga_pub_sv   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    somatic
    //       15     8031-NULL   NCOA4-      13  acc_tcga_sv        SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    somatic


    @Autowired
    StructuralVariantMyBatisRepository structuralVariantMyBatisRepository;

    @Before
    public void init() {
        molecularProfileIds = new ArrayList<>();
        molecularProfileIds.add("study_tcga_pub_sv");
        molecularProfileIds.add("study_tcga_pub_sv");
        molecularProfileIds.add("acc_tcga_sv");
        sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-A0SD-01");
        sampleIds.add("TCGA-A1-B0SO-01");
        tiers = Select.all();
        includeUnknownTier = true;
        includeDriver = true;
        includeVUS = true;
        includeUnknownOncogenicity = true;
        includeGermline = true;
        includeSomatic = true;
        includeUnknownStatus = true;
        
        GeneFilterQuery geneFilterQuery1 = new GeneFilterQuery("KIAA1549", 57670, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery2 = new GeneFilterQuery("EML4", 27436, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery3 = new GeneFilterQuery("TMPRSS2", 7113, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery4 = new GeneFilterQuery("NCOA4", 8031, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        geneQueries =  Arrays.asList(geneFilterQuery1, geneFilterQuery2, geneFilterQuery3, geneFilterQuery4);

        // Only search for the KIAA1549-BRAF and EML4-ALK fusions.
        structVarFilterQuery1 = new StructuralVariantFilterQuery("KIAA1549", 57670, "BRAF", 673, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        structVarFilterQuery2 = new StructuralVariantFilterQuery("EML4", 27436, "ALK", 238, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        structVarQueries = Arrays.asList(structVarFilterQuery1, structVarFilterQuery2);
        structVarFilterQueryNullGene1 = new StructuralVariantFilterQuery(null, null, "BRAF", 673, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        structVarFilterQueryNullGene2 = new StructuralVariantFilterQuery("NCOA4", 8031, null, null, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
    }

    List<String> molecularProfileIds = new ArrayList<>();
    List<String> sampleIds = new ArrayList<>();
    Select<String> tiers;
    boolean includeUnknownTier;
    boolean includeDriver;
    boolean includeVUS;
    boolean includeUnknownOncogenicity;
    boolean includeGermline;
    boolean includeSomatic;
    boolean includeUnknownStatus;
    private List<GeneFilterQuery> geneQueries;
    List<Integer> noEntrezIds = Collections.emptyList();
    private List<StructuralVariantQuery> noStructVars = Collections.emptyList();
    private List<StructuralVariantFilterQuery> structVarQueries;
    private StructuralVariantFilterQuery structVarFilterQuery1;
    private StructuralVariantFilterQuery structVarFilterQuery2;
    private StructuralVariantFilterQuery structVarFilterQueryNullGene1;
    private StructuralVariantFilterQuery structVarFilterQueryNullGene2;

    @Test
    public void fetchStructuralVariantsNoSampleIdentifiers() {

        List<String> molecularProfileIds = new ArrayList<>();
        List<Integer> entrezGeneIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        molecularProfileIds.add("study_tcga_pub_sv");
        entrezGeneIds.add(57670);

        List<StructuralVariant> result = 
                structuralVariantMyBatisRepository.fetchStructuralVariants(molecularProfileIds,
                    sampleIds, entrezGeneIds, noStructVars);

        Assert.assertEquals(3,  result.size());
        StructuralVariant structuralVariantFirstResult = result.get(0);
        Assert.assertEquals("study_tcga_pub_sv", structuralVariantFirstResult.getMolecularProfileId());
        Assert.assertEquals((String) "TCGA-A1-A0SB-01", structuralVariantFirstResult.getSampleId());
        Assert.assertEquals((String) "TCGA-A1-A0SB", structuralVariantFirstResult.getPatientId());
        Assert.assertEquals((String) "study_tcga_pub", structuralVariantFirstResult.getStudyId());
        Assert.assertEquals((Integer) 57670, structuralVariantFirstResult.getSite1EntrezGeneId());
        Assert.assertEquals("KIAA1549", structuralVariantFirstResult.getSite1HugoSymbol());
        Assert.assertEquals("ENST00000242365", structuralVariantFirstResult.getSite1EnsemblTranscriptId());
        Assert.assertEquals("7", structuralVariantFirstResult.getSite1Chromosome());
        Assert.assertEquals((Integer) 138536968, structuralVariantFirstResult.getSite1Position());
        Assert.assertEquals("KIAA1549-BRAF.K16B10.COSF509_1", structuralVariantFirstResult.getSite1Description());
        Assert.assertEquals((Integer) 673, structuralVariantFirstResult.getSite2EntrezGeneId());
        Assert.assertEquals("BRAF", structuralVariantFirstResult.getSite2HugoSymbol());
        Assert.assertEquals("ENST00000288602", structuralVariantFirstResult.getSite2EnsemblTranscriptId());
        Assert.assertEquals("7", structuralVariantFirstResult.getSite2Chromosome());
        Assert.assertEquals((Integer) 140482957, structuralVariantFirstResult.getSite2Position());
        Assert.assertEquals("KIAA1549-BRAF.K16B10.COSF509_2", structuralVariantFirstResult.getSite2Description());
        Assert.assertEquals(null, structuralVariantFirstResult.getSite2EffectOnFrame());
        Assert.assertEquals("GRCh37", structuralVariantFirstResult.getNcbiBuild());
        Assert.assertEquals("no", structuralVariantFirstResult.getDnaSupport());
        Assert.assertEquals("yes", structuralVariantFirstResult.getRnaSupport());
        Assert.assertEquals(null, structuralVariantFirstResult.getNormalReadCount());
        Assert.assertEquals((Integer) 100000, structuralVariantFirstResult.getTumorReadCount());
        Assert.assertEquals(null, structuralVariantFirstResult.getNormalVariantCount());
        Assert.assertEquals((Integer) 90000, structuralVariantFirstResult.getTumorVariantCount());
        Assert.assertEquals(null, structuralVariantFirstResult.getNormalPairedEndReadCount());
        Assert.assertEquals(null, structuralVariantFirstResult.getTumorPairedEndReadCount());
        Assert.assertEquals(null, structuralVariantFirstResult.getNormalSplitReadCount());
        Assert.assertEquals(null, structuralVariantFirstResult.getTumorSplitReadCount());
        Assert.assertEquals("KIAA1549-BRAF.K16B10.COSF509", structuralVariantFirstResult.getAnnotation());
        Assert.assertEquals(null, structuralVariantFirstResult.getBreakpointType());
        Assert.assertEquals(null, structuralVariantFirstResult.getConnectionType());
        Assert.assertEquals("Fusion", structuralVariantFirstResult.getEventInfo());
        Assert.assertEquals(null, structuralVariantFirstResult.getVariantClass());
        Assert.assertEquals(null, structuralVariantFirstResult.getLength());
        Assert.assertEquals("Gain-of-Function", structuralVariantFirstResult.getComments());
        Assert.assertEquals("Putative_Passenger", structuralVariantFirstResult.getDriverFilter());
        Assert.assertEquals("Pathogenic", structuralVariantFirstResult.getDriverFilterAnn());
        Assert.assertEquals("Tier 1", structuralVariantFirstResult.getDriverTiersFilter());
        Assert.assertEquals("Potentially Actionable", structuralVariantFirstResult.getDriverTiersFilterAnn());
        Assert.assertEquals("SOMATIC", structuralVariantFirstResult.getSvStatus());
        StructuralVariant structuralVariantSecondResult = result.get(1);
        Assert.assertEquals("study_tcga_pub_sv", structuralVariantSecondResult.getMolecularProfileId());
        Assert.assertEquals((String) "TCGA-A1-A0SD-01", structuralVariantSecondResult.getSampleId());
        Assert.assertEquals((String) "TCGA-A1-A0SD", structuralVariantSecondResult.getPatientId());
        Assert.assertEquals((String) "study_tcga_pub", structuralVariantSecondResult.getStudyId());
        Assert.assertEquals((Integer) 57670, structuralVariantSecondResult.getSite1EntrezGeneId());
        Assert.assertEquals("KIAA1549", structuralVariantSecondResult.getSite1HugoSymbol());
        Assert.assertEquals("ENST00000242365", structuralVariantSecondResult.getSite1EnsemblTranscriptId());
        Assert.assertEquals("7", structuralVariantSecondResult.getSite1Chromosome());
        Assert.assertEquals((Integer) 138536968, structuralVariantSecondResult.getSite1Position());
        Assert.assertEquals("KIAA1549-BRAF.K16B10.COSF509_1", structuralVariantSecondResult.getSite1Description());
        Assert.assertEquals((Integer) 673, structuralVariantSecondResult.getSite2EntrezGeneId());
        Assert.assertEquals("BRAF", structuralVariantSecondResult.getSite2HugoSymbol());
        Assert.assertEquals("ENST00000288602", structuralVariantSecondResult.getSite2EnsemblTranscriptId());
        Assert.assertEquals("7", structuralVariantSecondResult.getSite2Chromosome());
        Assert.assertEquals((Integer) 140482957, structuralVariantSecondResult.getSite2Position());
        Assert.assertEquals("KIAA1549-BRAF.K16B10.COSF509_2", structuralVariantSecondResult.getSite2Description());
        Assert.assertEquals(null, structuralVariantSecondResult.getSite2EffectOnFrame());
        Assert.assertEquals("GRCh37", structuralVariantSecondResult.getNcbiBuild());
        Assert.assertEquals("no", structuralVariantSecondResult.getDnaSupport());
        Assert.assertEquals("yes", structuralVariantSecondResult.getRnaSupport());
        Assert.assertEquals(null, structuralVariantSecondResult.getNormalReadCount());
        Assert.assertEquals((Integer) 100000, structuralVariantSecondResult.getTumorReadCount());
        Assert.assertEquals(null, structuralVariantSecondResult.getNormalVariantCount());
        Assert.assertEquals((Integer) 90000, structuralVariantSecondResult.getTumorVariantCount());
        Assert.assertEquals(null, structuralVariantSecondResult.getNormalPairedEndReadCount());
        Assert.assertEquals(null, structuralVariantSecondResult.getTumorPairedEndReadCount());
        Assert.assertEquals(null, structuralVariantSecondResult.getNormalSplitReadCount());
        Assert.assertEquals(null, structuralVariantSecondResult.getTumorSplitReadCount());
        Assert.assertEquals("KIAA1549-BRAF.K16B10.COSF509", structuralVariantSecondResult.getAnnotation());
        Assert.assertEquals(null, structuralVariantSecondResult.getBreakpointType());
        Assert.assertEquals(null, structuralVariantSecondResult.getConnectionType());
        Assert.assertEquals("Fusion", structuralVariantSecondResult.getEventInfo());
        Assert.assertEquals(null, structuralVariantSecondResult.getVariantClass());
        Assert.assertEquals(null, structuralVariantSecondResult.getLength());
        Assert.assertEquals("Gain-of-Function", structuralVariantSecondResult.getComments());
        Assert.assertEquals(null, structuralVariantSecondResult.getDriverFilter());
        Assert.assertEquals(null, structuralVariantSecondResult.getDriverFilterAnn());
        Assert.assertEquals(null, structuralVariantSecondResult.getDriverTiersFilter());
        Assert.assertEquals(null, structuralVariantSecondResult.getDriverTiersFilterAnn());
        Assert.assertEquals("GERMLINE", structuralVariantSecondResult.getSvStatus());
        StructuralVariant structuralVariantThirdResult = result.get(2);
        Assert.assertEquals("study_tcga_pub_sv", structuralVariantThirdResult.getMolecularProfileId());
        Assert.assertEquals((String) "TCGA-A1-A0SD-01", structuralVariantThirdResult.getSampleId());
        Assert.assertEquals((String) "TCGA-A1-A0SD", structuralVariantThirdResult.getPatientId());
        Assert.assertEquals((String) "study_tcga_pub", structuralVariantThirdResult.getStudyId());
        Assert.assertEquals((Integer) 57670, structuralVariantThirdResult.getSite1EntrezGeneId());
        Assert.assertEquals("KIAA1549", structuralVariantThirdResult.getSite1HugoSymbol());
        Assert.assertEquals("ENST00000242365", structuralVariantThirdResult.getSite1EnsemblTranscriptId());
        Assert.assertEquals("7", structuralVariantThirdResult.getSite1Chromosome());
        Assert.assertEquals((Integer) 138536968, structuralVariantThirdResult.getSite1Position());
        Assert.assertEquals("KIAA1549-BRAF.K16B10.COSF509_1", structuralVariantThirdResult.getSite1Description());
        Assert.assertEquals((Integer)673, structuralVariantThirdResult.getSite2EntrezGeneId());
        Assert.assertEquals("BRAF", structuralVariantThirdResult.getSite2HugoSymbol());
        Assert.assertEquals("ENST00000288602", structuralVariantThirdResult.getSite2EnsemblTranscriptId());
        Assert.assertEquals("7", structuralVariantThirdResult.getSite2Chromosome());
        Assert.assertEquals((Integer) 140482957, structuralVariantThirdResult.getSite2Position());
        Assert.assertEquals("KIAA1549-BRAF.K16B10.COSF509_2", structuralVariantThirdResult.getSite2Description());
        Assert.assertEquals(null, structuralVariantThirdResult.getSite2EffectOnFrame());
        Assert.assertEquals("GRCh37", structuralVariantThirdResult.getNcbiBuild());
        Assert.assertEquals("no", structuralVariantThirdResult.getDnaSupport());
        Assert.assertEquals("yes", structuralVariantThirdResult.getRnaSupport());
        Assert.assertEquals(null, structuralVariantThirdResult.getNormalReadCount());
        Assert.assertEquals((Integer) 100000, structuralVariantThirdResult.getTumorReadCount());
        Assert.assertEquals(null, structuralVariantThirdResult.getNormalVariantCount());
        Assert.assertEquals((Integer) 90000, structuralVariantThirdResult.getTumorVariantCount());
        Assert.assertEquals(null, structuralVariantThirdResult.getNormalPairedEndReadCount());
        Assert.assertEquals(null, structuralVariantThirdResult.getTumorPairedEndReadCount());
        Assert.assertEquals(null, structuralVariantThirdResult.getNormalSplitReadCount());
        Assert.assertEquals(null, structuralVariantThirdResult.getTumorSplitReadCount());
        Assert.assertEquals("KIAA1549-BRAF.K16B10.COSF509", structuralVariantThirdResult.getAnnotation());
        Assert.assertEquals(null, structuralVariantThirdResult.getBreakpointType());
        Assert.assertEquals(null, structuralVariantThirdResult.getConnectionType());
        Assert.assertEquals("Fusion", structuralVariantThirdResult.getEventInfo());
        Assert.assertEquals(null, structuralVariantThirdResult.getVariantClass());
        Assert.assertEquals(null, structuralVariantThirdResult.getLength());
        Assert.assertEquals("Gain-of-Function", structuralVariantThirdResult.getComments());
        Assert.assertEquals(null, structuralVariantThirdResult.getDriverFilter());
        Assert.assertEquals(null, structuralVariantThirdResult.getDriverFilterAnn());
        Assert.assertEquals(null, structuralVariantThirdResult.getDriverTiersFilter());
        Assert.assertEquals(null, structuralVariantThirdResult.getDriverTiersFilterAnn());
        Assert.assertEquals("SOMATIC", structuralVariantThirdResult.getSvStatus());

    }

    @Test
    public void fetchStructuralVariantsWithSampleIdentifier() throws Exception {

        List<String> molecularProfileIds = new ArrayList<>();
        List<Integer> entrezGeneIds = new ArrayList<Integer>();
        List<String> sampleIds = new ArrayList<String>();

        molecularProfileIds.add("study_tcga_pub_sv");
        entrezGeneIds.add(57670);
        sampleIds.add("TCGA-A1-A0SB-01");

        List<StructuralVariant> result = 
                structuralVariantMyBatisRepository.fetchStructuralVariants(molecularProfileIds,
                    sampleIds, entrezGeneIds, noStructVars);

        Assert.assertEquals(1,  result.size());
        StructuralVariant structuralVariantResult = result.get(0);
        Assert.assertEquals("study_tcga_pub_sv", structuralVariantResult.getMolecularProfileId());
        Assert.assertEquals((String) "TCGA-A1-A0SB-01", structuralVariantResult.getSampleId());
        Assert.assertEquals((String) "TCGA-A1-A0SB", structuralVariantResult.getPatientId());
        Assert.assertEquals((String) "study_tcga_pub", structuralVariantResult.getStudyId());

    }

    @Test
    public void fetchStructuralVariantsMultiStudyWithSampleIdentifiers() throws Exception {

        List<Integer> entrezGeneIds = new ArrayList<Integer>();
        List<String> molecularProfileIds = new ArrayList<String>();
        List<String> sampleIds = new ArrayList<String>();

        entrezGeneIds.add(57670);
        molecularProfileIds.add("study_tcga_pub_sv");
        sampleIds.add("TCGA-A1-A0SB-01");
        molecularProfileIds.add("acc_tcga_sv");
        sampleIds.add("TCGA-A1-B0SO-01");

        List<StructuralVariant> result = 
                structuralVariantMyBatisRepository.fetchStructuralVariants(molecularProfileIds,
                    sampleIds, entrezGeneIds, noStructVars);

        Assert.assertEquals(2,  result.size());
        StructuralVariant structuralVariantFirstResult = result.get(0);
        Assert.assertEquals("acc_tcga_sv", structuralVariantFirstResult.getMolecularProfileId());
        Assert.assertEquals((String) "TCGA-A1-B0SO-01", structuralVariantFirstResult.getSampleId());
        Assert.assertEquals((String) "TCGA-A1-B0SO", structuralVariantFirstResult.getPatientId());
        Assert.assertEquals((String) "acc_tcga", structuralVariantFirstResult.getStudyId());
        Assert.assertEquals("acc_tcga_sv", structuralVariantFirstResult.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-B0SO-01", structuralVariantFirstResult.getSampleId());
        Assert.assertEquals("TCGA-A1-B0SO", structuralVariantFirstResult.getPatientId());
        Assert.assertEquals("acc_tcga", structuralVariantFirstResult.getStudyId());
        StructuralVariant structuralVariantSecondResult = result.get(1);
        Assert.assertEquals("study_tcga_pub_sv", structuralVariantSecondResult.getMolecularProfileId());
        Assert.assertEquals((String) "TCGA-A1-A0SB-01", structuralVariantSecondResult.getSampleId());
        Assert.assertEquals((String) "TCGA-A1-A0SB", structuralVariantSecondResult.getPatientId());
        Assert.assertEquals((String) "study_tcga_pub", structuralVariantSecondResult.getStudyId());
    }

    @Test
    public void fetchStructuralVariantsMultiStudyNoGeneIdentifiers() throws Exception {

        List<String> molecularProfileIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        molecularProfileIds.add("study_tcga_pub_sv");
        sampleIds.add("TCGA-A1-A0SB-01");
        molecularProfileIds.add("acc_tcga_sv");
        sampleIds.add("TCGA-A1-B0SO-01");

        List<StructuralVariant> result =
            structuralVariantMyBatisRepository.fetchStructuralVariants(molecularProfileIds,
                sampleIds, noEntrezIds, noStructVars);

        Assert.assertEquals(8,  result.size());
    }

    @Test
    public void fetchStructuralVariantsMultiStudyByGeneQueriesWithSampleIdentifiers() throws Exception {

        List<StructuralVariant> result =
            structuralVariantMyBatisRepository.fetchStructuralVariantsByGeneQueries(molecularProfileIds,
                sampleIds, geneQueries);

        Assert.assertEquals(11,  result.size());

        List<String> resultTcgaPubVariants = result.stream()
            .filter(s -> "study_tcga_pub_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());
        List<String> resultTcgaVariants = result.stream()
            .filter(s -> "acc_tcga_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());
        
        Assert.assertArrayEquals(new String[] {"EML4-ALK.E6bA20.AB374362", "EML4-ALK.E6bA20.AB374362-2", "KIAA1549-BRAF.K16B10.COSF509", "KIAA1549-BRAF.K16B10.COSF509", "KIAA1549-BRAF.K16B10.COSF509", "NCOA4-RET.N7R1", "TMPRSS2-ERG.T1E2.COSF23.1"}, resultTcgaPubVariants.toArray());
        Assert.assertArrayEquals(new String[] {"KIAA1549-BRAF.K16B10.COSF509", "NCOA4-NULL", "NCOA4-RET.N7R1-2", "TMPRSS2-ERG.T1E2.COSF23.1"}, resultTcgaVariants.toArray());
    }

    @Test
    public void fetchStructuralVariantsMultiStudyByGeneQueriesWithSampleIdentifiersExcludePassenger() throws Exception {
        
        // There is one passenger event in 'study_tcga_pub_sv', the rest is unannotated
        geneQueries.stream().forEach(
            q -> {
                q.setIncludeVUS(false);
            }
        );

        List<StructuralVariant> result =
            structuralVariantMyBatisRepository.fetchStructuralVariantsByGeneQueries(molecularProfileIds,
                sampleIds, geneQueries);

        Assert.assertEquals(10,  result.size());

        List<String> resultTcgaPubVariants = result.stream()
            .filter(s -> "study_tcga_pub_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());
        List<String> resultTcgaVariants = result.stream()
            .filter(s -> "acc_tcga_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());

        Assert.assertArrayEquals(new String[] {"EML4-ALK.E6bA20.AB374362", "EML4-ALK.E6bA20.AB374362-2", "KIAA1549-BRAF.K16B10.COSF509", "KIAA1549-BRAF.K16B10.COSF509", "NCOA4-RET.N7R1", "TMPRSS2-ERG.T1E2.COSF23.1"}, resultTcgaPubVariants.toArray());
        Assert.assertArrayEquals(new String[] {"KIAA1549-BRAF.K16B10.COSF509", "NCOA4-NULL", "NCOA4-RET.N7R1-2", "TMPRSS2-ERG.T1E2.COSF23.1"}, resultTcgaVariants.toArray());
    }

    @Test
    public void fetchStructuralVariantsMultiStudyByGeneQueriesWithSampleIdentifiersSelectUnknownOncogenicity() throws Exception {

        // There is one passenger event in 'study_tcga_pub_sv', the rest is unannotatedd
        geneQueries.stream().forEach(
            q -> {
                q.setIncludeDriver(false);
                q.setIncludeVUS(false);
            }
        );

        List<StructuralVariant> result =
            structuralVariantMyBatisRepository.fetchStructuralVariantsByGeneQueries(molecularProfileIds,
                sampleIds, geneQueries);

        Assert.assertEquals(8,  result.size());

        List<String> resultTcgaPubVariants = result.stream()
            .filter(s -> "study_tcga_pub_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());
        List<String> resultTcgaVariants = result.stream()
            .filter(s -> "acc_tcga_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());

        Assert.assertArrayEquals(new String[] {"EML4-ALK.E6bA20.AB374362", "KIAA1549-BRAF.K16B10.COSF509", "KIAA1549-BRAF.K16B10.COSF509", "TMPRSS2-ERG.T1E2.COSF23.1"}, resultTcgaPubVariants.toArray());
        Assert.assertArrayEquals(new String[] {"KIAA1549-BRAF.K16B10.COSF509", "NCOA4-NULL", "NCOA4-RET.N7R1-2", "TMPRSS2-ERG.T1E2.COSF23.1"}, resultTcgaVariants.toArray());
    }

    @Test
    public void fetchStructuralVariantsMultiStudyByGeneQueriesWithSampleIdentifiersExcludeUnknownOncogenicity() throws Exception {

        // There is one passenger event in 'study_tcga_pub_sv', the rest is unannotated

        geneQueries.stream().forEach(
            q -> {
                q.setIncludeDriver(false);
                q.setIncludeUnknownOncogenicity(false);
            }
        );

        List<StructuralVariant> result =
            structuralVariantMyBatisRepository.fetchStructuralVariantsByGeneQueries(molecularProfileIds,
                sampleIds, geneQueries);

        Assert.assertEquals(1,  result.size());

        List<String> resultTcgaPubVariants = result.stream()
            .filter(s -> "study_tcga_pub_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());

        Assert.assertArrayEquals(new String[] {"KIAA1549-BRAF.K16B10.COSF509"}, resultTcgaPubVariants.toArray());
    }

    @Test
    public void fetchStructuralVariantsMultiStudyByGeneQueriesWithSampleIdentifiersIncludeTier() throws Exception {

        // There is one passenger event that is 'Tier 1 annotated' in 'study_tcga_pub_sv', the rest is unannotated
        geneQueries.stream().forEach(
            q -> {
                q.setIncludeUnknownTier(false);
                q.setSelectedTiers(Select.byValues(Arrays.asList("Tier 1")));
            }
        );

        List<StructuralVariant> result =
            structuralVariantMyBatisRepository.fetchStructuralVariantsByGeneQueries(molecularProfileIds,
                sampleIds, geneQueries);

        Assert.assertEquals(1,  result.size());

        List<String> resultTcgaPubVariants = result.stream()
            .filter(s -> "study_tcga_pub_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());

        Assert.assertArrayEquals(new String[] {"KIAA1549-BRAF.K16B10.COSF509"}, resultTcgaPubVariants.toArray());
    }

    @Test
    public void fetchStructuralVariantsMultiStudyByGeneQueriesWithSampleIdentifiersUnknownTier() throws Exception {

        // There is one passenger event that is 'Tier 1 annotated' in 'study_tcga_pub_sv', the rest is unannotated
        geneQueries.stream().forEach(
            q -> {
                q.setSelectedTiers(Select.none());
            }
        );
        
        List<StructuralVariant> result =
            structuralVariantMyBatisRepository.fetchStructuralVariantsByGeneQueries(molecularProfileIds,
                sampleIds, geneQueries);

        Assert.assertEquals(8,  result.size());

        List<String> resultTcgaPubVariants = result.stream()
            .filter(s -> "study_tcga_pub_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());
        List<String> resultTcgaVariants = result.stream()
            .filter(s -> "acc_tcga_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());

        Assert.assertArrayEquals(new String[] {"EML4-ALK.E6bA20.AB374362", "KIAA1549-BRAF.K16B10.COSF509", "KIAA1549-BRAF.K16B10.COSF509", "TMPRSS2-ERG.T1E2.COSF23.1"}, resultTcgaPubVariants.toArray());
        Assert.assertArrayEquals(new String[] {"KIAA1549-BRAF.K16B10.COSF509", "NCOA4-NULL", "NCOA4-RET.N7R1-2", "TMPRSS2-ERG.T1E2.COSF23.1"}, resultTcgaVariants.toArray());
    }
    
    @Test
    public void fetchStructuralVariantsWithSingleStructuralVariantQueries() throws Exception {

        List<String> molecularProfileIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();

        molecularProfileIds.add("study_tcga_pub_sv");

        List<StructuralVariantQuery> singleStructVarQuery = new ArrayList<>();
        singleStructVarQuery.add(new StructuralVariantQuery(
            new StructuralVariantGeneSubQuery(27436), 
            new StructuralVariantGeneSubQuery(238)
        ));
        
        List<StructuralVariant> result = 
                structuralVariantMyBatisRepository.fetchStructuralVariants(
                    molecularProfileIds,
                    sampleIds, 
                    noEntrezIds,
                    singleStructVarQuery
                );
        
        Assert.assertEquals(2,  result.size());
        StructuralVariant structuralVariantFirstResult = result.get(0);
        Assert.assertEquals((Integer) 27436, structuralVariantFirstResult.getSite1EntrezGeneId());
        Assert.assertEquals((Integer) 238, structuralVariantFirstResult.getSite2EntrezGeneId());
        Assert.assertEquals("Fusion", structuralVariantFirstResult.getEventInfo());
    }

    @Test
    public void fetchStructuralVariantsWithStructuralVariantQueryAndGeneId() throws Exception {

        List<String> molecularProfileIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();

        molecularProfileIds.add("study_tcga_pub_sv");

        List<Integer> entrezGeneId = Arrays.asList(8031);
        List<StructuralVariantQuery> singleStructVarQuery = new ArrayList<>();
        singleStructVarQuery.add(new StructuralVariantQuery(
            new StructuralVariantGeneSubQuery(27436),
            new StructuralVariantGeneSubQuery(238)
        ));

        List<StructuralVariant> result =
            structuralVariantMyBatisRepository.fetchStructuralVariants(
                molecularProfileIds,
                sampleIds,
                entrezGeneId,
                singleStructVarQuery
            );

        Assert.assertEquals(3,  result.size());
    }
    
    @Test
    public void fetchStructuralVariantsWithMultipleStructuralVariantQueries() throws Exception {
        List<String> molecularProfileIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();

        molecularProfileIds.add("study_tcga_pub_sv");

        List<StructuralVariantQuery> multipleStructVarQueries = new ArrayList<>();
        multipleStructVarQueries.add(new StructuralVariantQuery(
            new StructuralVariantGeneSubQuery(27436), 
            new StructuralVariantGeneSubQuery(238)
        ));
        multipleStructVarQueries.add(new StructuralVariantQuery(
            new StructuralVariantGeneSubQuery(57670),
            new StructuralVariantGeneSubQuery(673)
        ));
        
        List<StructuralVariant> result = 
                structuralVariantMyBatisRepository.fetchStructuralVariants(molecularProfileIds,
                    sampleIds, noEntrezIds, multipleStructVarQueries
                );
        
        Assert.assertEquals(5,  result.size());

        List<StructuralVariant> structuralVariantFirstResult = result.stream()
            .filter(sv -> Objects.equals(sv.getSite1EntrezGeneId(), 27436) 
                && Objects.equals(sv.getSite2EntrezGeneId(), 238))
            .collect(Collectors.toList());;
        Assert.assertEquals(2,  structuralVariantFirstResult.size());

        List<StructuralVariant> structuralVariantSecondResults = result.stream()
            .filter(sv -> Objects.equals(sv.getSite1EntrezGeneId(), 57670) 
                && Objects.equals(sv.getSite2EntrezGeneId(), 673))
            .collect(Collectors.toList());
        Assert.assertEquals(3, structuralVariantSecondResults.size());
    }
    
    @Test
    public void fetchStructuralVariantsWithMultipleStructuralVariantIdentifierWildcardQueryValue() {
        List<String> molecularProfileIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();

        molecularProfileIds.add("study_tcga_pub_sv");

        List<StructuralVariantQuery> structVarQueries = new ArrayList<>();
        
        StructuralVariantQuery svId1 = new StructuralVariantQuery(
            new StructuralVariantGeneSubQuery(27436),
            new StructuralVariantGeneSubQuery(StructuralVariantSpecialValue.ANY_GENE)
        );
        structVarQueries.add(svId1);
        
        StructuralVariantQuery svId2 = new StructuralVariantQuery(
            new StructuralVariantGeneSubQuery(StructuralVariantSpecialValue.ANY_GENE),
            new StructuralVariantGeneSubQuery(673)
        );
        structVarQueries.add(svId2);
        
        List<StructuralVariant> result = 
                structuralVariantMyBatisRepository.fetchStructuralVariants(molecularProfileIds,
                    sampleIds, noEntrezIds, structVarQueries
                );
        
        Assert.assertEquals(5,  result.size());

        List<StructuralVariant> structuralVariantFirstResult = result.stream()
            .filter(sv -> Objects.equals(sv.getSite1EntrezGeneId(), 27436) 
                && Objects.equals(sv.getSite2EntrezGeneId(), 238))
            .collect(Collectors.toList());;
        Assert.assertEquals(2,  structuralVariantFirstResult.size());

        List<StructuralVariant> structuralVariantSecondResults = result.stream()
            .filter(sv -> Objects.equals(sv.getSite1EntrezGeneId(), 57670) 
                && Objects.equals(sv.getSite2EntrezGeneId(), 673))
            .collect(Collectors.toList());
        Assert.assertEquals(3, structuralVariantSecondResults.size());
    }
    
    @Test
    public void fetchStructuralVariantsWithMultipleStructuralVariantQueryNullQueryValue() {
        List<String> molecularProfileIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();

        molecularProfileIds.add("acc_tcga_sv");

        List<StructuralVariantQuery> structVarQueries = new ArrayList<>();
        StructuralVariantQuery structVarWithNull = new StructuralVariantQuery(
            new StructuralVariantGeneSubQuery(8031), 
            new StructuralVariantGeneSubQuery(StructuralVariantSpecialValue.NO_GENE)
        );
        structVarQueries.add(structVarWithNull);

        List<StructuralVariant> result = 
                structuralVariantMyBatisRepository.fetchStructuralVariants(molecularProfileIds,
                    sampleIds, noEntrezIds, structVarQueries
                );
        
        Assert.assertEquals(1,  result.size());
        Assert.assertEquals((Integer) 8031, result.get(0).getSite1EntrezGeneId());
        Assert.assertEquals("ENST00000340058_NULL",  result.get(0).getSite2EnsemblTranscriptId());
    }

    @Test
    public void fetchStructuralVariantsMultiStudyByStructVarQueriesAll() throws Exception {

        List<StructuralVariant> result =
            structuralVariantMyBatisRepository.fetchStructuralVariantsByStructVarQueries(molecularProfileIds,
                sampleIds, structVarQueries);

        Assert.assertEquals(6,  result.size());

        List<String> resultTcgaPubVariants = result.stream()
            .filter(s -> "study_tcga_pub_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());
        List<String> resultTcgaVariants = result.stream()
            .filter(s -> "acc_tcga_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());

        Assert.assertArrayEquals(new String[] {"EML4-ALK.E6bA20.AB374362", "EML4-ALK.E6bA20.AB374362-2", "KIAA1549-BRAF.K16B10.COSF509", "KIAA1549-BRAF.K16B10.COSF509", "KIAA1549-BRAF.K16B10.COSF509"}, resultTcgaPubVariants.toArray());
        Assert.assertArrayEquals(new String[] {"KIAA1549-BRAF.K16B10.COSF509"}, resultTcgaVariants.toArray());
    }
    @Test
    public void fetchStructuralVariantsMultiStudyByStructVarQueriesNoGermline() throws Exception {

        structVarFilterQuery1.setIncludeGermline(false);
        structVarFilterQuery2.setIncludeGermline(false);
        
        List<StructuralVariant> result =
            structuralVariantMyBatisRepository.fetchStructuralVariantsByStructVarQueries(molecularProfileIds,
                sampleIds, structVarQueries);

        Assert.assertEquals(4,  result.size());

        List<String> resultTcgaPubVariants = result.stream()
            .filter(s -> "study_tcga_pub_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());
        List<String> resultTcgaVariants = result.stream()
            .filter(s -> "acc_tcga_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());

        Assert.assertArrayEquals(new String[] {"EML4-ALK.E6bA20.AB374362-2", "KIAA1549-BRAF.K16B10.COSF509", "KIAA1549-BRAF.K16B10.COSF509"}, resultTcgaPubVariants.toArray());
        Assert.assertArrayEquals(new String[] {"KIAA1549-BRAF.K16B10.COSF509"}, resultTcgaVariants.toArray());
    }

    @Test
    public void fetchStructuralVariantsMultiStudyByStructVarQueriesNoSomatic() throws Exception {

        structVarFilterQuery1.setIncludeSomatic(false);
        structVarFilterQuery2.setIncludeSomatic(false);
        
        List<StructuralVariant> result =
            structuralVariantMyBatisRepository.fetchStructuralVariantsByStructVarQueries(molecularProfileIds,
                sampleIds, structVarQueries);

        Assert.assertEquals(2,  result.size());

        List<String> resultTcgaPubVariants = result.stream()
            .filter(s -> "study_tcga_pub_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());
        List<String> resultTcgaVariants = result.stream()
            .filter(s -> "acc_tcga_sv".equals(s.getMolecularProfileId()))
            .map(StructuralVariant::getAnnotation)
            .collect(Collectors.toList());

        Assert.assertArrayEquals(new String[] {"EML4-ALK.E6bA20.AB374362", "KIAA1549-BRAF.K16B10.COSF509"}, resultTcgaPubVariants.toArray());
        Assert.assertEquals(0, resultTcgaVariants.size());
    }

    @Test
    public void fetchStructuralVariantsMultiStudyByStructVarQueriesAnyGene1() throws Exception {

        structVarFilterQuery1.getGene1Query()
            .setSpecialValue(StructuralVariantSpecialValue.ANY_GENE);
        structVarFilterQuery2.getGene1Query()
            .setSpecialValue(StructuralVariantSpecialValue.ANY_GENE);
        
        List<StructuralVariant> result =
            structuralVariantMyBatisRepository.fetchStructuralVariantsByStructVarQueries(molecularProfileIds,
                sampleIds, structVarQueries);

        Assert.assertEquals(6,  result.size());
    }

    @Test
    public void fetchStructuralVariantsMultiStudyByStructVarQueriesAnyGene2() throws Exception {

        structVarFilterQuery1.getGene2Query()
            .setSpecialValue(StructuralVariantSpecialValue.ANY_GENE);
        structVarFilterQuery2.getGene2Query()
            .setSpecialValue(StructuralVariantSpecialValue.ANY_GENE);
        
        List<StructuralVariant> result =
            structuralVariantMyBatisRepository.fetchStructuralVariantsByStructVarQueries(molecularProfileIds,
                sampleIds, structVarQueries);

        Assert.assertEquals(6,  result.size());
    }
    
    @Test
    public void fetchStructuralVariantsMultiStudyByStructVarQueriesNullGene1() throws Exception {

        List<StructuralVariant> result =
            structuralVariantMyBatisRepository.fetchStructuralVariantsByStructVarQueries(molecularProfileIds,
                sampleIds, Arrays.asList(structVarFilterQueryNullGene1));

        Assert.assertEquals(0,  result.size());
    }
    
    @Test
    public void fetchStructuralVariantsMultiStudyByStructVarQueriesNullGene2() throws Exception {

        List<StructuralVariant> result =
            structuralVariantMyBatisRepository.fetchStructuralVariantsByStructVarQueries(molecularProfileIds,
                sampleIds, Arrays.asList(structVarFilterQueryNullGene2));

        Assert.assertEquals(1,  result.size());
    }

}
