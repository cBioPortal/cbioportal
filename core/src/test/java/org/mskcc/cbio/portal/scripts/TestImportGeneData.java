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

    private static String geneDataFilename = initializeGeneDataFilename();
    private static String initializeGeneDataFilename()
    {
        String home = System.getenv(GlobalProperties.HOME_DIR);
        return (home != null) ? 
            home + File.separator + "core/target/test-classes/genes_test.txt" : null;
    }

    public void testImportGeneData() throws Exception {
        ResetDatabase.resetDatabase();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);
		// TBD: change this to use getResourceAsStream()
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
