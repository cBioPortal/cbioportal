package org.mskcc.cbio.cgds.scripts;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.dao.DaoMutation;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.ExtendedMutation;
import org.mskcc.cbio.cgds.scripts.ImportExtendedMutationData;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.util.ProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class TestImportExtendedMutationData extends TestCase {

    public void testImportExtendedMutationData() {

        try {

            ProgressMonitor pMonitor = new ProgressMonitor();
            pMonitor.setConsoleMode(false);
			// TBD: change this to use getResourceAsStream()
            File file = new File("target/test-classes/data_mutations_extended.txt");
            ImportExtendedMutationData parser;

            try {
                parser = new ImportExtendedMutationData(file, 1, pMonitor, "no_such_germline_whitelistfile");
                Assert.fail("Should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                assertEquals("Gene list 'no_such_germline_whitelistfile' not found.", e.getMessage());
            }
            loadGenes();
            parser = new ImportExtendedMutationData(file, 1, pMonitor);
            parser.importData();
            checkBasicFilteringRules();
            
            // accept everything else
            validateMutationAminoAcid (1, "TCGA-AA-3664", 51806, "P113L");   // valid Unknown
            validateMutationAminoAcid (1, "TCGA-AA-3664", 89, "S116R"); // Unknown  Somatic

            loadGenes();
			// TBD: change this to use getResourceAsStream()
            parser = new ImportExtendedMutationData(file, 1, pMonitor, "target/test-classes/test_germline_white_list_file2.txt");
            // put on: CLEC7A
            parser.importData();
            checkBasicFilteringRules();
            checkGermlineMutations();
            acceptEverythingElse();


            loadGenes();
			// TBD: change this to use getResourceAsStream()
            parser = new ImportExtendedMutationData(file, 1, pMonitor, "target/test-classes/test_germline_white_list_file2.txt");
            parser.importData();
            checkBasicFilteringRules();
            checkGermlineMutations();
            acceptEverythingElse();
            validateMutationAminoAcid (1, "TCGA-AA-3664", 54407, "T433A");


            loadGenes();
			// TBD: change this to use getResourceAsStream()
            parser = new ImportExtendedMutationData(file, 1, pMonitor, "target/test-classes/test_germline_white_list_file2.txt");
            parser.importData();
            checkBasicFilteringRules();
            checkGermlineMutations();
            acceptEverythingElse();
            validateMutationAminoAcid(1, "TCGA-AA-3664", 54407, "T433A");
            // Unknown  Somatic mutations on somatic whitelist
            validateMutationAminoAcid(1, "TCGA-AA-3664", 6667, "A513V");
            // Unknown  Somatic mutations on somatic whitelist2

	        // additional tests for the MAF columns added after oncotator
	        loadGenes();
	        file = new File("target/test-classes/data_mutations_oncotated.txt");
	        parser = new ImportExtendedMutationData(file, 1, pMonitor);
	        parser.importData();
	        checkOncotatedImport();

        } catch (DaoException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // reject somatic mutations that aren't valid somatic, or on one of the somatic whitelists
    private void acceptEverythingElse() throws DaoException {
        DaoMutation daoMutation = DaoMutation.getInstance();
        assertEquals(1, daoMutation.getMutations(1, "TCGA-AA-3664", 51806).size());   // valid Unknown
        assertEquals(1, daoMutation.getMutations(1, "TCGA-AA-3664", 89).size()); // Unknown  Somatic
    }

    private void checkBasicFilteringRules() throws DaoException {
        rejectSilentLOHIntronWildtype();
        acceptValidSomaticMutations();
    }

	private void checkOncotatedImport() throws DaoException
	{
		DaoMutation daoMutation = DaoMutation.getInstance();
		ArrayList<ExtendedMutation> mutationList = daoMutation.getAllMutations();

		// assert table size; 3 silent mutations should be rejected
		assertEquals(17, mutationList.size());

		// assert data for oncotator columns
		assertEquals("FAM90A1", mutationList.get(0).getGeneSymbol());
		assertEquals("Missense_Mutation", mutationList.get(1).getOncotatorVariantClassification());
		assertEquals("p.R131H", mutationList.get(4).getOncotatorProteinChange());
		assertEquals("rs76360727;rs33980232", mutationList.get(10).getOncotatorDbSnpRs());
		assertEquals("p.E366_Q409del(13)|p.Q367R(1)|p.E366_K477del(1)", mutationList.get(16).getOncotatorCosmicOverlapping());
	}

    private void validateMutationAminoAcid (int geneticProfileId, String caseId, long entrezGeneId,
            String expectedAminoAcidChange) throws DaoException {
        DaoMutation daoMutation = DaoMutation.getInstance();
        ArrayList<ExtendedMutation> mutationList = daoMutation.getMutations
                (geneticProfileId, caseId, entrezGeneId);
        assertEquals(1, mutationList.size());
        assertEquals(expectedAminoAcidChange, mutationList.get(0).getAminoAcidChange());
    }

    private void acceptValidSomaticMutations() throws DaoException {
        DaoMutation daoMutation = DaoMutation.getInstance();

        // valid Somatic
        validateMutationAminoAcid (1, "TCGA-AA-3664", 282770, "R113C");

        // valid Somatic
        validateMutationAminoAcid (1, "TCGA-AA-3664", 51259, "G61G");
    }

    private void rejectSilentLOHIntronWildtype() throws DaoException {
        DaoMutation daoMutation = DaoMutation.getInstance();
        assertEquals(0, daoMutation.getMutations(1, "TCGA-AA-3664", 114548).size()); // silent
        assertEquals(0, daoMutation.getMutations(1, "TCGA-AA-3664", 343035).size()); // LOH
        assertEquals(0, daoMutation.getMutations(1, "TCGA-AA-3664", 80114).size()); // Wildtype
        assertEquals(0, daoMutation.getMutations(1, "TCGA-AA-3664", 219736).size()); // Wildtype
        assertEquals(0, daoMutation.getMutations(1, "TCGA-AA-3664", 6609).size()); // Intron
    }


    private void checkGermlineMutations() throws DaoException {
        DaoMutation daoMutation = DaoMutation.getInstance();
        assertEquals(0, daoMutation.getMutations(1, "TCGA-AA-3664", 64581).size());
        // missense, Germline mutation on germline whitelist

        // Germline mutation on germline whitelist
        validateMutationAminoAcid (1, "TCGA-AA-3664", 2842, "L113P");
        assertEquals(0, daoMutation.getMutations(1, "TCGA-AA-3664", 50839).size());
        // Germline mutations NOT on germline whitelist
    }

    private void loadGenes() throws DaoException {
        ResetDatabase.resetDatabase();
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
    }
}