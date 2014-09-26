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

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.BitSet;

/**
 * Command Line Tool to Import Background Gene Data.
 */
public class ImportGeneData {

    public static void importData(ProgressMonitor pMonitor, File geneFile) throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOn();
        FileReader reader = new FileReader(geneFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        while (line != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t");
                int entrezGeneId = Integer.parseInt(parts[1]);
                String geneSymbol = parts[2];
                String locusTag = parts[3];
                String strAliases = parts[4];
                String strXrefs = parts[5];
                String cytoband = parts[7];
                String desc = parts[8];
                String type = parts[9];
                Set<String> aliases = new HashSet<String>();
                if (!locusTag.equals("-")) {
                    aliases.add(locusTag);
                }
                if (!strAliases.equals("-")) {
                    aliases.addAll(Arrays.asList(strAliases.split("\\|")));
                }
                
                if (geneSymbol.startsWith("MIR") && type.equalsIgnoreCase("miscRNA")) {
                    line = buf.readLine();
                    continue; // ignore miRNA; process seperately
                }
                
                CanonicalGene gene = new CanonicalGene(entrezGeneId, geneSymbol,
                        aliases);
                if (!cytoband.equals("-")) {
                    gene.setCytoband(cytoband);
                }
                gene.setType(type);
                daoGene.addGene(gene);
            }
            line = buf.readLine();
        }
        reader.close();
        if (MySQLbulkLoader.isBulkLoad()) {
           MySQLbulkLoader.flushAll();
        }        
    }

    private static void importGeneLength(ProgressMonitor pMonitor, File geneFile) throws IOException, DaoException {
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        FileReader reader = new FileReader(geneFile);
        BufferedReader buf = new BufferedReader(reader);
        String line;
        CanonicalGene currentGene = null;
        List<long[]> loci = new ArrayList<long[]>();
        while ((line=buf.readLine()) != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t");
                CanonicalGene gene = daoGeneOptimized.getNonAmbiguousGene(parts[3], parts[0]);
                if (gene==null) {
                    System.err.println("Could not find non ambiguous gene: "+parts[3]);
                    continue;
                }
                
                if (currentGene != gene) {
                    if (currentGene!=null) {
                        int length = calculateGeneLength(loci);
                        if (currentGene.getLength()!=0) {
                            System.err.println(currentGene.getHugoGeneSymbolAllCaps()+" has multiple length.");
                        } else {
                            currentGene.setLength(length);
                        }
                    }
                    loci.clear();
                    currentGene = gene;
                }
                
                loci.add(new long[]{Long.parseLong(parts[1]), Long.parseLong(parts[2])});
            }
        }
        
        daoGeneOptimized.flushUpdateToDatabase();
    }
    
    private static int calculateGeneLength(List<long[]> loci) {
        long min = Long.MAX_VALUE, max=-1;
        for (long[] l : loci) {
            if (l[0]<min) {
                min = l[0];
            }
            if (l[1]>max) {
                max = l[1];
            }
        }
        BitSet bitSet = new BitSet((int)(max-min));
        for (long[] l : loci) {
            bitSet.set((int)(l[0]-min), ((int)(l[1]-min)));
        }
        
        return bitSet.cardinality();
    }
    
    private static void importSuppGeneData(ProgressMonitor pMonitor, File suppGeneFile) throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(suppGeneFile);
        BufferedReader buf = new BufferedReader(reader);
        String line;
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        while ((line = buf.readLine()) != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t");
                CanonicalGene gene = new CanonicalGene(parts[0]);
                if (!parts[1].isEmpty()) {
                    gene.setType(parts[1]);
                }
                if (!parts[2].isEmpty()) {
                    gene.setCytoband(parts[2]);
                }
                if (!parts[3].isEmpty()) {
                    gene.setLength(Integer.parseInt(parts[3]));
                }
                daoGene.addGene(gene);
            }
            line = buf.readLine();
        }
        reader.close(); 
    }

    public static void main(String[] args) throws Exception {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.deleteAllRecords();
        if (args.length == 0) {
            System.out.println("command line usage:  importGenes.pl <ncbi_genes.txt> <supp-genes.txt> <microrna.txt> <all_exon_loci.bed>");
            return;
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);
        

        File geneFile = new File(args[0]);
        System.out.println("Reading gene data from:  " + geneFile.getAbsolutePath());
        int numLines = FileUtil.getNumLines(geneFile);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportGeneData.importData(pMonitor, geneFile);
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
        
        if (args.length>=2) {
            File suppGeneFile = new File(args[1]);
            System.out.println("Reading supp. gene data from:  " + suppGeneFile.getAbsolutePath());
            numLines = FileUtil.getNumLines(suppGeneFile);
            System.out.println(" --> total number of lines:  " + numLines);
            pMonitor.setMaxValue(numLines);
            ImportGeneData.importSuppGeneData(pMonitor, suppGeneFile);
        }
        
        if (args.length>=3) {
            File miRNAFile = new File(args[2]);
            System.out.println("Reading miRNA data from:  " + miRNAFile.getAbsolutePath());
            numLines = FileUtil.getNumLines(miRNAFile);
            System.out.println(" --> total number of lines:  " + numLines);
            pMonitor.setMaxValue(numLines);
            ImportMicroRNAIDs.importData(pMonitor, miRNAFile);
    }
        
        if (args.length>=4) {
            File lociFile = new File(args[3]);
            System.out.println("Reading loci data from:  " + lociFile.getAbsolutePath());
            numLines = FileUtil.getNumLines(lociFile);
            System.out.println(" --> total number of lines:  " + numLines);
            pMonitor.setMaxValue(numLines);
            ImportGeneData.importGeneLength(pMonitor, lociFile);
        }
    }
}
