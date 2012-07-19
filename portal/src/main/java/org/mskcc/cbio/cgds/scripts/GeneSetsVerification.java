
package org.mskcc.cbio.cgds.scripts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.mskcc.cbio.portal.model.GeneSet;
import org.mskcc.cbio.portal.util.GeneSetUtil;

/**
 * The class verify if the gene symbols in the sample gene sets are the latest
 * HUPO gene symbols.
 * @author jgao
 */
public class GeneSetsVerification {
    private String ncbiGeneFile;
    
    public GeneSetsVerification(String ncbiGeneFile) {
        this.ncbiGeneFile = ncbiGeneFile;
    }
    
    /**
     * Verify if genes in gene sets are all latest HUPO gene symbols.
     * @return true of all updated, otherwise, false.
     * @throws IOException 
     */
    public boolean verify() throws IOException {
        Map<String,List<String>> geneSetMap = getGeneSet();
        Set<String> ncbiSymbols = getNCBISymbols();
        
        // go over all genes in geneset
        boolean ret = true;
        for (Map.Entry<String,List<String>> entry : geneSetMap.entrySet()) {
            String geneSetName = entry.getKey();
            for (String gene : entry.getValue()) {
                if (!ncbiSymbols.contains(gene)) {
                    ret = false;
                    System.err.println("Gene symbol "+gene+" is out of date "
                            + "(in gene set of " + geneSetName + ")");
                }
            }
        }
        return ret;
    }
    
    private Map<String,List<String>> getGeneSet() throws IOException {
        GeneSetUtil geneSetUtil = GeneSetUtil.getInstance();
        ArrayList<GeneSet> geneSetList = geneSetUtil.getGeneSetList();
        Map<String,List<String>> map = new LinkedHashMap<String,List<String>>(geneSetList.size());
        for (GeneSet geneSet : geneSetList) {
            String geneSetName = geneSet.getName();
            String geneList = geneSet.getGeneList();
            map.put(geneSetName, Arrays.asList(geneList.split(" ")));
        }
        return map;
    }
    
    private Set<String> getNCBISymbols() throws IOException {
        Set<String> set = new HashSet<String>();
        FileReader reader = new FileReader(ncbiGeneFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        while (line != null) {
            if (!line.startsWith("#")) {
                String[] parts = line.split("\t");
                set.add(parts[2]);
            }
            line = buf.readLine();
        }
        set.add("");
        return set;
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("command line usage:  verifyGeneSets.pl"
                    + " <ncbi_genes.txt>");
            System.exit(1);
        }
        
        String ncbiGeneFile = args[0];
        GeneSetsVerification geneSetsVerification
                = new GeneSetsVerification(ncbiGeneFile);
        if (!geneSetsVerification.verify()) {
            System.out.println("Please update the gene symbols in the sample"
                    + " gene sets. \nPress enter to continue...");
            Scanner keyboard = new Scanner(System.in);
            keyboard.nextLine();
            System.exit(1);
        }
    }
}
