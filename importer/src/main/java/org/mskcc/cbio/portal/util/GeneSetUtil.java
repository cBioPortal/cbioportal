/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.util;

import java.io.*;
import java.util.*;
import org.mskcc.cbio.portal.model.SetOfGenes;

/**
 * Singleton to store arbitrary gene sets, to be displayed to the user.
 * <p/>
 * This will (eventually) be replaced with live gene sets from Broad MSigDB and Pathway Commons.
 *
 * @author Ethan Cerami
 */
public class GeneSetUtil {
    private ArrayList<SetOfGenes> geneSetList = new ArrayList<SetOfGenes>();
    private static GeneSetUtil geneSetUtil;

    /**
     * Gets Global Singleton.
     * @return GeneSetUtil.
     * @throws IOException IO Error.
     */
    public static GeneSetUtil getInstance() throws IOException {
        if (geneSetUtil == null) {
            geneSetUtil = new GeneSetUtil();
        }
        return geneSetUtil;
    }

    /**
     * Private Constructor.
     * @throws IOException IO Error.
     */
    private GeneSetUtil() throws IOException {
        InputStream in = this.getClass().getResourceAsStream("/gene_sets.txt");
        geneSetList = GeneSetReader.readGeneSets(in);
        in.close();
    }

    /**
     * Gets all Gene Sets.
     *
     * @return ArrayList of GeneSet Objects.
     */
    public ArrayList<SetOfGenes> getGeneSetList() {
        return geneSetList;
    }
}
