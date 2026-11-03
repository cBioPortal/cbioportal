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
 * Reads in Gene Sets from an InputStream.
 *
 * @author Ethan Cerami.
 */
public class GeneSetReader {

    /**
     * Constructor.
     *
     * @param in    InputStream Object.
     * @return ArrayList of GeneSet Objects.
     * @throws IOException  IO Error.
     */
    public static ArrayList<SetOfGenes> readGeneSets (InputStream in) throws IOException {
        ArrayList<SetOfGenes> geneSetList = new ArrayList<SetOfGenes>();

        //  User-Defined Gene Set Goes First
        SetOfGenes g0 = new SetOfGenes();
        g0.setName("User-defined List");
        g0.setGeneList("");
        geneSetList.add(g0);

        BufferedReader bufReader = new BufferedReader(new InputStreamReader(in));
        String line = bufReader.readLine();
        while (line != null) {
            line = line.trim();
            String parts[] = line.split("=");
            g0 = new SetOfGenes();
            String genes[] = parts[1].split("\\s");

            //  Store number of genes in the gene name
            g0.setName(parts[0] + " (" + genes.length + " genes)");
            g0.setGeneList(parts[1]);
            geneSetList.add(g0);
            line = bufReader.readLine();
        }
        return geneSetList;
    }
}
