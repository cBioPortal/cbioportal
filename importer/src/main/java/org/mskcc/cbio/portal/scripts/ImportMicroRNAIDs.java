/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;

import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 * Command Line Tool to Import Background Gene Data.
 */
public class ImportMicroRNAIDs {

    public static void importData(File geneFile) throws IOException, DaoException {
        ClickHouseBulkLoader.bulkLoadOff();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        List<CanonicalGene> mirnas = new ArrayList<CanonicalGene>();

        try (FileReader reader = new FileReader(geneFile);
             BufferedReader buf = new BufferedReader(reader)) {
            String line = buf.readLine(); // skip first line
            while ((line=buf.readLine()) != null) {
                ProgressMonitor.incrementCurValue();
                ConsoleUtil.showProgress();
                if (!line.startsWith("#")) {
                    String parts[] = line.split("\t");
                    String geneSymbol = parts[2];
                    Set<String> aliases = new HashSet<String>();
                    setAliases(parts[0],aliases);
                    if (!parts[0].equalsIgnoreCase(parts[1])) {
                        setAliases(parts[1],aliases);
                    }
                    CanonicalGene mirna = new CanonicalGene(geneSymbol,aliases);
                    mirna.setType(CanonicalGene.MIRNA_TYPE);
                    mirnas.add(mirna);
                }
            }
        }

        for (CanonicalGene mirna : mirnas) {
            daoGene.addGene(mirna);
        }
    }
    
    private static void setAliases(String hsa, Set<String> aliases) {
        aliases.add(hsa);
        if (hsa.startsWith("hsa-")) {
            String mir = hsa.substring(4).toUpperCase();
            aliases.add(mir);
            aliases.add(getHUGOInNCBIFile(mir));
        }
    }
    
    private static  String getHUGOInNCBIFile(String mir) {
        StringBuilder sb = new StringBuilder();
        sb.append("MIR");
        if (mir.startsWith("LET")) {
            sb.append("LET");
        }
        
        int ix = mir.indexOf("-");
        sb.append(mir.substring(ix+1));
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        System.err.println("This script will be called from ImportGeneData");
//        if (args.length == 0) {
//            System.out.println("command line usage:  importMicroRNAIDs.pl <microrna.txt>");
//            return;
//        }
//        ProgressMonitor.setConsoleMode(true);
//
//        File geneFile = new File(args[0]);
//        System.out.println("Reading data from:  " + geneFile.getAbsolutePath());
//        int numLines = FileUtil.getNumLines(geneFile);
//        System.out.println(" --> total number of lines:  " + numLines);
//        ProgressMonitor.setMaxValue(numLines);
//        ImportMicroRNAIDs.importData(geneFile);
//        ConsoleUtil.showWarnings();
//        System.err.println("Done.");
    }
}
