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

package org.mskcc.cbio.portal.scripts;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoReferenceGenomeGene;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ReferenceGenome;
import org.mskcc.cbio.portal.model.ReferenceGenomeGene;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * JUnit tests for ImportGeneData class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestImportGeneData {

    @Test
    /*
     * Checks that ImportGeneData works by calculating the length from three genes 
     * in genes_test.txt. The file genes_test.txt contains real data.
     */
    public void testImportGeneData() throws Exception {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        ProgressMonitor.setConsoleMode(false);
        
        File file = new File("src/test/resources/genes_test.txt");
        ImportGeneData.importData(file, "GRCh37");

        CanonicalGene gene = daoGene.getGene(10);
        assertEquals("NAT2", gene.getHugoGeneSymbolAllCaps());
        gene = daoGene.getGene(15);
        assertEquals("AANAT", gene.getHugoGeneSymbolAllCaps());

        gene = daoGene.getGene("ABCA3");
        assertEquals(21, gene.getEntrezGeneId());
    }
}
