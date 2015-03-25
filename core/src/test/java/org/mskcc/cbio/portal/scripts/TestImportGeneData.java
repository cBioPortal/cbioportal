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

import junit.framework.TestCase;
import org.mskcc.cbio.portal.util.GlobalProperties;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.scripts.ImportGeneData;
import org.mskcc.cbio.portal.scripts.ResetDatabase;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.io.File;

/**
 * JUnit tests for ImportGeneData class.
 */
public class TestImportGeneData extends TestCase {

    private static String geneDataFilename = null;
    private static String suppGeneDataFilename = null;
    static {
        String home = System.getenv(GlobalProperties.HOME_DIR);
        if (home != null) {
            geneDataFilename = home + File.separator + "core/target/test-classes/genes_test.txt";
            suppGeneDataFilename = home + File.separator + "core/target/test-classes/supp-genes.txt";
        }
    }

    public void testImportGeneData() throws Exception {
        ResetDatabase.resetDatabase();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);
		// TBD: change this to use getResourceAsStream()
        if (suppGeneDataFilename!=null) {
            File file = new File(suppGeneDataFilename);
            ImportGeneData.importSuppGeneData(pMonitor, file);
        }
        
        if (geneDataFilename != null) {
            File file = new File(geneDataFilename);
            ImportGeneData.importData(pMonitor, file);

            CanonicalGene gene = daoGene.getGene(10);
            assertEquals("NAT2", gene.getHugoGeneSymbolAllCaps());
            gene = daoGene.getGene(15);
            assertEquals("AANAT", gene.getHugoGeneSymbolAllCaps());

            gene = daoGene.getGene("ABCA3");
            assertEquals(21, gene.getEntrezGeneId());
        }
        else {
            throw new IllegalArgumentException("Cannot find test gene file, is PORTAL_HOME set?");
        }
    }
}
