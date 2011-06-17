package org.mskcc.portal.util;

import org.mskcc.portal.model.GeneSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Temporary class which stores arbitrary gene sets, to be displayed to the user.
 * <p/>
 * This will be eventually replaced with live gene sets from Broad MSigDB and Pathway Commons.
 */
public class GeneSetUtil {
    private ArrayList<GeneSet> geneSetList = new ArrayList<GeneSet>();

    /**
     * Constructor.
     */
    public GeneSetUtil() {
        GeneSet g0 = new GeneSet();

        //  Custom Gene Set Goes First
        g0.setName("User-defined List");
        g0.setGeneList("");
        geneSetList.add(g0);
        g0 = new GeneSet();

        //  Load Gene Sets
        try {
            InputStream in = this.getClass().getResourceAsStream("gene_sets.txt");
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(in));
            String line = bufReader.readLine();
            while (line != null) {
                line = line.trim();
                String parts[] = line.split("=");
                g0 = new GeneSet();
                g0.setName(parts[0]);
                g0.setGeneList(parts[1]);
                geneSetList.add(g0);
                line = bufReader.readLine();
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets all Gene Sets.
     *
     * @return ArrayList of GeneSet Objects.
     */
    public ArrayList<GeneSet> getGeneSetList() {
        return geneSetList;
    }

}
