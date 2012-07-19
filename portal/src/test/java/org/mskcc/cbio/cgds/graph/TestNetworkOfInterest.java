package org.mskcc.cbio.cgds.test.graph;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.MySQLbulkLoader;
import org.mskcc.cgds.dao.DaoInteraction;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.graph.NetworkOfInterest;

import java.util.ArrayList;

import edu.uci.ics.jung.graph.Graph;

/**
 * JUnit Tests for Network Of Interest.
 */
public class TestNetworkOfInterest extends TestCase {

    /**
     * Test the Network Of Interaction Class.
     *
     * @throws org.mskcc.cgds.dao.DaoException Database Error
     */
    public void testDaoInteraction() throws DaoException {
        ResetDatabase.resetDatabase();
        MySQLbulkLoader.bulkLoadOff();
        DaoInteraction daoInteraction = DaoInteraction.getInstance();

        CanonicalGene brca1 = new CanonicalGene (672, "BRCA1");
        CanonicalGene brca2 = new CanonicalGene (675, "BRCA2");
        CanonicalGene tp53 = new CanonicalGene (7157, "TP53");
        CanonicalGene pten = new CanonicalGene (5728, "PTEN");

        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.addGene(brca1);
        daoGene.addGene(brca2);
        daoGene.addGene(tp53);
        daoGene.addGene(pten);

        daoInteraction.addInteraction(brca1, brca2, "pp", "HPRD", "Y2H", "12344");
        daoInteraction.addInteraction(brca1, tp53, "pp", "HPRD", "Y2H", "12344");
        daoInteraction.addInteraction(brca1, pten, "pp", "HPRD", "Y2H", "12344");
        daoInteraction.addInteraction(tp53, pten, "pp", "HPRD", "Y2H", "12344");

        ArrayList<CanonicalGene> geneList = new ArrayList<CanonicalGene>();
        geneList.add(brca1);
        geneList.add(brca2);
        geneList.add(tp53);
        geneList.add(pten);
        NetworkOfInterest noi = new NetworkOfInterest(geneList);
        Graph<String, String> graph = noi.getGraph();

        assertEquals (4, graph.getVertexCount());
        assertEquals (4, graph.getEdgeCount());
    }

}
