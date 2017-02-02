/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.util.*;

import java.io.*;
import java.util.*;

/**
 * Command Line Tool to Import Background Gene Data.
 */
public class ImportGeneData {

    public static void importData(File geneFile) throws IOException, DaoException {
        Map<String, Set<CanonicalGene>> genesWithSymbolFromNomenClatureAuthority = new LinkedHashMap<>();
        Map<String, Set<CanonicalGene>> genesWithoutSymbolFromNomenClatureAuthority = new LinkedHashMap<>();
        try (FileReader reader = new FileReader(geneFile)) {
            BufferedReader buf = new BufferedReader(reader);
            String line;
            while ((line = buf.readLine()) != null) {
                ProgressMonitor.incrementCurValue();
                ConsoleUtil.showProgress();
                if (line.startsWith("#")) {
                    continue;
                }
                
                	String species = GlobalProperties.getSpecies();
                    String parts[] = line.split("\t");
                    int taxonomy = Integer.parseInt(parts[0]);
                    if (species.equals("human")) {
	                    if (taxonomy!=9606) {
	                        // only import human genes
	                        continue;
	                    }
	                } else if (species.equals("mouse")) {
	                    if (taxonomy!=10090) {
	                        // only import mouse genes
	                        continue;
	                    }
	                } else {
	                	throw new Error("Species "+species+" is not supported.");
	                }
                    
                    int entrezGeneId = Integer.parseInt(parts[1]);
                    String geneSymbol = parts[2];
                    String locusTag = parts[3];
                    String strAliases = parts[4];
                    String strXrefs = parts[5];
                    String cytoband = parts[7];
                    String desc = parts[8];
                    String type = parts[9];
                    String mainSymbol = parts[10]; // use 10 instead of 2 since column 2 may have duplication
                    Set<String> aliases = new HashSet<String>();

                    if (!locusTag.equals("-")) {
                        aliases.add(locusTag);
                    }
                    if (!strAliases.equals("-")) {
                        aliases.addAll(Arrays.asList(strAliases.split("\\|")));
                    }
                    
                    if (geneSymbol.startsWith("MIR") && type.equalsIgnoreCase("miscRNA")) {
                        line = buf.readLine();
                        continue; // ignore miRNA; process separately
                    }
                    
                    CanonicalGene gene = null;
                    if (!mainSymbol.equals("-")) {
                        gene = new CanonicalGene(entrezGeneId, mainSymbol, aliases);
                        Set<CanonicalGene> genes = genesWithSymbolFromNomenClatureAuthority.get(mainSymbol);
                        if (genes==null) {
                            genes = new HashSet<CanonicalGene>();
                            genesWithSymbolFromNomenClatureAuthority.put(mainSymbol, genes);
                        }
                        genes.add(gene);
                    } else if (!geneSymbol.equals("-")) {
                        gene = new CanonicalGene(entrezGeneId, geneSymbol, aliases);
                        Set<CanonicalGene> genes = genesWithoutSymbolFromNomenClatureAuthority.get(geneSymbol);
                        if (genes==null) {
                            genes = new HashSet<CanonicalGene>();
                            genesWithoutSymbolFromNomenClatureAuthority.put(geneSymbol, genes);
                        }
                        genes.add(gene);
                    }
                    
                    if (gene!=null) {
                        if (!cytoband.equals("-")) {
                        	if (species.equals("mouse")) {
                        		//Usually three cytobands are represented in the gene info for mouse:
                        		//First, only the chromosome number, then the chromosome number and the position
                        		//of the gene in cM, and finally, by the "Correct" cytoband, that is the name of 
                        		//the chromosome and the cytoband, which is a letter (from A to H) followed by a
                        		//numeric position, for example X A3 or 3 A1.2.
                            	List<String> cytobands = new ArrayList<String>();
                            	cytobands.addAll(Arrays.asList(cytoband.split("\\|")));
                            	for (String i : cytobands) {
                            		if (!i.contains("cM")) { //Skip cytobands containing cM
                            			if (cytobands.size() <=2) {
                            				cytoband = i; //Only one cytoband is left from these gene.
                            			} else { 
                            				//We have more than one cytoband for these gene, so keep the one which
                            				//has the cytoband.
                            				if (i.contains("A") || i.contains("B") || i.contains("C") || i.contains("D") || i.contains("E") || i.contains("F") || i.contains("G") || i.contains("H")) {
                            					cytoband = i;
                            				}
                            			}
                            		}
                            	}
                        	}
                        	gene.setCytoband(cytoband); //For human there is no need to parse the cytoband
                        }
                        gene.setType(type);
                    }
            }
        }
        
        MySQLbulkLoader.bulkLoadOn();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        // Add genes with symbol from nomenclature authority
        for (Map.Entry<String, Set<CanonicalGene>> entry : genesWithSymbolFromNomenClatureAuthority.entrySet()) {
            Set<CanonicalGene> genes = entry.getValue();
            if (genes.size()==1) {
                daoGene.addGene(genes.iterator().next());
            } else {
            	//TODO - is unexpected for official symbols...raise Exception instead?
                logDuplicateGeneSymbolWarning(entry.getKey(), genes);
            }
        }

        // Add genes without symbol from nomenclature authority
        if (genesWithoutSymbolFromNomenClatureAuthority.keySet().size() > 0) {
        	int nrImported = 0;
        	int nrSkipped = 0;
	        for (Map.Entry<String, Set<CanonicalGene>> entry : genesWithoutSymbolFromNomenClatureAuthority.entrySet()) {
	            Set<CanonicalGene> genes = entry.getValue();
	            String symbol = entry.getKey();
	            if (genes.size()==1) {
	                CanonicalGene gene = genes.iterator().next();
	                if (!genesWithSymbolFromNomenClatureAuthority.containsKey(symbol)) {
	                    daoGene.addGene(gene);
	                    nrImported++;
	                } else {
	                    // ignore entries with a symbol that have the same value as stardard one
	                    ProgressMonitor.logWarning("Ignored line with entrez gene id "+gene.getEntrezGeneId()
	                            + " because symbol "+symbol+" is already an 'official symbol' of another gene");
	                    nrSkipped++;
	                }
	            } else {
	                logDuplicateGeneSymbolWarning(symbol, genes);
	                nrSkipped =+ genes.size();
	            }
	        }
	        ProgressMonitor.logWarning("There were " +genesWithoutSymbolFromNomenClatureAuthority.keySet().size() + 
	        		" genes names in this file without an official symbol from nomenclature authorty. Imported: " + nrImported + 
	        		", skipped (because of duplicate symbol entry or because symbol is an 'official symbol' of another gene): " + nrSkipped);
        }
        
        if (MySQLbulkLoader.isBulkLoad()) {
           MySQLbulkLoader.flushAll();
        }        
    }
    
    private static void logDuplicateGeneSymbolWarning(String symbol, Set<CanonicalGene> genes) {
        StringBuilder sb = new StringBuilder();
        sb.append("More than 1 gene has the same symbol ")
                .append(symbol)
                .append(":");
        for (CanonicalGene gene : genes) {
            sb.append(" ")
                    .append(gene.getEntrezGeneId())
                    .append(". Ignore...");
        }
        ProgressMonitor.logWarning(sb.toString());
    }

    private static void importGeneLength(File geneFile) throws IOException, DaoException {
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        FileReader reader = new FileReader(geneFile);
        BufferedReader buf = new BufferedReader(reader);
        String line;
        CanonicalGene currentGene = null;
        List<long[]> loci = new ArrayList<long[]>();
        while ((line=buf.readLine()) != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
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
    
    static void importSuppGeneData(File suppGeneFile) throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(suppGeneFile);
        BufferedReader buf = new BufferedReader(reader);
        String line;
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        while ((line = buf.readLine()) != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
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
        }
        reader.close(); 
    }

    public static void main(String[] args) throws Exception {
		SpringUtil.initDataSource();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.deleteAllRecords();
        if (args.length == 0) {
            System.out.println("command line usage:  importGenes.pl <ncbi_genes.txt> <supp-genes.txt> <microrna.txt> <all_exon_loci.bed>");
            return;
        }
        ProgressMonitor.setConsoleMode(true);
        
        File geneFile = new File(args[0]);
        System.out.println("Reading gene data from:  " + geneFile.getAbsolutePath());
        int numLines = FileUtil.getNumLines(geneFile);
        System.out.println(" --> total number of lines:  " + numLines);
        ProgressMonitor.setMaxValue(numLines);
        ImportGeneData.importData(geneFile);
        ConsoleUtil.showWarnings();
        System.err.println("Done. Restart tomcat to make sure the cache is replaced with the new data.");
        
        if (args.length>=2) {
            File suppGeneFile = new File(args[1]);
            System.out.println("Reading supp. gene data from:  " + suppGeneFile.getAbsolutePath());
            numLines = FileUtil.getNumLines(suppGeneFile);
            System.out.println(" --> total number of lines:  " + numLines);
            ProgressMonitor.setMaxValue(numLines);
            ImportGeneData.importSuppGeneData(suppGeneFile);
        }
        
        if (args.length>=3) {
            File miRNAFile = new File(args[2]);
            System.out.println("Reading miRNA data from:  " + miRNAFile.getAbsolutePath());
            numLines = FileUtil.getNumLines(miRNAFile);
            System.out.println(" --> total number of lines:  " + numLines);
            ProgressMonitor.setMaxValue(numLines);
            ImportMicroRNAIDs.importData(miRNAFile);
    }
        
        if (args.length>=4) {
            File lociFile = new File(args[3]);
            System.out.println("Reading loci data from:  " + lociFile.getAbsolutePath());
            numLines = FileUtil.getNumLines(lociFile);
            System.out.println(" --> total number of lines:  " + numLines);
            ProgressMonitor.setMaxValue(numLines);
            ImportGeneData.importGeneLength(lociFile);
        }
    }
}
