/*
 * Copyright (c) 2020 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;

import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import java.util.*;

/**
 * JUnit Tests for DaoGenePanel.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestDaoGenePanel {

    /**
     * Tests DaoGenePanel.addGenePanel().
     * @throws DaoException Database Error.
     */
	@Test
    public void testAddGenePanel() throws DaoException {

		CanonicalGene brca1 = DaoGeneOptimized.getInstance().getGene("BRCA1");
		CanonicalGene brca2 = DaoGeneOptimized.getInstance().getGene("BRCA2");
		CanonicalGene kras = DaoGeneOptimized.getInstance().getGene("KRAS");
        HashSet<CanonicalGene> canonicalGenes = new HashSet<CanonicalGene>();
        canonicalGenes.add(brca1);
        canonicalGenes.add(brca2);
        canonicalGenes.add(kras);

        DaoGenePanel.addGenePanel("testGenePanel", "Test gene panel description", canonicalGenes);

        GenePanel genePanel = DaoGenePanel.getGenePanelByStableId("testGenePanel");
        assertTrue(genePanel != null);
        assertTrue(genePanel.getStableId().equals("testGenePanel"));
        assertTrue(genePanel.getDescription().equals("Test gene panel description"));
        assertTrue(genePanel.getDescription().equals("Test gene panel description"));
        assertEquals(genePanel.getGenes().size(), 3);

        DaoGenePanel.deleteGenePanel(genePanel);
        genePanel = DaoGenePanel.getGenePanelByStableId("testGenePanel");
        assertTrue(genePanel == null);
    }
}
