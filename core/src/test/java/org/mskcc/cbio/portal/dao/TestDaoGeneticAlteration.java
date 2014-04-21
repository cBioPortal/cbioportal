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

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

import junit.framework.TestCase;

import java.util.*;

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
        ArrayList<Integer> internalSampleIds = createSamples();

        //  Add the Sample List
        int numRows = DaoGeneticProfileSamples.addGeneticProfileSamples(1, internalSampleIds);
        assertEquals (1, numRows);

        //  Add Some Data
        String data = "200:400:600:800";
        String values[] = data.split(":");
        DaoGeneticAlteration dao = DaoGeneticAlteration.getInstance();
        numRows = dao.addGeneticAlterations(1, 672, values);
        assertEquals (1, numRows);

        // if bulkLoading, execute LOAD FILE
        if( MySQLbulkLoader.isBulkLoad()){
           MySQLbulkLoader.flushAll();
        }

        HashMap<Integer, String> valueMap = dao.getGeneticAlterationMap(1, 672);
        assertEquals ("200", valueMap.get(1));
        assertEquals ("400", valueMap.get(2));
        assertEquals ("600", valueMap.get(3));
        assertEquals ("800", valueMap.get(4));

        //  Test the getGenesInProfile method
        Set <CanonicalGene> geneSet = dao.getGenesInProfile(1);
        ArrayList <CanonicalGene> geneList = new ArrayList <CanonicalGene> (geneSet);
        assertEquals (1, geneList.size());
        CanonicalGene gene = geneList.get(0);
        assertEquals ("BRCA1", gene.getHugoGeneSymbolAllCaps());
        assertEquals (672, gene.getEntrezGeneId());
    }

    private ArrayList<Integer> createSamples() throws DaoException {
        ArrayList<Integer> toReturn = new ArrayList<Integer>();
        CancerStudy study = new CancerStudy("study", "description", "id", "brca", true);
        Patient p = new Patient(study, "TCGA-1");
        int pId = DaoPatient.addPatient(p);
        Sample s = new Sample("TCGA-1-1-01", pId, "type");
        toReturn.add(DaoSample.addSample(s));
        s = new Sample("TCGA-1-2-01", pId, "type");
        toReturn.add(DaoSample.addSample(s));
        s = new Sample("TCGA-1-3-01", pId, "type");
        toReturn.add(DaoSample.addSample(s));
        s = new Sample("TCGA-1-4-01", pId, "type");
        toReturn.add(DaoSample.addSample(s));
        return toReturn;
    }
}
