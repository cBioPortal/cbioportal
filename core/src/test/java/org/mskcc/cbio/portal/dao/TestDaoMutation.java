/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.dao;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.model.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;

/**
 * JUnit tests for DaoMutation class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestDaoMutation {

    int geneticProfileId;
    int sampleId;
    CanonicalGene gene;

    @Before
    public void setUp() throws DaoException {
        int studyId = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub").getInternalId();
        ArrayList<GeneticProfile> list = DaoGeneticProfile.getAllGeneticProfiles(studyId);
        geneticProfileId = list.get(0).getGeneticProfileId();

        sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-A1-A0SB-01").getInternalId();

        gene = new CanonicalGene(321, "BLAH");
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        daoGeneOptimized.addGene(gene);
    }

    @Test
    public void testDaoMutation() throws DaoException {
        MySQLbulkLoader.bulkLoadOn();
        runTheTest();
    }

    private void runTheTest() throws DaoException{
        //  Add a fake gene

        ExtendedMutation mutation = new ExtendedMutation();

        mutation.setMutationEventId(1);
        mutation.setKeyword("key");
        mutation.setGeneticProfileId(geneticProfileId);
        mutation.setSampleId(sampleId);
        mutation.setGene(gene);
        mutation.setValidationStatus("validated");
        mutation.setMutationStatus("somatic");
        mutation.setMutationType("missense");
        mutation.setChr("chr1");
        mutation.setStartPosition(10000);
        mutation.setEndPosition(20000);
        mutation.setSequencingCenter("Broad");
        mutation.setSequencer("SOLiD");
        mutation.setProteinChange("BRCA1_123");
        mutation.setFunctionalImpactScore("H");
        mutation.setFisValue(Float.MIN_VALUE);
        mutation.setLinkXVar("link1");
        mutation.setLinkPdb("link2");
        mutation.setLinkMsa("link3");
        mutation.setNcbiBuild("37/hg19");
        mutation.setStrand("+");
        mutation.setVariantType("Consolidated");
        mutation.setReferenceAllele("ATGC");
        mutation.setTumorSeqAllele1("ATGC");
        mutation.setTumorSeqAllele2("ATGC");
        mutation.setTumorSeqAllele("ATGC");
        mutation.setDbSnpRs("rs12345");
        mutation.setDbSnpValStatus("by2Hit2Allele;byCluster");
        mutation.setMatchedNormSampleBarcode("TCGA-02-0021-10A-01D-0002-04");
        mutation.setMatchNormSeqAllele1("TGCA");
        mutation.setMatchNormSeqAllele2("TGCA");
        mutation.setTumorValidationAllele1("AT-GC");
        mutation.setTumorValidationAllele2("AT-GC");
        mutation.setMatchNormValidationAllele1("C");
        mutation.setMatchNormValidationAllele2("G");
        mutation.setVerificationStatus("Verified");
        mutation.setSequencingPhase("Phase_6");
        mutation.setSequenceSource("PCR;Capture;WGS");
        mutation.setValidationMethod("Sanger_PCR_WGA;Sanger_PCR_gDNA");
        mutation.setScore("NA");
        mutation.setBamFile("NA");
        mutation.setTumorAltCount(6);
        mutation.setTumorRefCount(16);
        mutation.setNormalAltCount(8);
        mutation.setNormalRefCount(18);
        mutation.setOncotatorDbSnpRs("rs149680468");
        mutation.setOncotatorCodonChange("c.(133-135)TCT>TTT");
        mutation.setOncotatorRefseqMrnaId("NM_001904");
        mutation.setOncotatorUniprotName("CTNB1_HUMAN");
        mutation.setOncotatorUniprotAccession("P35222");
        mutation.setOncotatorProteinPosStart(666);
        mutation.setOncotatorProteinPosEnd(678);
        mutation.setCanonicalTranscript(true);
        mutation.setAnnotationJson(makeMockAnnotationJsonString());

        DaoMutation.addMutation(mutation,true);

        // if bulkLoading, execute LOAD FILE
        if( MySQLbulkLoader.isBulkLoad()){
            MySQLbulkLoader.flushAll();
        }
        ArrayList<ExtendedMutation> mutationList = DaoMutation.getMutations(geneticProfileId, 1, 321);
        validateMutation(mutationList.get(0));

        //  Test the getGenesInProfile method
        Set<CanonicalGene> geneSet = DaoMutation.getGenesInProfile(geneticProfileId);
        assertEquals(1, geneSet.size());

        ArrayList<CanonicalGene> geneList = new ArrayList<CanonicalGene>(geneSet);
        CanonicalGene gene = geneList.get(0);
        assertEquals(321, gene.getEntrezGeneId());
        assertEquals("BLAH", gene.getHugoGeneSymbolAllCaps());
    }

    private void validateMutation(ExtendedMutation mutation) {
        assertEquals(geneticProfileId, mutation.getGeneticProfileId());
        assertEquals(sampleId, mutation.getSampleId());
        assertEquals(321, mutation.getEntrezGeneId());
        assertEquals("validated", mutation.getValidationStatus());
        assertEquals("somatic", mutation.getMutationStatus());
        assertEquals("missense", mutation.getMutationType());
        assertEquals("chr1", mutation.getChr());
        assertEquals(10000, mutation.getStartPosition());
        assertEquals(20000, mutation.getEndPosition());
        assertEquals("Broad", mutation.getSequencingCenter());
        assertEquals("SOLiD", mutation.getSequencer());
        assertEquals("BRCA1_123", mutation.getProteinChange());
        assertEquals("H", mutation.getFunctionalImpactScore());
        assertEquals(Float.MIN_VALUE, mutation.getFisValue(), 1E-30);
        assertEquals("link1", mutation.getLinkXVar());
        assertEquals("link2", mutation.getLinkPdb());
        assertEquals("link3", mutation.getLinkMsa());
        assertEquals("37/hg19", mutation.getNcbiBuild());
        assertEquals("+", mutation.getStrand());
        assertEquals("Consolidated", mutation.getVariantType());
        assertEquals("ATGC", mutation.getReferenceAllele());
        assertEquals("ATGC", mutation.getTumorSeqAllele1());
        assertEquals("ATGC", mutation.getTumorSeqAllele2());
        assertEquals("rs12345", mutation.getDbSnpRs());
        assertEquals("by2Hit2Allele;byCluster", mutation.getDbSnpValStatus());
        assertEquals("TCGA-02-0021-10A-01D-0002-04", mutation.getMatchedNormSampleBarcode());
        assertEquals("TGCA", mutation.getMatchNormSeqAllele1());
        assertEquals("TGCA", mutation.getMatchNormSeqAllele2());
        assertEquals("AT-GC", mutation.getTumorValidationAllele1());
        assertEquals("AT-GC", mutation.getTumorValidationAllele2());
        assertEquals("C", mutation.getMatchNormValidationAllele1());
        assertEquals("G", mutation.getMatchNormValidationAllele2());
        assertEquals("Verified", mutation.getVerificationStatus());
        assertEquals("Phase_6", mutation.getSequencingPhase());
        assertEquals("PCR;Capture;WGS", mutation.getSequenceSource());
        assertEquals("Sanger_PCR_WGA;Sanger_PCR_gDNA", mutation.getValidationMethod());
        assertEquals("NA", mutation.getScore());
        assertEquals("NA", mutation.getBamFile());
        assertEquals(Integer.valueOf(6), mutation.getTumorAltCount());
        assertEquals(Integer.valueOf(16), mutation.getTumorRefCount());
        assertEquals(Integer.valueOf(8), mutation.getNormalAltCount());
        assertEquals(Integer.valueOf(18), mutation.getNormalRefCount());
        assertEquals("rs149680468", mutation.getOncotatorDbSnpRs());
        assertEquals("c.(133-135)TCT>TTT", mutation.getOncotatorCodonChange());
        assertEquals("NM_001904", mutation.getOncotatorRefseqMrnaId());
        assertEquals("CTNB1_HUMAN", mutation.getOncotatorUniprotName());
        assertEquals("P35222", mutation.getOncotatorUniprotAccession());
        assertEquals(666, mutation.getOncotatorProteinPosStart());
        assertEquals(678, mutation.getOncotatorProteinPosEnd());
        assertEquals(true, mutation.isCanonicalTranscript());
        validateMockAnnotationJson(mutation);

    }

    private void validateMockAnnotationJson(ExtendedMutation mutation) {
        Map<String, Map<String, String>> annotationJsonMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            annotationJsonMap = mapper.readValue(mutation.getAnnotationJson(), annotationJsonMap.getClass());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        Map<String, Map<String, String>> expectedAnnotationJsonMap = makeMockExpectedAnnotationJsonMap();

        // confirm expected sizes
        Assert.assertTrue(annotationJsonMap.containsKey("namespace1"));
        Assert.assertEquals(expectedAnnotationJsonMap.get("namespace1").size(), annotationJsonMap.get("namespace1").size());
        Assert.assertEquals(2, annotationJsonMap.get("namespace1").size());
        Assert.assertTrue(annotationJsonMap.containsKey("namespace2"));
        Assert.assertEquals(1, annotationJsonMap.get("namespace2").size());
        Assert.assertEquals(expectedAnnotationJsonMap.get("namespace2").size(), annotationJsonMap.get("namespace2").size());

        // compare namespace 1 annotation map values
        Map<String, String> namespace1Map = annotationJsonMap.get("namespace1");
        Map<String, String> expectedNamespace1Map = expectedAnnotationJsonMap.get("namespace1");
        Assert.assertEquals(expectedNamespace1Map.get("header1"), namespace1Map.get("header1"));
        Assert.assertEquals(expectedNamespace1Map.get("header2"), namespace1Map.get("header2"));

        // compare namespace 2 annotation map values
        Map<String, String> namespace2Map = annotationJsonMap.get("namespace2");
        Map<String, String> expectedNamespace2Map = expectedAnnotationJsonMap.get("namespace2");
        Assert.assertEquals(expectedNamespace2Map.get("header1"), namespace2Map.get("header1"));
    }

    private String makeMockAnnotationJsonString() {
        // return annotation json map as a string
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(makeMockExpectedAnnotationJsonMap());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Map<String, Map<String, String>> makeMockExpectedAnnotationJsonMap() {
        // construct map of namespaces to key-value pairs
        Map<String, Map<String, String>> annotationJsonMap = new HashMap<>();
        Map<String, String> namespace1Annotations = new HashMap<>();
        namespace1Annotations.put("header1", "ns1_header1_value");
        namespace1Annotations.put("header2", "ns1_header2_value");
        annotationJsonMap.put("namespace1", namespace1Annotations);

        Map<String, String> namespace2Annotations = new HashMap<>();
        namespace2Annotations.put("header1", "ns2_header1_value");
        annotationJsonMap.put("namespace2", namespace2Annotations);
        return annotationJsonMap;
    }
}
