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

package org.mskcc.cbio.portal.dao;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * JUnit test for DaoMutationFrequency class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestDaoMutationFrequency {

	@Ignore
	@Test
    public void testDaoMutationFrequency() throws DaoException {
        // DaoMutationFrequency is not used any more
//        ResetDatabase.resetDatabase();
//        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
//        daoGene.addGene(new CanonicalGene(672, "BRCA1"));
//        daoGene.addGene(new CanonicalGene(675, "BRCA2"));
//
//        DaoMutationFrequency daoMutationFrequency = new DaoMutationFrequency();
//        daoMutationFrequency.addGene(672, 0.06, 2);
//        daoMutationFrequency.addGene(675, 0.10, 2);
//
//        ArrayList <CanonicalGene> list = daoMutationFrequency.getTop100SomaticMutatedGenes(2);
//        assertEquals (2, list.size());
//        CanonicalGene gene0 = list.get(0);
//        assertEquals (675, gene0.getEntrezGeneId());
//        assertEquals ("BRCA2", gene0.getHugoGeneSymbolAllCaps());
//        assertEquals (0.10, gene0.getSomaticMutationFrequency(), 0.0001);
//
//        CanonicalGene gene1 = list.get(1);
//        assertEquals (672, gene1.getEntrezGeneId());
//        assertEquals ("BRCA1", gene1.getHugoGeneSymbolAllCaps());
//        assertEquals (0.06, gene1.getSomaticMutationFrequency(), 0.0001);
//        
//        daoMutationFrequency.deleteAllRecords();
//        assertEquals ( null, daoMutationFrequency.getSomaticMutationFrequency( 672 ) );
    }
}