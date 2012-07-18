package org.mskcc.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.cgds.model.CanonicalGene;

import java.util.ArrayList;
import java.util.Set;

/**
 * JUnit tests for DaoMutation class.
 */
public class TestDaoMutation extends TestCase {

    public void testDaoMutation() throws DaoException {
        // test with both values of MySQLbulkLoader.isBulkLoad()
        MySQLbulkLoader.bulkLoadOff();
        runTheTest();
        MySQLbulkLoader.bulkLoadOn();
        runTheTest();
    }

    private void runTheTest() throws DaoException{
        //  Add a fake gene
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        CanonicalGene blahGene = new CanonicalGene(321, "BLAH");
        daoGene.addGene(blahGene);
        
        ResetDatabase.resetDatabase();
        DaoMutation dao = DaoMutation.getInstance();

        ExtendedMutation mutation = new ExtendedMutation();
        mutation.setGeneticProfileId(1);
        mutation.setCaseId("1234");
        mutation.setGene(blahGene);
        mutation.setValidationStatus("validated");
        mutation.setMutationStatus("somatic");
        mutation.setMutationType("missence");
        mutation.setChr("chr1");
        mutation.setStartPosition(10000);
        mutation.setEndPosition(20000);
        mutation.setSequencingCenter("Broad");
        mutation.setSequencer("SOLiD");
        mutation.setAminoAcidChange("BRCA1_123");
        mutation.setFunctionalImpactScore("H");
        mutation.setLinkXVar("link1");
        mutation.setLinkPdb("link2");
        mutation.setLinkMsa("link3");

        dao.addMutation(mutation);

        // if bulkLoading, execute LOAD FILE
        if( MySQLbulkLoader.isBulkLoad()){
            dao.flushMutations();
        }
        ArrayList<ExtendedMutation> mutationList = dao.getMutations(1, "1234", 321);
        validateMutation(mutationList.get(0));

        //  Test the getGenesInProfile method
        Set<CanonicalGene> geneSet = dao.getGenesInProfile(1);
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
        assertEquals ("missence", mutation.getMutationType());
        assertEquals ("chr1", mutation.getChr());
        assertEquals (10000, mutation.getStartPosition());
        assertEquals (20000, mutation.getEndPosition());
        assertEquals ("Broad", mutation.getSequencingCenter());
        assertEquals ("SOLiD", mutation.getSequencer());
        assertEquals ("BRCA1_123", mutation.getAminoAcidChange());
        assertEquals ("H", mutation.getFunctionalImpactScore());
        assertEquals ("link1", mutation.getLinkXVar());
        assertEquals ("link2", mutation.getLinkPdb());
        assertEquals ("link3", mutation.getLinkMsa());
    }
}