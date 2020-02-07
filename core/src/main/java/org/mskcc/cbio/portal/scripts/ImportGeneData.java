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
import org.mskcc.cbio.portal.model.ReferenceGenome;
import org.mskcc.cbio.portal.model.ReferenceGenomeGene;
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
    public static void importData(File geneFile, String genomeBuild) throws IOException, DaoException {
        Map<String, Set<CanonicalGene>> genesWithSymbolFromNomenClatureAuthority = new LinkedHashMap<>();
        Map<String, Set<CanonicalGene>> genesWithoutSymbolFromNomenClatureAuthority = new LinkedHashMap<>();
        Map<String, Set<ReferenceGenomeGene>> refGenesWithSymbolFromNomenClatureAuthority = new LinkedHashMap<>();
        Map<String, Set<ReferenceGenomeGene>> refGenesWithoutSymbolFromNomenClatureAuthority = new LinkedHashMap<>();
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
                    String chr = cytoband.split("p|q|;|c|\\|| ")[0]; // various strange characters were found in this column
                    int referenceGenomeId = DaoReferenceGenome.getReferenceGenomeByBuildName(genomeBuild).getReferenceGenomeId();
                    String desc = parts[8];
                    String type = parts[9];
                    String mainSymbol = parts[10]; // use 10 instead of 2 since column 2 may have duplication
                    Set<String> aliases = new HashSet<String>();

                    // try to get chr from other column if needed
                    if (chr.equals("-")) {
                        if (!parts[6].equals("-")) {
                            chr = parts[6];
                        } else { 
                            continue; // skip if both columns are absent
                        }
                    }
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
                    ReferenceGenomeGene refGene = null;
                    if (!mainSymbol.equals("-")) {
                        //Try the main symbol:
                        gene = new CanonicalGene(entrezGeneId, mainSymbol, aliases);
                        refGene = new ReferenceGenomeGene(entrezGeneId, referenceGenomeId, chr, cytoband, 0, 0, 0);
                        Set<CanonicalGene> genes = genesWithSymbolFromNomenClatureAuthority.get(mainSymbol);
                        Set<ReferenceGenomeGene> refGenes = refGenesWithSymbolFromNomenClatureAuthority.get(mainSymbol);
                        
                        if (genes==null) {
                            genes = new HashSet<CanonicalGene>();
                            genesWithSymbolFromNomenClatureAuthority.put(mainSymbol, genes);
                        }
                        if (refGenes==null) {
                            refGenes = new HashSet<ReferenceGenomeGene>();
                            refGenesWithSymbolFromNomenClatureAuthority.put(mainSymbol, refGenes);
                        }
                        genes.add(gene);
                        refGenes.add(refGene);
                    } else if (!geneSymbol.equals("-")) {
                        //there is no main symbol, so import using the temporary/unofficial(?) symbol:
                        gene = new CanonicalGene(entrezGeneId, geneSymbol, aliases);
                        refGene = new ReferenceGenomeGene(entrezGeneId, referenceGenomeId, chr, 
                            cytoband, 0, 0, 0);
                        Set<CanonicalGene> genes = genesWithoutSymbolFromNomenClatureAuthority.get(geneSymbol);
                        if (genes==null) {
                            genes = new HashSet<CanonicalGene>();
                            genesWithoutSymbolFromNomenClatureAuthority.put(geneSymbol, genes);
                        }
                        genes.add(gene);
                        Set<ReferenceGenomeGene> refGenes = refGenesWithoutSymbolFromNomenClatureAuthority.get(geneSymbol);
                        if (refGenes==null) {
                            refGenes = new HashSet<ReferenceGenomeGene>();
                            refGenesWithoutSymbolFromNomenClatureAuthority.put(geneSymbol, refGenes);
                        }
                        refGenes.add(refGene);
                    }
                    
                    if (gene!=null) {
                        gene.setType(type);
                    }
            }
            addGenesToDB(genesWithSymbolFromNomenClatureAuthority, genesWithoutSymbolFromNomenClatureAuthority);
            addReferenceGenomeGenesToDB(refGenesWithSymbolFromNomenClatureAuthority, 
                refGenesWithoutSymbolFromNomenClatureAuthority);
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
    /**
     * Iterate over the reference genome genes found in the given maps and try to add them to the DB.
     *
     * @param refGenesWithSymbolFromNomenClatureAuthority: reference genome genes with official symbol
     * @param refGenesWithoutSymbolFromNomenClatureAuthority: reference genome genes without official symbol (can happen, some entrez genes
     * have no official symbol yet, but a temporary symbol).
     *
     * @throws DaoException
     */
    private static void addReferenceGenomeGenesToDB(Map<String, Set<ReferenceGenomeGene>> refGenesWithSymbolFromNomenClatureAuthority,
                                     Map<String, Set<ReferenceGenomeGene>> refGenesWithoutSymbolFromNomenClatureAuthority) throws DaoException {

        DaoReferenceGenomeGene daoReferenceGenomeGene = DaoReferenceGenomeGene.getInstance();

        int nrExisting = 0;
        // Add genes with symbol from nomenclature authority
        for (Map.Entry<String, Set<ReferenceGenomeGene>> entry : refGenesWithSymbolFromNomenClatureAuthority.entrySet()) {
            Set<ReferenceGenomeGene> refGenes = entry.getValue();
            if (refGenes.size()==1) {
                ReferenceGenomeGene refGene = refGenes.iterator().next();
                // first check if gene exists. If exists, give warning and skip record:
                if (daoReferenceGenomeGene.getGene(refGene.getEntrezGeneId(), refGene.getReferenceGenomeId()) != null) {
                    ProgressMonitor.logWarning("Reference Genome Gene is already in table. Updates are not allowed. If you need to update the `gene` table, please remove all studies first and empty the `gene` table.");
                    nrExisting++;
                    continue;
                }
                try {
                    daoReferenceGenomeGene.addOrUpdateGene(refGene);
                } catch (Exception e) {
                    ProgressMonitor.logWarning("chromosome too large" + refGene.getChr());
                    continue;
                }
                ProgressMonitor.logWarning("New reference genome gene with official symbol added");
            } else {
                //TODO - is unexpected for official symbols...raise Exception instead?
                logDuplicateReferenceGenomeGeneSymbolWarning(entry.getKey(), refGenes, true);
            }
        }

        // Add genes without symbol from nomenclature authority
        if (refGenesWithoutSymbolFromNomenClatureAuthority.keySet().size() > 0) {
            int nrImported = 0;
            int nrSkipped = 0;
            for (Map.Entry<String, Set<ReferenceGenomeGene>> entry : refGenesWithoutSymbolFromNomenClatureAuthority.entrySet()) {
                Set<ReferenceGenomeGene> refGenes = entry.getValue();
                String symbol = entry.getKey();
                if (refGenes.size()==1) {
                    ReferenceGenomeGene refGene = refGenes.iterator().next();
                    //only add the gene if it does not conflict with an official symbol:
                    if (!refGenesWithSymbolFromNomenClatureAuthority.containsKey(symbol)) {
                        //first check if gene exists. If exists, give warning and skip record since we don't allow updates in this process:
                        if (daoReferenceGenomeGene.getGene(refGene.getEntrezGeneId(), refGene.getReferenceGenomeId()) != null) {
                            ProgressMonitor.logWarning("Gene is already in table. Updates are not allowed. If you need to update the `gene` table, please remove all studies first and empty the `gene` table.");
                            nrSkipped++;
                            nrExisting++;
                            continue;
                        }
                        daoReferenceGenomeGene.addOrUpdateGene(refGene);
                        ProgressMonitor.logWarning("New gene with *no* official symbol added");
                        nrImported++;
                    } else {
                        // ignore entries with a symbol that have the same value as stardard one
                        ProgressMonitor.logWarning("Ignored line with entrez gene id "+refGene.getEntrezGeneId() + " because its 'unnoficial' symbol " +
                            symbol+" is already an 'official symbol' of another gene");
                        nrSkipped++;
                        continue;
                    }
                } else {
                    logDuplicateReferenceGenomeGeneSymbolWarning(entry.getKey(), refGenes, true);
                    nrSkipped++;
                    continue;
                }
            }
            ProgressMonitor.logWarning("There were " +refGenesWithoutSymbolFromNomenClatureAuthority.keySet().size() +
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

    private static void logDuplicateReferenceGenomeGeneSymbolWarning(String symbol, Set<ReferenceGenomeGene> refGenes, boolean isOfficialSymbol) {
        StringBuilder sb = new StringBuilder();
        if (isOfficialSymbol) {
            sb.append("More than 1 gene has the same (official) symbol ");
        } else {
            sb.append("More than 1 gene has the same (unofficial) symbol ");
        }
        sb.append(symbol)
            .append(":");
        for (ReferenceGenomeGene refGene : refGenes) {
            sb.append(" ")
                .append(refGene.getEntrezGeneId())
                .append(". Ignore...");
        }
        ProgressMonitor.logWarning(sb.toString());
    }
    
    private static String getCytoband(String cytoband, String species) {
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
                        if (cytobands.size() <= 2) {
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
            return cytoband;
        }
        return null;
    }

    /**
     * This method imports the gene lengths of a General Transfer Format (gtf) file. This file contains different genetic features from genes (CDS, exons, introns...) in each line.
         * All features of a single gene contain the same Ensembl ID. Therefore, this method uses the Ensembl IDs to differentiate between different genes. All the features with the same 
     * Ensembl ID are in consecutive lines. This method uses the gene symbol to retrieve the Entrez ID, but different Ensembl IDs can share the same symbol. If these Ensembl IDs are
     * located in different chromosomes, the method uses the length of the Ensembl ID according to the cytoband from the gene saved in the database. In the case multiple Ensembl IDs
     * with the same symbol are on the same chromosome or no cytoband information is available, the length of the last Ensembl ID is taken.
     * 
     * @param geneFile
     * @throws IOException
     * @throws DaoException
     */
    
    public static void importGeneLength(File geneFile, String genomeBuild, String species, boolean hasGenes) throws IOException, DaoException {
        //Set the variables needed for the method
        FileReader reader = new FileReader(geneFile);
        BufferedReader buf = new BufferedReader(reader);
        int referenceGenomeId = DaoReferenceGenome.getReferenceGenomeByBuildName(genomeBuild).getReferenceGenomeId();
        String line;
        ProgressMonitor.setCurrentMessage("\nUpdating gene lengths... \n"); //Display a message in the console
        boolean geneUpdated = false;
        
        String previousEnsembl = "";
        String currentEnsembl = "";
        String previousSymbol = "";
        String currentSymbol = "";
        String previousChrom = "";
        String currentChrom = "";
        Long currentStart;
        Long currentStop;
        
        String parts[] = null;
        List<long[]> loci = new ArrayList<long[]>();
        int nrGenesUpdated = 0;
        
        //Iterate over the file and fill the hash map with the max and min values of each gene (start and end position)
        while ((line=buf.readLine()) != null) {
                if(line.charAt(0) == '#'){
                        continue;
                }
                parts = line.split("\t");
                currentChrom = parts[0];
                currentStart = Long.parseLong(parts[3]);
                currentStop = Long.parseLong(parts[4]) + 1; // We have to add 1 here, because the last base is also included.
                
                if (parts[2].contains("exon") || parts[2].contains("CDS")) {
                        String info[] = parts[8].split(";");

                        //Retrieve the ensembl ID
                        for (String i : info) {
                                if (i.contains("gene_id")) {
                                        String j[] = i.split(" ");
                                        currentEnsembl = j[1].replaceAll("\"", "");
                                }
                                else if (i.contains("gene_name")) {
                                        String j[] = i.split(" ");
                                        currentSymbol = j[2].replaceAll("\"", ""); 
                                }
                        }

                        /// Only in case of the first line
                if (previousEnsembl.equals("")) { 
                        previousEnsembl = currentEnsembl;
                        previousSymbol = currentSymbol;
                        previousChrom = currentChrom;
                        loci.add(new long[]{currentStart, currentStop}); //Add the new positions        
                }                
                /// For all other lines
                else {

                    /// If there is no switch from Ensembl ID 
                    if (previousEnsembl.equals(currentEnsembl)) { 
                        
                        loci.add(new long[]{currentStart, currentStop}); //Add the new positions
                    }   
                    /// If there is a switch
                    else {
                        geneUpdated = updateLength(previousSymbol, previousChrom, loci,
                            referenceGenomeId);
                        if (geneUpdated) {
                            nrGenesUpdated++;
                        }
                        /// At the end of writing a new gene, clear the loci and save the new ensemblID.
                        loci.clear();

                        previousEnsembl = currentEnsembl;
                        previousSymbol = currentSymbol;
                        previousChrom = currentChrom;
                        loci.add(new long[]{currentStart, currentStop}); //Add the new positions
                    }
                }
            }
        }
        /// Write the last gene
        /// First check if the gene exists in the database
        geneUpdated = updateLength(previousSymbol, previousChrom, loci, referenceGenomeId);
        if (geneUpdated) {
            nrGenesUpdated++;
        }
        ProgressMonitor.setCurrentMessage("Updated length info for " + nrGenesUpdated + " genes\n");
        
        buf.close();
    }
    
    /**
     * This method receives a symbol, a chromosome and a list of loci (should be from the same gene), and with that it retrieves the database gene and it calculates the length
     * of all its exons contained in loci. If the symbol is non-ambiguous, or the chromosome reported does not match the cytoband of the database gene, then length is not updated.
     * The method reports a boolean stating if the gene length has been updated or not.
     * 
     * @param symbol
     * @param chromosome
     * @param loci
     * @return
     * @throws IOException
     * @throws DaoException
     */
    public static boolean updateLength(String symbol, String chromosome, List<long[]> loci, int refreneceGenomeId) throws IOException, DaoException {
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        CanonicalGene gene = daoGeneOptimized.getNonAmbiguousGene(symbol);
        
        boolean lengthUpdated = false;

        if (gene == null) {
            ProgressMonitor.logWarning("Unable to retrieve gene by symbol: " +symbol);
            return lengthUpdated;
        }

        System.out.println(" --> update reference genome gene:  " + gene.getHugoGeneSymbolAllCaps());
        DaoReferenceGenomeGene daoReferenceGenomeGene = DaoReferenceGenomeGene.getInstance();

        /// Check if the gene is in the database
        ReferenceGenomeGene refGene = daoReferenceGenomeGene.getGene(gene.getEntrezGeneId(), refreneceGenomeId);
        /// If it's not in the database, don't add it
        if (refGene != null) {
                /// Calc length
            long[] exonic = calculateGeneLength(loci);
                
            /// If there is a cytoband in database, check if cytoband-chr matches input-chr
            String cbChr = "chr"+refGene.getCytoband();
            System.out.println(" updateLength--> cytoband of the gene:  " + cbChr);
            if (cbChr.equals(chromosome)) { //Update the length only if the chromosome matches
                // update reference genome gene
                System.out.println(" updateLength--> update reference genome gene:  " + gene.getHugoGeneSymbolAllCaps());
                refGene.setExonicLength((int) exonic[2]);
                refGene.setStart(exonic[0]);
                refGene.setEnd(exonic[1]);
                daoReferenceGenomeGene.updateGene(refGene);
                System.out.println(" updateLength--> exonic length of the gene:  " + 
                        daoReferenceGenomeGene.getGene(gene.getEntrezGeneId(), refreneceGenomeId).getExonicLength());
                lengthUpdated = true;
            }
            else {
                ProgressMonitor.logWarning("Cytoband does not match, gene not saved (likely another version of gene in gtf has correct chr and is saved)");
            }
        }
        return lengthUpdated;
    }

    /**
     * This method uses a list of exon loci from the same gene and it adds the length of all of them to get the gene length. If some of the exons are
     * overlapping, the overlapping part is only counted once in the calculation. For example, if an exon goes from position 3 to 10 and another one from 
     * position 5 to 11, when calculating the length these exons would be considered as a single exon going from position 3 to 11.
     * 
     * @param loci
     * @return
     */
    public static long[] calculateGeneLength(List<long[]> loci) {
        long min = Long.MAX_VALUE, max=-1;
        for (long[] l : loci) {
            if (l[0]<min) {
                min = l[0];
            }
            if (l[1]>max) {
                max = l[1];
            }
        }
        if (max < min) {
                throw new IllegalArgumentException("Found error: max=" + max + ", min=" + min);
        }
        BitSet bitSet = new BitSet((int)(max-min));
        for (long[] l : loci) {
            bitSet.set((int)(l[0]-min), ((int)(l[1]-min)));
        }
        
        return new long[]{min, max, bitSet.cardinality()};
    }
    
    static void importSuppGeneData(File suppGeneFile, String referenceGenomeBuild) throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(suppGeneFile);
        BufferedReader buf = new BufferedReader(reader);
        String line;
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        DaoReferenceGenomeGene daoRefGene = DaoReferenceGenomeGene.getInstance();
        ReferenceGenome refGenome = DaoReferenceGenome.getReferenceGenomeByBuildName(referenceGenomeBuild);
        while ((line = buf.readLine()) != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t");
                CanonicalGene gene = new CanonicalGene(parts[0]);
                ReferenceGenomeGene refGene = new ReferenceGenomeGene(
                                                gene.getEntrezGeneId(),
                                                refGenome.getReferenceGenomeId());
                if (!parts[1].isEmpty()) {
                    gene.setType(parts[1]);
                }
                if (!parts[2].isEmpty()) {
                    refGene.setCytoband(parts[2]);
                }
                daoGene.addGene(gene);
                daoRefGene.addOrUpdateGene(refGene);
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
                        parser.accepts( "gtf", "gtf file for calculating and storing gene lengths" ).withRequiredArg().describedAs( "gencode.<version>.annotation.gtf" ).ofType( String.class );
                        parser.accepts( "genome-build", "genome build eg GRCh38" ).withRequiredArg().describedAs( "genome build" ).ofType( String.class );
                        parser.accepts( "species", "different kinds of organisms eg. humna").withRequiredArg().describedAs( "species" ).ofType( String.class );
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
                    ImportGeneData.importData(geneFile, (String)options.valueOf("genome-build"));
                    MySQLbulkLoader.flushAll(); //Gene and gene_alias should be updated before calculating gene length (gtf)!
                }
                
                if(options.has("supp-genes")) {
                    File suppGeneFile = new File((String) options.valueOf("genes"));
                    System.out.println("Reading supp. gene data from:  " + suppGeneFile.getAbsolutePath());
                    numLines = FileUtil.getNumLines(suppGeneFile);
                    System.out.println(" --> total number of lines:  " + numLines);
                    ProgressMonitor.setMaxValue(numLines);
                    ImportGeneData.importSuppGeneData(suppGeneFile, 
                                        (String)options.valueOf("genome-build"));
                }
                
                if(options.has("microrna")) {
                    File miRNAFile = new File((String) options.valueOf("microrna"));
                    System.out.println("Reading miRNA data from:  " + miRNAFile.getAbsolutePath());
                    numLines = FileUtil.getNumLines(miRNAFile);
                    System.out.println(" --> total number of lines:  " + numLines);
                    ProgressMonitor.setMaxValue(numLines);
                    ImportMicroRNAIDs.importData(miRNAFile);
                }
                
                if(options.has("gtf")) {
                    File lociFile = new File((String) options.valueOf("gtf"));
                    String species = ReferenceGenome.HOMO_SAPIENS;
                    if (options.has("species")) {
                        species = (String)options.valueOf("species");
                    }
                    System.out.println("Reading loci data from:  " + lociFile.getAbsolutePath());
                    numLines = FileUtil.getNumLines(lociFile);
                    System.out.println(" --> total number of lines:  " + numLines);
                    ProgressMonitor.setMaxValue(numLines);
                    ImportGeneData.importGeneLength(lociFile, (String)options.valueOf("genome-build"), 
                                        species, options.has("genes"));
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
