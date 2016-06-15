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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Command Line Tool to Import Background Gene Data.
 */
public class ImportGeneData extends ConsoleRunnable {

    /**
     * 
     * @param geneFile
     * @throws IOException
     * @throws DaoException
     */
    public static void importData(File geneFile) throws IOException, DaoException {
        Map<String, Set<CanonicalGene>> genesWithSymbolFromNomenClatureAuthority = new LinkedHashMap<>();
        Map<String, Set<CanonicalGene>> genesWithoutSymbolFromNomenClatureAuthority = new LinkedHashMap<>();
        try (FileReader reader = new FileReader(geneFile)) {
            prepareGeneTablesForUpdate();
            BufferedReader buf = new BufferedReader(reader);
            String line;
            while ((line = buf.readLine()) != null) {
                ProgressMonitor.incrementCurValue();
                ConsoleUtil.showProgress();
                if (line.startsWith("#")) {
                    continue;
                }
                
                    String parts[] = line.split("\t");
                    int taxonimy = Integer.parseInt(parts[0]);
                    if (taxonimy!=9606) {
                        // only import human genes
                        continue;
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
                        continue; // ignore miRNA; process seperately
                    }
                    
                    CanonicalGene gene = null;
                    if (!mainSymbol.equals("-")) {
                        //try the main symbol:
                        gene = new CanonicalGene(entrezGeneId, mainSymbol, aliases);
                        Set<CanonicalGene> genes = genesWithSymbolFromNomenClatureAuthority.get(mainSymbol);
                        if (genes==null) {
                            genes = new HashSet<CanonicalGene>();
                            genesWithSymbolFromNomenClatureAuthority.put(mainSymbol, genes);
                        }
                        genes.add(gene);
                    } else if (!geneSymbol.equals("-")) {
                        //there is no main symbol, so import using the temporary/unnoficial(?) symbol:
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
                            gene.setCytoband(cytoband);
                        }
                        gene.setType(type);
                    }
            }
        }
        
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        // Add genes with symbol from nomenclature authority
        for (Map.Entry<String, Set<CanonicalGene>> entry : genesWithSymbolFromNomenClatureAuthority.entrySet()) {
            Set<CanonicalGene> genes = entry.getValue();
            if (genes.size()==1) {
                CanonicalGene gene = genes.iterator().next();
                //if exists
                CanonicalGene dbGene = daoGene.getGene(gene.getEntrezGeneId());
                if (dbGene != null) {
                    //if symbol has changed, record event:
                    if (!dbGene.getHugoGeneSymbolAllCaps().equals("~" + gene.getHugoGeneSymbolAllCaps())) {
                        ProgressMonitor.logWarning("Gene symbol change for EntrezId=" + dbGene.getEntrezGeneId() + ": symbol changed from " + 
                                    dbGene.getHugoGeneSymbolAllCaps().substring(1) + " to " + gene.getHugoGeneSymbolAllCaps());
                        //update gene:
                        daoGene.updateGene(gene);
                    }
                }
                else {
                    daoGene.addGene(gene);
                    ProgressMonitor.logWarning("New gene added");
                }
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
	                    //if exists
	                    CanonicalGene dbGene = daoGene.getGene(gene.getEntrezGeneId());
	                    if (dbGene != null) {
    	                    //if symbol has changed, record event:
    	                    if (!dbGene.getHugoGeneSymbolAllCaps().equals("~" + gene.getHugoGeneSymbolAllCaps())) {
    	                        ProgressMonitor.logWarning("Gene symbol change for EntrezId=" + dbGene.getEntrezGeneId() + ": symbol changed from " + 
    	                                dbGene.getHugoGeneSymbolAllCaps().substring(1) + " to " + gene.getHugoGeneSymbolAllCaps());
    	                        //update gene:
    	                        daoGene.updateGene(gene);
    	                    }
	                    }
	                    else {
	                        daoGene.addGene(gene);
	                        ProgressMonitor.logWarning("New gene with no official symbol added");
	                    }
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
        
        //report deprecated genes (i.e. genes where HUGO_GENE_SYMBOL remain with ~ at the start after the update process above):
        List<CanonicalGene> deprecatedGenes = daoGene.getDeprecatedGenes();
        for (CanonicalGene deprecatedGene: deprecatedGenes) {
            ProgressMonitor.logWarning("Deprecated gene (gene found in DB but not in the given file anymore): " + 
                            deprecatedGene.getHugoGeneSymbolAllCaps() + " (Entrez=" + deprecatedGene.getEntrezGeneId() + ")");  
        }
    }
    
    
    /**
     * This method will prepare the gene and gene_alias tables for being updated. The gene_alias table 
     * will just reflect the new data being imported, so we start by making it empty. The gene table
     * is a bit more complicated since many other tables refer to it. Here we keep track of the previous symbol
     * for reporting purposes at the end. See TODO - add method ref. 
     * 
     * 
     * @return
     * @throws DaoException
     */
    private static int prepareGeneTablesForUpdate() throws DaoException {
        
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            int rows = 0;

            con = JdbcUtil.getDbConnection(ImportGeneData.class);
            pstmt = con.prepareStatement
                    ("SET SQL_SAFE_UPDATES = 0;" +
                     "   UPDATE gene SET HUGO_GENE_SYMBOL = SUBSTRING_INDEX(HUGO_GENE_SYMBOL, '~', -1);" +
                     "   UPDATE gene SET PREVIOUS_SYMBOL = HUGO_GENE_SYMBOL, HUGO_GENE_SYMBOL=CONCAT('~', HUGO_GENE_SYMBOL); ");
           
            rows = pstmt.executeUpdate();

            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(ImportGeneData.class, con, pstmt, rs);
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
	 		parser.accepts( "exon-loci", "exon loci file for calculating and storing gene lengths" ).withRequiredArg().describedAs( "all_exon_loci.bed" ).ofType( String.class );
	
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
			if(options.has("genes")) {
				geneFile = new File((String) options.valueOf("genes"));
			}
			else {
				throw new UsageException(progName, description, parser,
				        "Error: 'genes' argument required.");
			}
	        System.out.println("Reading gene data from:  " + geneFile.getAbsolutePath());
	        int numLines = FileUtil.getNumLines(geneFile);
	        System.out.println(" --> total number of lines:  " + numLines);
	        ProgressMonitor.setMaxValue(numLines);
	        MySQLbulkLoader.bulkLoadOn();
	        ImportGeneData.importData(geneFile);
	        ConsoleUtil.showWarnings();
	        
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
	        
	        if(options.has("exon-loci")) {
	            File lociFile = new File((String) options.valueOf("exon-loci"));
	            System.out.println("Reading loci data from:  " + lociFile.getAbsolutePath());
	            numLines = FileUtil.getNumLines(lociFile);
	            System.out.println(" --> total number of lines:  " + numLines);
	            ProgressMonitor.setMaxValue(numLines);
	            ImportGeneData.importGeneLength(lociFile);
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
