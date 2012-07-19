package org.mskcc.cbio.portal.util;

import org.mskcc.portal.model.GeneSet;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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
    public static ArrayList<GeneSet> readGeneSets (InputStream in) throws IOException {
        ArrayList<GeneSet> geneSetList = new ArrayList<GeneSet>();

        //  User-Defined Gene Set Goes First
        GeneSet g0 = new GeneSet();
        g0.setName("User-defined List");
        g0.setGeneList("");
        geneSetList.add(g0);

        BufferedReader bufReader = new BufferedReader(new InputStreamReader(in));
        String line = bufReader.readLine();
        while (line != null) {
            line = line.trim();
            String parts[] = line.split("=");
            g0 = new GeneSet();
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