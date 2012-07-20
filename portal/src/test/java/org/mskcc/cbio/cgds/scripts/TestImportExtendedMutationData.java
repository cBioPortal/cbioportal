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
            File file = new File("test_data/data_mutations_extended.txt");
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
            parser = new ImportExtendedMutationData(file, 1, pMonitor, "test_data/test_germline_white_list_file2.txt");
            // put on: CLEC7A
            parser.importData();
            checkBasicFilteringRules();
            checkGermlineMutations();
            acceptEverythingElse();


            loadGenes();
            parser = new ImportExtendedMutationData(file, 1, pMonitor, "test_data/test_germline_white_list_file2.txt");
            parser.importData();
            checkBasicFilteringRules();
            checkGermlineMutations();
            acceptEverythingElse();
            validateMutationAminoAcid (1, "TCGA-AA-3664", 54407, "T433A");


            loadGenes();
            parser = new ImportExtendedMutationData(file, 1, pMonitor, "test_data/test_germline_white_list_file2.txt");
            parser.importData();
            checkBasicFilteringRules();
            checkGermlineMutations();
            acceptEverythingElse();
            validateMutationAminoAcid(1, "TCGA-AA-3664", 54407, "T433A");
            // Unknown  Somatic mutations on somatic whitelist
            validateMutationAminoAcid(1, "TCGA-AA-3664", 6667, "A513V");
            // Unknown  Somatic mutations on somatic whitelist2

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

    }
}