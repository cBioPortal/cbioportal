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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.MutSig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.io.IOException;

/**
 * @author Lennart Bastian
 */


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestDaoMutSig {

    /**
     * Tests DaoGene and DaoGeneOptimized.
     *
     * @throws org.mskcc.cbio.portal.dao.DaoException
     *          Database Error.
     */

	@Test
    public void testDaoMutSig() throws DaoException, IOException {
        
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

        // Add Gene TP53 to both gene table and mut_sig table
        CanonicalGene gene1 = new CanonicalGene(10298321, "TP53");
        daoGeneOptimized.addGene(gene1);
        MutSig tp53 = new MutSig(1, gene1, 1, 145177, 48, 1E-11f, 1E-8f);

        // Add Gene PTEN to both gene table and mut_sig table
        CanonicalGene gene2 = new CanonicalGene(10298321, "PTEN");
        daoGeneOptimized.addGene(gene2);
        MutSig pten = new MutSig(1, gene2, 2, 156252, 34, 1E-11f, 1E-8f);
        DaoMutSig.addMutSig(pten);

        //get tp53 from mutsig table using hugoGeneSymbol
        MutSig mutSig = DaoMutSig.getMutSig("TP53", 1);
        CanonicalGene testGene = mutSig.getCanonicalGene();
        assertTrue("TP53".equals(testGene.getHugoGeneSymbolAllCaps()));
        assertEquals(1, mutSig.getCancerType());
        
        //get pten from mutsig table using entrez ID
        long foo = 10298321;
        MutSig mutSig2 = DaoMutSig.getMutSig(foo, 1);
        CanonicalGene testGene2 = mutSig2.getCanonicalGene();
        assertEquals(10298321, testGene2.getEntrezGeneId());
        assertEquals(1, mutSig2.getCancerType());
    }
}
