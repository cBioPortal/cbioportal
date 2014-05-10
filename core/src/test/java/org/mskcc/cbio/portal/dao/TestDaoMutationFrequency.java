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

import junit.framework.TestCase;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoMutationFrequency;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.scripts.ResetDatabase;
import org.mskcc.cbio.portal.model.CanonicalGene;

import java.util.ArrayList;

/**
 * JUnit test for DaoMutationFrequency class.
 */
public class TestDaoMutationFrequency extends TestCase {

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