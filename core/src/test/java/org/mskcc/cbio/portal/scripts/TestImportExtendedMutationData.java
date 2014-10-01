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

package org.mskcc.cbio.portal.scripts;

import junit.framework.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import java.io.*;
import java.util.ArrayList;

/**
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class TestImportExtendedMutationData extends TestCase {

    public void testImportExtendedMutationData() {

        try {

            MySQLbulkLoader.bulkLoadOn();
            
            ProgressMonitor pMonitor = new ProgressMonitor();
            pMonitor.setConsoleMode(false);
			// TBD: change this to use getResourceAsStream()
            File file = new File("target/test-classes/data_mutations_extended.txt");
            ImportExtendedMutationData parser;

            try {
                TestImportUtil.createSmallDbms(true);
                parser = new ImportExtendedMutationData(file, 1, pMonitor, "no_such_germline_whitelistfile");
                Assert.fail("Should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                assertEquals("Gene list 'no_such_germline_whitelistfile' not found.", e.getMessage());
            }
            loadGenes();
            
            parser = new ImportExtendedMutationData(file, 1, pMonitor);
            parser.importData();
            MySQLbulkLoader.flushAll();
            
            checkBasicFilteringRules();
            
            // accept everything else
            validateMutationAminoAcid (1, 1, 51806, "P113L");   // valid Unknown
            validateMutationAminoAcid (1, 1, 89, "S116R"); // Unknown  Somatic

            loadGenes();
			// TBD: change this to use getResourceAsStream()
            parser = new ImportExtendedMutationData(file, 1, pMonitor, "target/test-classes/test_germline_white_list_file2.txt");
            // put on: CLEC7A
            parser.importData();
            MySQLbulkLoader.flushAll();
            checkBasicFilteringRules();
            checkGermlineMutations();
            acceptEverythingElse();


            loadGenes();
			// TBD: change this to use getResourceAsStream()
            parser = new ImportExtendedMutationData(file, 1, pMonitor, "target/test-classes/test_germline_white_list_file2.txt");
            parser.importData();
            MySQLbulkLoader.flushAll();
            checkBasicFilteringRules();
            checkGermlineMutations();
            acceptEverythingElse();
            validateMutationAminoAcid (1, 1, 54407, "T433A");


            loadGenes();
			// TBD: change this to use getResourceAsStream()
            parser = new ImportExtendedMutationData(file, 1, pMonitor, "target/test-classes/test_germline_white_list_file2.txt");
            parser.importData();
            MySQLbulkLoader.flushAll();
            checkBasicFilteringRules();
            checkGermlineMutations();
            acceptEverythingElse();
            validateMutationAminoAcid(1, 1, 54407, "T433A");
            // Unknown  Somatic mutations on somatic whitelist
            validateMutationAminoAcid(1, 1, 6667, "A513V");
            // Unknown  Somatic mutations on somatic whitelist2

            // additional tests for the MAF columns added after oncotator
            loadGenes();
            file = new File("target/test-classes/data_mutations_oncotated.txt");
            parser = new ImportExtendedMutationData(file, 1, pMonitor);
            parser.importData();
            MySQLbulkLoader.flushAll();
            checkOncotatedImport();

        } catch (DaoException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // reject somatic mutations that aren't valid somatic, or on one of the somatic whitelists
    private void acceptEverythingElse() throws DaoException {
        assertEquals(1, DaoMutation.getMutations(1, 1, 51806).size());   // valid Unknown
        assertEquals(1, DaoMutation.getMutations(1, 1, 89).size()); // Unknown  Somatic
    }

    private void checkBasicFilteringRules() throws DaoException {
        rejectSilentLOHIntronWildtype();
        acceptValidSomaticMutations();
    }

	private void checkOncotatedImport() throws DaoException
	{
		ArrayList<ExtendedMutation> mutationList = DaoMutation.getAllMutations();

		// assert table size; 3 silent mutations should be rejected
		assertEquals(16, mutationList.size());

		// assert data for oncotator columns
		//assertEquals("FAM90A1", mutationList.get(0).getGeneSymbol());
		//assertEquals("Missense_Mutation", mutationList.get(1).getOncotatorVariantClassification());
		//assertEquals("p.R131H", mutationList.get(4).getOncotatorProteinChange());
		//assertEquals("rs76360727;rs33980232", mutationList.get(9).getOncotatorDbSnpRs());
//		assertEquals("p.E366_Q409del(13)|p.Q367R(1)|p.E366_K477del(1)",
//		             mutationList.get(15).getOncotatorCosmicOverlapping());
	}

    private void validateMutationAminoAcid (int geneticProfileId, Integer sampleId, long entrezGeneId,
            String expectedAminoAcidChange) throws DaoException {
        ArrayList<ExtendedMutation> mutationList = DaoMutation.getMutations
                (geneticProfileId, sampleId, entrezGeneId);
        assertEquals(1, mutationList.size());
        assertEquals(expectedAminoAcidChange, mutationList.get(0).getProteinChange());
    }

    private void acceptValidSomaticMutations() throws DaoException {
        // valid Somatic
        validateMutationAminoAcid (1, 1, 282770, "R113C");

        // valid Somatic
        validateMutationAminoAcid (1, 1, 51259, "G61G");
    }

    private void rejectSilentLOHIntronWildtype() throws DaoException {
        assertEquals(0, DaoMutation.getMutations(1, 1, 114548).size()); // silent
        assertEquals(0, DaoMutation.getMutations(1, 1, 343035).size()); // LOH
        assertEquals(0, DaoMutation.getMutations(1, 1, 80114).size()); // Wildtype
        assertEquals(0, DaoMutation.getMutations(1, 1, 219736).size()); // Wildtype
        assertEquals(0, DaoMutation.getMutations(1, 1, 6609).size()); // Intron
    }


    private void checkGermlineMutations() throws DaoException {
        assertEquals(0, DaoMutation.getMutations(1, 1, 64581).size());
        // missense, Germline mutation on germline whitelist

        // Germline mutation on germline whitelist
        validateMutationAminoAcid (1, 1, 2842, "L113P");
        assertEquals(0, DaoMutation.getMutations(1, 1, 50839).size());
        // Germline mutations NOT on germline whitelist
    }

    private void loadGenes() throws DaoException {
        TestImportUtil.createSmallDbms(true);
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

	    // genes for "data_mutations_extended.txt"
        daoGene.addGene(new CanonicalGene(114548L, "NLRP3"));
        daoGene.addGene(new CanonicalGene(3339L, "HSPG2"));
        daoGene.addGene(new CanonicalGene(282770L, "OR10AG1"));
        daoGene.addGene(new CanonicalGene(51806L, "CALML5"));
        daoGene.addGene(new CanonicalGene(343035L, "RD3"));
        daoGene.addGene(new CanonicalGene(80114L, "BICC1"));
        daoGene.addGene(new CanonicalGene(219736L, "STOX1"));
        daoGene.addGene(new CanonicalGene(6609L, "SMPD1"));
        daoGene.addGene(new CanonicalGene(51259L, "TMEM216"));
        daoGene.addGene(new CanonicalGene(89L, "ACTN3"));
        daoGene.addGene(new CanonicalGene(64581L, "CLEC7A"));
        daoGene.addGene(new CanonicalGene(50839L, "TAS2R10"));
        daoGene.addGene(new CanonicalGene(54407L, "SLC38A2"));
        daoGene.addGene(new CanonicalGene(6667L, "SP1"));
        daoGene.addGene(new CanonicalGene(2842L, "GPR19"));

	    // additional genes for "data_mutations_oncotated.txt"
	    daoGene.addGene(new CanonicalGene(55138L, "FAM90A1"));
	    daoGene.addGene(new CanonicalGene(10628L, "TXNIP"));
	    daoGene.addGene(new CanonicalGene(80343, "SEL1L2"));
	    daoGene.addGene(new CanonicalGene(29102L, "DROSHA"));
	    daoGene.addGene(new CanonicalGene(7204L, "TRIO"));
	    daoGene.addGene(new CanonicalGene(57111L, "RAB25"));
	    daoGene.addGene(new CanonicalGene(773L, "CACNA1A"));
	    daoGene.addGene(new CanonicalGene(100132025L, "LOC100132025"));
	    daoGene.addGene(new CanonicalGene(1769L, "DNAH8"));
	    daoGene.addGene(new CanonicalGene(343171L, "OR2W3"));
	    daoGene.addGene(new CanonicalGene(2901L, "GRIK5"));
	    daoGene.addGene(new CanonicalGene(10568L, "SLC34A2"));
	    daoGene.addGene(new CanonicalGene(140738L, "TMEM37"));
	    daoGene.addGene(new CanonicalGene(94025L, "MUC16"));
	    daoGene.addGene(new CanonicalGene(1915L, "EEF1A1"));
	    daoGene.addGene(new CanonicalGene(65083L, "NOL6"));
	    daoGene.addGene(new CanonicalGene(7094L, "TLN1"));
	    daoGene.addGene(new CanonicalGene(51196L, "PLCE1"));
	    daoGene.addGene(new CanonicalGene(1952L, "CELSR2"));
	    daoGene.addGene(new CanonicalGene(2322L, "FLT3"));
	    daoGene.addGene(new CanonicalGene(867L, "CBL"));
            
            MySQLbulkLoader.flushAll();
    }
}