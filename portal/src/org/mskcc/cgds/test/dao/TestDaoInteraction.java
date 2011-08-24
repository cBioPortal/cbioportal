package org.mskcc.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoInteraction;
import org.mskcc.cgds.dao.MySQLbulkLoader;
import org.mskcc.cgds.dao.DaoMicroRnaAlteration;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.Interaction;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * JUnit Tests for DaoInteraction.
 */
public class TestDaoInteraction extends TestCase {

    /**
     * Test the DaoInteraction Class.
     *
     * @throws DaoException Database Error
     */
    public void testDaoInteraction() throws DaoException {

        // test with both values of MySQLbulkLoader.isBulkLoad()
        MySQLbulkLoader.bulkLoadOff();
        runTheTest();
        MySQLbulkLoader.bulkLoadOn();
        runTheTest();
    }

    private void runTheTest() throws DaoException{
        ResetDatabase.resetDatabase();
        DaoInteraction daoInteraction = DaoInteraction.getInstance();

        CanonicalGene geneA = new CanonicalGene (672, "BRCA1");
        CanonicalGene geneB = new CanonicalGene (675, "BRCA2");

        int recordsAdded = daoInteraction.addInteraction(geneA, geneB, "pp", "HPRD",
                "Y2H", "12344");
        assertEquals (1, recordsAdded);

        recordsAdded = daoInteraction.addInteraction(geneA, geneB, "state_change", "REACTOME",
                "in-vivo", "12355");
        assertEquals (1, recordsAdded);

        // if bulkLoading, execute LOAD FILE
        if( MySQLbulkLoader.isBulkLoad()){
            daoInteraction.flushToDatabase();
        }

        //  Get the interactions back
        ArrayList<Interaction> interactionList = daoInteraction.getAllInteractions();
        assertEquals (2, interactionList.size());

        Interaction interaction1 = interactionList.get(0);
        assertEquals (672, interaction1.getGeneA());
        assertEquals (675, interaction1.getGeneB());
        assertEquals ("pp", interaction1.getInteractionType());
        assertEquals ("HPRD", interaction1.getSource());
        assertEquals ("Y2H", interaction1.getExperimentTypes());
        assertEquals ("12344", interaction1.getPmids());

        Interaction interaction2 = interactionList.get(1);
        assertEquals (672, interaction2.getGeneA());
        assertEquals (675, interaction2.getGeneB());
        assertEquals ("state_change", interaction2.getInteractionType());
        assertEquals ("REACTOME", interaction2.getSource());
        assertEquals ("in-vivo", interaction2.getExperimentTypes());
        assertEquals ("12355", interaction2.getPmids());

        //  Get the Interactions back by a direct query
        interactionList = daoInteraction.getInteractions(geneA);
        assertEquals (2, interactionList.size());
    }

}
