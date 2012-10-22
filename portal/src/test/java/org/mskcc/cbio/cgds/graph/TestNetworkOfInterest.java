/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.cgds.graph;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.MySQLbulkLoader;
import org.mskcc.cbio.cgds.dao.DaoInteraction;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.graph.NetworkOfInterest;

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
