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

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.*;
import java.util.*;

/**
 * Command Line Tool to Import Background Gene Data.
 */
public class ImportGeneData extends ConsoleRunnable {

    /**
     * Adds the genes parsed from the file into the Database.
     * 
     * @param geneFile File with gene information
     * @throws IOException
     * @throws DaoException
     */
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
                        //Try the main symbol:
                        gene = new CanonicalGene(entrezGeneId, mainSymbol, aliases);
                        Set<CanonicalGene> genes = genesWithSymbolFromNomenClatureAuthority.get(mainSymbol);
                        if (genes==null) {
                            genes = new HashSet<CanonicalGene>();
                            genesWithSymbolFromNomenClatureAuthority.put(mainSymbol, genes);
                        }
                        genes.add(gene);
                    } else if (!geneSymbol.equals("-")) {
                        //there is no main symbol, so import using the temporary/unofficial(?) symbol:
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
            addGenesToDB(genesWithSymbolFromNomenClatureAuthority, genesWithoutSymbolFromNomenClatureAuthority);
        }
    }
        
        /**
         * Iterate over the genes found in the given maps and try to add them to the DB.
         * 
         * @param genesWithSymbolFromNomenClatureAuthority: genes with official symbol
         * @param genesWithoutSymbolFromNomenClatureAuthority: genes without official symbol (can happen, some entrez genes
         * have no official symbol yet, but a temporary symbol).
         * 
         * @throws DaoException
         */
        private static void addGenesToDB(Map<String, Set<CanonicalGene>> genesWithSymbolFromNomenClatureAuthority,
                  Map<String, Set<CanonicalGene>> genesWithoutSymbolFromNomenClatureAuthority) throws DaoException {
                     
          DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
                  int nrExisting = 0;
        // Add genes with symbol from nomenclature authority
        for (Map.Entry<String, Set<CanonicalGene>> entry : genesWithSymbolFromNomenClatureAuthority.entrySet()) {
            Set<CanonicalGene> genes = entry.getValue();
            if (genes.size()==1) {
                CanonicalGene gene = genes.iterator().next();
                // first check if gene exists. If exists, give warning and skip record:
                if (daoGene.getGene(gene.getEntrezGeneId()) != null) {
                    ProgressMonitor.logWarning("Gene is already in table. Updates are not allowed. If you need to update the `gene` table, please remove all studies first and empty the `gene` table.");
                    nrExisting++;
                    continue;
                }
                daoGene.addGene(gene);
                ProgressMonitor.logWarning("New gene with official symbol added");
                } else {
                //TODO - is unexpected for official symbols...raise Exception instead?
                    logDuplicateGeneSymbolWarning(entry.getKey(), genes, true);
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
                        //only add the gene if it does not conflict with an official symbol:
                        if (!genesWithSymbolFromNomenClatureAuthority.containsKey(symbol)) {
                            //first check if gene exists. If exists, give warning and skip record since we don't allow updates in this process:
                            if (daoGene.getGene(gene.getEntrezGeneId()) != null) {
                                ProgressMonitor.logWarning("Gene is already in table. Updates are not allowed. If you need to update the `gene` table, please remove all studies first and empty the `gene` table.");
                                nrSkipped++;
                                nrExisting++;
                                continue;
                            }
                            daoGene.addGene(gene);
                            ProgressMonitor.logWarning("New gene with *no* official symbol added");
                            nrImported++;
                        } else {
                            // ignore entries with a symbol that have the same value as stardard one
                            ProgressMonitor.logWarning("Ignored line with entrez gene id "+gene.getEntrezGeneId() + " because its 'unnoficial' symbol " +
                            symbol+" is already an 'official symbol' of another gene");
                            nrSkipped++;
                            continue;
                        }
                    } else {
                        logDuplicateGeneSymbolWarning(symbol, genes, false);
                        nrSkipped++;
                        continue;
                    }
                }
                ProgressMonitor.logWarning("There were " +genesWithoutSymbolFromNomenClatureAuthority.keySet().size() + 
                                " genes names in this file without an official symbol from nomenclature authority. Imported: " + nrImported + 
                                ". Gene names skipped (because of duplicate symbol entry or because symbol is an 'official symbol' of another gene): " + nrSkipped);
        }
        
        if (nrExisting > 0) {
            ProgressMonitor.logWarning("Number of records skipped because the gene was already in the gene table (updates are not allowed - see specific warnings above): " + nrExisting);
        }
    }
    
    private static void logDuplicateGeneSymbolWarning(String symbol, Set<CanonicalGene> genes, boolean isOfficialSymbol) {
        StringBuilder sb = new StringBuilder();
        if (isOfficialSymbol) {
            sb.append("More than 1 gene has the same (official) symbol ");
            } else {
                sb.append("More than 1 gene has the same (unofficial) symbol ");
            }
            sb.append(symbol)
                .append(":");
        for (CanonicalGene gene : genes) {
            sb.append(" ")
                    .append(gene.getEntrezGeneId())
                    .append(". Ignore...");
        }
        ProgressMonitor.logWarning(sb.toString());
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
                daoGene.addGene(gene);
            }
        }
        reader.close(); 
    }

        @Override
    public void run() {
                try {
                        SpringUtil.initDataSource();
        
                String description = "Update gene / gene alias tables ";
                
                // using a real options parser, helps avoid bugs
                        OptionParser parser = new OptionParser();
                        OptionSpec<Void> help = parser.accepts( "help", "print this help info" );
                        parser.accepts( "genes", "ncbi genes file" ).withRequiredArg().describedAs( "ncbi_genes.txt" ).ofType( String.class );
                        parser.accepts( "supp-genes", "alternative genes file" ).withRequiredArg().describedAs( "supp-genes.txt" ).ofType( String.class );
                        parser.accepts( "microrna", "microrna file" ).withRequiredArg().describedAs( "microrna.txt" ).ofType( String.class );
        
                        String progName = "importGenes";
                        OptionSet options = null;
                        try {
                                options = parser.parse( args );
                        } catch (OptionException e) {
                                throw new UsageException(progName, description, parser,
                                        e.getMessage());
                        }
                          
                        if( options.has( help ) ){
                                throw new UsageException(progName, description, parser);
                        }
                        
                ProgressMonitor.setConsoleMode(true);
                
                File geneFile;
                int numLines;
                        if(options.has("genes")) {
                                geneFile = new File((String) options.valueOf("genes"));
                        
                        System.out.println("Reading gene data from:  " + geneFile.getAbsolutePath());
                        numLines = FileUtil.getNumLines(geneFile);
                        System.out.println(" --> total number of lines:  " + numLines);
                        ProgressMonitor.setMaxValue(numLines);
                        MySQLbulkLoader.bulkLoadOn();
                        ImportGeneData.importData(geneFile);
                        MySQLbulkLoader.flushAll(); //Gene and gene_alias should be updated before calculating gene length (gtf)!
                        }
                
                if(options.has("supp-genes")) {
                    File suppGeneFile = new File((String) options.valueOf("genes"));
                    System.out.println("Reading supp. gene data from:  " + suppGeneFile.getAbsolutePath());
                    numLines = FileUtil.getNumLines(suppGeneFile);
                    System.out.println(" --> total number of lines:  " + numLines);
                    ProgressMonitor.setMaxValue(numLines);
                    ImportGeneData.importSuppGeneData(suppGeneFile);
                }
                
                if(options.has("microrna")) {
                    File miRNAFile = new File((String) options.valueOf("microrna"));
                    System.out.println("Reading miRNA data from:  " + miRNAFile.getAbsolutePath());
                    numLines = FileUtil.getNumLines(miRNAFile);
                    System.out.println(" --> total number of lines:  " + numLines);
                    ProgressMonitor.setMaxValue(numLines);
                    ImportMicroRNAIDs.importData(miRNAFile);
                }
                MySQLbulkLoader.flushAll();
            System.err.println("Done. Restart tomcat to make sure the cache is replaced with the new data.");

                }
                catch (RuntimeException e) {
                        throw e;
                }
                catch (Exception e) {
                   throw new RuntimeException(e);
                }
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public ImportGeneData(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportGeneData(args);
        runner.runInConsole();
    }

}
