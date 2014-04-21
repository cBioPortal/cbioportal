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

package org.mskcc.cbio.portal.util;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.model.GeneSet;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * JUnit test for the Gene Set Reader.
 */
public class TestGeneSetReader extends TestCase {

    /**
     * Tests the Gene Set Reader Class.
     *
     * @throws java.io.IOException IO Error.
     */
    public void testGeneSetReader() throws IOException {
		// TBD: change this to use getResourceAsStream()
        File file = new File("target/test-classes/gene_sets.txt");
        FileInputStream fin = new FileInputStream (file);

        ArrayList<GeneSet> geneSetList = GeneSetReader.readGeneSets(fin);
        assertEquals (23, geneSetList.size());

        //  Verify that the correct # of genes are inserted
        assertEquals ("Prostate Cancer: AR Signaling (10 genes)",
            geneSetList.get(1).getName());
        assertEquals ("Prostate Cancer: AR and steroid synthesis enzymes (30 genes)",
            geneSetList.get(2).getName());

        assertEquals ("SOX9 RAN TNK2 EP300 PXN NCOA2 AR NRIP1 NCOR1 NCOR2",
            geneSetList.get(1).getGeneList());
    }
}