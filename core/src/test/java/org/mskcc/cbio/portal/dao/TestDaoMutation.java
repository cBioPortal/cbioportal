/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.scripts.ResetDatabase;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.model.CanonicalGene;

import java.util.ArrayList;
import java.util.Set;

/**
 * JUnit tests for DaoMutation class.
 */
public class TestDaoMutation extends TestCase {

	public void testDaoMutation() throws DaoException {
		MySQLbulkLoader.bulkLoadOn();
		runTheTest();
	}

	private void runTheTest() throws DaoException{
		//  Add a fake gene
		DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
		CanonicalGene blahGene = new CanonicalGene(321, "BLAH");
		daoGene.addGene(blahGene);

		ResetDatabase.resetDatabase();

		ExtendedMutation mutation = new ExtendedMutation();

                mutation.setMutationEventId(1);
                mutation.setKeyword("key");
		mutation.setGeneticProfileId(1);
		mutation.setCaseId("1234");
		mutation.setGene(blahGene);
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

		DaoMutation.addMutation(mutation,true);

		// if bulkLoading, execute LOAD FILE
		if( MySQLbulkLoader.isBulkLoad()){
                    MySQLbulkLoader.flushAll();
		}
		ArrayList<ExtendedMutation> mutationList = DaoMutation.getMutations(1, "1234", 321);
		validateMutation(mutationList.get(0));

		//  Test the getGenesInProfile method
		Set<CanonicalGene> geneSet = DaoMutation.getGenesInProfile(1);
		assertEquals (1, geneSet.size());

		ArrayList<CanonicalGene> geneList = new ArrayList<CanonicalGene>(geneSet);
		CanonicalGene gene = geneList.get(0);
		assertEquals (321, gene.getEntrezGeneId());
		assertEquals ("BLAH", gene.getHugoGeneSymbolAllCaps());

	}

	private void validateMutation(ExtendedMutation mutation) {
		assertEquals (1, mutation.getGeneticProfileId());
		assertEquals ("1234", mutation.getCaseId());
		assertEquals (321, mutation.getEntrezGeneId());
		assertEquals ("validated", mutation.getValidationStatus());
		assertEquals ("somatic", mutation.getMutationStatus());
		assertEquals ("missense", mutation.getMutationType());
		assertEquals ("chr1", mutation.getChr());
		assertEquals (10000, mutation.getStartPosition());
		assertEquals (20000, mutation.getEndPosition());
		assertEquals ("Broad", mutation.getSequencingCenter());
		assertEquals ("SOLiD", mutation.getSequencer());
		assertEquals ("BRCA1_123", mutation.getProteinChange());
		assertEquals ("H", mutation.getFunctionalImpactScore());
		assertEquals (Float.MIN_VALUE, mutation.getFisValue());
		assertEquals ("link1", mutation.getLinkXVar());
		assertEquals ("link2", mutation.getLinkPdb());
		assertEquals ("link3", mutation.getLinkMsa());
		assertEquals ("37/hg19", mutation.getNcbiBuild());
		assertEquals ("+", mutation.getStrand());
		assertEquals ("Consolidated", mutation.getVariantType());
		assertEquals ("ATGC", mutation.getReferenceAllele());
		assertEquals ("ATGC", mutation.getTumorSeqAllele1());
		assertEquals ("ATGC", mutation.getTumorSeqAllele2());
		assertEquals ("rs12345", mutation.getDbSnpRs());
		assertEquals ("by2Hit2Allele;byCluster", mutation.getDbSnpValStatus());
		assertEquals ("TCGA-02-0021-10A-01D-0002-04", mutation.getMatchedNormSampleBarcode());
		assertEquals ("TGCA", mutation.getMatchNormSeqAllele1());
		assertEquals ("TGCA", mutation.getMatchNormSeqAllele2());
		assertEquals ("AT-GC", mutation.getTumorValidationAllele1());
		assertEquals ("AT-GC", mutation.getTumorValidationAllele2());
		assertEquals ("C", mutation.getMatchNormValidationAllele1());
		assertEquals ("G", mutation.getMatchNormValidationAllele2());
		assertEquals ("Verified", mutation.getVerificationStatus());
		assertEquals ("Phase_6", mutation.getSequencingPhase());
		assertEquals ("PCR;Capture;WGS", mutation.getSequenceSource());
		assertEquals ("Sanger_PCR_WGA;Sanger_PCR_gDNA", mutation.getValidationMethod());
		assertEquals ("NA", mutation.getScore());
		assertEquals ("NA", mutation.getBamFile());
		assertEquals (6, mutation.getTumorAltCount());
		assertEquals (16, mutation.getTumorRefCount());
		assertEquals (8, mutation.getNormalAltCount());
		assertEquals (18, mutation.getNormalRefCount());
		assertEquals ("rs149680468", mutation.getOncotatorDbSnpRs());
		assertEquals ("c.(133-135)TCT>TTT", mutation.getOncotatorCodonChange());
		assertEquals("NM_001904", mutation.getOncotatorRefseqMrnaId());
		assertEquals("CTNB1_HUMAN", mutation.getOncotatorUniprotName());
		assertEquals("P35222", mutation.getOncotatorUniprotAccession());
		assertEquals(666, mutation.getOncotatorProteinPosStart());
		assertEquals(678, mutation.getOncotatorProteinPosEnd());
		assertEquals (true, mutation.isCanonicalTranscript());
	}
}