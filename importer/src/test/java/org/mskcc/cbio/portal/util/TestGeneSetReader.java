/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.util;

import java.io.*;
import java.util.*;
import org.mskcc.cbio.portal.model.SetOfGenes;
import static org.junit.Assert.assertEquals;

/**
 * JUnit test for the Gene Set Reader.
 */
public class TestGeneSetReader {

    /**
     * Tests the Gene Set Reader Class.
     *
     * @throws java.io.IOException IO Error.
     */
    public void testGeneSetReader() throws IOException {
		// TBD: change this to use getResourceAsStream()
        File file = new File("target/test-classes/gene_sets.txt");
        FileInputStream fin = new FileInputStream (file);

        ArrayList<SetOfGenes> geneSetList = GeneSetReader.readGeneSets(fin);
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
