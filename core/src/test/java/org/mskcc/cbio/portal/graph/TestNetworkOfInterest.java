/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.graph;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.dao.DaoInteraction;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.util.ArrayList;

import edu.uci.ics.jung.graph.Graph;

/**
 * JUnit Tests for Network Of Interest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestNetworkOfInterest {

    /**
     * Test the Network Of Interaction Class.
     *
     * @throws org.mskcc.cbio.portal.dao.DaoException Database Error
     */
	@Test
    public void testDaoInteraction() throws DaoException {

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
