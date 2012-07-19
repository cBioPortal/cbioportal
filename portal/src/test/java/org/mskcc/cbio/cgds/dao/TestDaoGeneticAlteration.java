package org.mskcc.cbio.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.*;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.model.CanonicalGene;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;

/**
 * JUnit tests for DaoGeneticAlteration class.
 */
public class TestDaoGeneticAlteration extends TestCase {

    public void testDaoGeneticAlteration() throws DaoException {
        
        // test with both values of MySQLbulkLoader.isBulkLoad()
        MySQLbulkLoader.bulkLoadOff();
        runTheTest();
        MySQLbulkLoader.bulkLoadOn();
        runTheTest();
    }
    
    private void runTheTest() throws DaoException{
        //  Add Gene
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.addGene(new CanonicalGene (672, "BRCA1"));

        ResetDatabase.resetDatabase();
        DaoGeneticProfileCases daoGeneticProfileCases = new DaoGeneticProfileCases();

        //  Add the Case List
        ArrayList<String> orderedCaseList = new ArrayList<String>();
        orderedCaseList.add("TCGA-1");
        orderedCaseList.add("TCGA-2");
        orderedCaseList.add("TCGA-3");
        orderedCaseList.add("TCGA-4");
        int numRows = daoGeneticProfileCases.addGeneticProfileCases(1, orderedCaseList);
        assertEquals (1, numRows);

        //  Add Some Data
        String data = "200:400:600:800";
        String values[] = data.split(":");
        DaoGeneticAlteration dao = DaoGeneticAlteration.getInstance();
        numRows = dao.addGeneticAlterations(1, 672, values);
        assertEquals (1, numRows);

        // if bulkLoading, execute LOAD FILE
        if( MySQLbulkLoader.isBulkLoad()){
            dao.flushGeneticAlteration();
        }

        HashMap<String, String> valueMap = dao.getGeneticAlterationMap(1, 672);
        assertEquals ("200", valueMap.get("TCGA-1"));
        assertEquals ("400", valueMap.get("TCGA-2"));
        assertEquals ("600", valueMap.get("TCGA-3"));
        assertEquals ("800", valueMap.get("TCGA-4"));

        //  Test the getGenesInProfile method
        Set <CanonicalGene> geneSet = dao.getGenesInProfile(1);
        ArrayList <CanonicalGene> geneList = new ArrayList <CanonicalGene> (geneSet);
        assertEquals (1, geneList.size());
        CanonicalGene gene = geneList.get(0);
        assertEquals ("BRCA1", gene.getHugoGeneSymbolAllCaps());
        assertEquals (672, gene.getEntrezGeneId());
    }

}
