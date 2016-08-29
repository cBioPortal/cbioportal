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

package org.mskcc.cbio.portal.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.StringUtilsLevenshtein;

/**
 * A Utility Class that speeds access to Gene Info.
 *
 * @author Ethan Cerami
 */
public class DaoGeneOptimized {
    private static final String CBIO_CANCER_GENES_FILE = "/cbio_cancer_genes.txt";
    private static final String GENE_SYMBOL_DISAMBIGUATION_FILE = "/gene_symbol_disambiguation.txt";
        
    private static final DaoGeneOptimized daoGeneOptimized = new DaoGeneOptimized();
    //nb: make sure any map is also cleared in clearCache() method below:
    private final HashMap<String, CanonicalGene> geneSymbolMap = new HashMap <String, CanonicalGene>();
    private final HashMap<Long, CanonicalGene> entrezIdMap = new HashMap <Long, CanonicalGene>();
    private final HashMap<String, List<CanonicalGene>> geneAliasMap = new HashMap<String, List<CanonicalGene>>();
    private final Set<CanonicalGene> cbioCancerGenes = new HashSet<CanonicalGene>();
    private final Map<String, CanonicalGene> disambiguousGenes = new HashMap<String, CanonicalGene>();
    
    /**
     * Private Constructor, to enforce singleton pattern.
     *
     * @throws DaoException Database Error.
     */
    private DaoGeneOptimized () {
        fillCache();
    }
    
    private synchronized void fillCache() {
        try {
            //  Automatically populate hashmap upon init
            ArrayList<CanonicalGene> globalGeneList = DaoGene.getAllGenes();
            for (CanonicalGene currentGene:  globalGeneList) {
                cacheGene(currentGene);
            }
        } catch (DaoException e) {
            e.printStackTrace();
        }
        
        try {
            if (geneSymbolMap.size()>10000) {
                // only for deployed version; not for unit test and importing
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(getClass().getResourceAsStream(CBIO_CANCER_GENES_FILE)));
                for (String line=in.readLine(); line!=null; line=in.readLine()) {
                    String[] parts = line.trim().split("\t",-1);
                    CanonicalGene gene = null;
                    if (parts.length>1) {
                        gene = getGene(Long.parseLong(parts[1]));
                    } else {
                        gene = getGene(parts[0]);
                    }
                    if (gene!=null) {
                        cbioCancerGenes.add(gene);
                    } else {
                        ProgressMonitor.logWarning(line+" in the cbio cancer gene list config file [resources" + CBIO_CANCER_GENES_FILE +
                                "] is not a HUGO gene symbol. You should either update this file or update the `gene` and `gene_alias` tables to fix this.");
                    }
                }
                in.close();
            }
            
            {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(getClass().getResourceAsStream(GENE_SYMBOL_DISAMBIGUATION_FILE)));
                for (String line=in.readLine(); line!=null; line=in.readLine()) {
                    if (line.startsWith("#")) {
                        continue;
                    }
                    String[] parts = line.trim().split("\t",-1);
                    CanonicalGene gene = getGene(Long.parseLong(parts[1]));
                    if (gene==null) {
                        ProgressMonitor.logWarning(line+" in config file [resources" + GENE_SYMBOL_DISAMBIGUATION_FILE +
                                "]is not valid. You should either update this file or update the `gene` and `gene_alias` tables to fix this.");
                    }
                    disambiguousGenes.put(parts[0], gene);
                }
                in.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void clearCache()
    {
        geneSymbolMap.clear();
        entrezIdMap.clear();
        geneAliasMap.clear();
        cbioCancerGenes.clear();
        disambiguousGenes.clear();
    }

    /**
     * Clear and fill cache again. Useful for unit tests and
     * for the Import procedure to update the genes table, clearing the
     * cache without the need to restart the webserver.
     */
    public synchronized void reCache()
    {
        clearCache();
        fillCache();
    }
    
    /**
     * Adds a new Gene Record to the Database. If the Entrez Gene ID is negative,
     * a fake Entrez Gene ID will be assigned.
     * @param gene  Canonical Gene Object.
     * @return number of records successfully added.
     * @throws DaoException Database Error.
     */
    public int addGene(CanonicalGene gene) throws DaoException {
        int ret;
        if (gene.getEntrezGeneId()>0) {
            ret = DaoGene.addGene(gene);
        } else {
            ret = DaoGene.addGeneWithoutEntrezGeneId(gene);
        }
        cacheGene(gene);
        return ret;
    }
    
    /**
     * Update database with gene length
     * @return number of records updated.
     * @throws DaoException
     */
    public int flushUpdateToDatabase() throws DaoException {
        DaoGene.deleteAllRecords();
        MySQLbulkLoader.bulkLoadOn();
        int ret = 0;
        for (CanonicalGene gene : getAllGenes()) {
            ret += DaoGene.addGene(gene);
        }
        MySQLbulkLoader.flushAll();
        return ret;
    }
    
    public void deleteGene(CanonicalGene gene) throws DaoException {
        DaoGene.deleteGene(gene.getEntrezGeneId());
        geneSymbolMap.remove(gene.getHugoGeneSymbolAllCaps());
        for (String alias : gene.getAliases()) {
            String aliasUp = alias.toUpperCase();
            List<CanonicalGene> genes = geneAliasMap.get(aliasUp);
            genes.remove(gene);
            if (genes.isEmpty()) {
                geneAliasMap.remove(aliasUp);
            }
        }
    }
    
    private void cacheGene(CanonicalGene gene) {
        geneSymbolMap.put(gene.getHugoGeneSymbolAllCaps(), gene);
        entrezIdMap.put(gene.getEntrezGeneId(), gene);

        for (String alias : gene.getAliases()) {
            String aliasUp = alias.toUpperCase();
            List<CanonicalGene> genes = geneAliasMap.get(aliasUp);
            if (genes==null) {
                genes = new ArrayList<CanonicalGene>();
                geneAliasMap.put(aliasUp, genes);
            }
            genes.add(gene);
        }
    }

    /**
     * Gets Global Singleton Instance.
     *
     * @return DaoGeneOptimized Singleton.
     * @throws DaoException Database Error.
     */
    public static DaoGeneOptimized getInstance() {
        return daoGeneOptimized;
    }

    /**
     * Gets Gene by HUGO Gene Symbol.
     *
     * @param hugoGeneSymbol HUGO Gene Symbol.
     * @return Canonical Gene Object.
     */
    public CanonicalGene getGene(String hugoGeneSymbol) {
        return geneSymbolMap.get(hugoGeneSymbol.toUpperCase());
    }

    /**
     * Looks for a Gene where HUGO Gene Symbol or an alias matches the given symbol.
     *
     * @param geneSymbol: HUGO Gene Symbol or an alias
     * @param searchInAliases: set to true if this method should search for a match in this.geneAliasMap
     * in case a matching hugo symbol cannot be found in this.geneSymbolMap
     *
     * @return
     */
    public List<CanonicalGene> getGene(String geneSymbol, boolean searchInAliases) {
        CanonicalGene gene = getGene(geneSymbol);
        if (gene!=null) {
            return Collections.singletonList(gene);
        }
        
        if (searchInAliases) {
            List<CanonicalGene> genes = geneAliasMap.get(geneSymbol.toUpperCase());
            if (genes!=null) {
                return Collections.unmodifiableList(genes);
            }
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Gets Gene By Entrez Gene ID.
     *
     * @param entrezId Entrez Gene ID.
     * @return Canonical Gene Object.
     */
    public CanonicalGene getGene(long entrezId) {
        return entrezIdMap.get(entrezId);
    }
    
    /**
     * Look for genes with a specific ID. First look for genes with the specific
     * Entrez Gene ID, if found return this gene; then for HUGO symbol, if found,
     * return this gene; and lastly for aliases, if found, return a list of
     * matched genes (could be more than one). If nothing matches, return an
     * empty list.
     * @param geneId an Entrez Gene ID or HUGO symbol or gene alias
     * @return A list of genes that match, an empty list if no match.
     */
    public List<CanonicalGene> guessGene(String geneId) {
        return guessGene(geneId, null);
    }
    
    /**
     * Look for genes with a specific ID on a chr. First look for genes with the specific
     * Entrez Gene ID, if found return this gene; then for HUGO symbol, if found,
     * return this gene; and lastly for aliases, if found, return a list of
     * matched genes (could be more than one). If chr is not null, use that to match too.
     * If nothing matches, return an empty list.
     * @param geneId an Entrez Gene ID or HUGO symbol or gene alias
     * @param chr chromosome
     * @return A list of genes that match, an empty list if no match.
     */
    public List<CanonicalGene> guessGene(String geneId, String chr) {
        if (geneId==null) {
            return Collections.emptyList();
        }
        
        CanonicalGene gene;
        if (geneId.matches("[0-9]+")) { // likely to be a entrez gene id
            gene = getGene(Integer.parseInt(geneId));
            if (gene!=null) {
                return Collections.singletonList(gene);
            }
        }
        
        gene = getGene(geneId); // HUGO gene symbol
        if (gene!=null) {
            return Collections.singletonList(gene);
        }
        
        List<CanonicalGene> genes = geneAliasMap.get(geneId.toUpperCase());
        if (genes!=null) {
            if (chr==null) {
                return Collections.unmodifiableList(genes);
            }
            
            String nchr = normalizeChr(chr);
            
            List<CanonicalGene> ret = new ArrayList<CanonicalGene>();
            for (CanonicalGene cg : genes) {
                String gchr = getChrFromCytoband(cg.getCytoband());
                if (gchr==null // TODO: should we exlude this?
                        || gchr.equals(nchr)) {
                    ret.add(cg);
                }
            }
            return ret;
        }
        
        return Collections.emptyList();
    }
    
    
    private static Map<String,String> validChrValues = null;
    public static String normalizeChr(String strChr) {
        if (strChr==null) {
            return null;
        }
        
        if (validChrValues==null) {
            validChrValues = new HashMap<String,String>();
            for (int lc = 1; lc<=24; lc++) {
                    validChrValues.put(Integer.toString(lc),Integer.toString(lc));
                    validChrValues.put("CHR" + Integer.toString(lc),Integer.toString(lc));
            }
            validChrValues.put("X","23");
            validChrValues.put("CHRX","23");
            validChrValues.put("Y","24");
            validChrValues.put("CHRY","24");
            validChrValues.put("NA","NA");
            validChrValues.put("MT","MT"); // mitochondria
        }

        return validChrValues.get(strChr);
    }
    
    private static String getChrFromCytoband(String cytoband) {
        if (cytoband==null) {
            return null;
        }
        
        if (cytoband.startsWith("X")) {
            return "23";
        }
        
        if (cytoband.startsWith("Y")) {
            return "24";
        }
        
        Pattern p = Pattern.compile("([0-9]+).*");
        Matcher m = p.matcher(cytoband);
        if (m.find()) {
            return m.group(1);
        }
        
        return null;
    }
    
    /**
     * Look for gene that can be non-ambiguously determined.
     * @param geneId an Entrez Gene ID or HUGO symbol or gene alias
     * @return a gene that can be non-ambiguously determined, or null if cannot.
     */
    public CanonicalGene getNonAmbiguousGene(String geneId) {
        return getNonAmbiguousGene(geneId, null);
    }
    
    /**
     * Look for gene that can be non-ambiguously determined.
     * @param geneId an Entrez Gene ID or HUGO symbol or gene alias
     * @param chr chromosome
     * @return a gene that can be non-ambiguously determined, or null if cannot.
     */
    public CanonicalGene getNonAmbiguousGene(String geneId, String chr) {
        List<CanonicalGene> genes = guessGene(geneId, chr);
        if (genes.isEmpty()) {
            return null;
        }
        
        if (genes.size()==1) {
            return genes.get(0);
        }
        
        if (disambiguousGenes.containsKey(geneId)) {
            return disambiguousGenes.get(geneId);
        }
        
        StringBuilder sb = new StringBuilder("Ambiguous alias ");
        sb.append(geneId);
        sb.append(": corresponding entrez ids of ");
        for (CanonicalGene gene : genes) {
            sb.append(gene.getEntrezGeneId());
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        
        ProgressMonitor.logWarning(sb.toString());
        return null;
        
    }
    
    public Set<Long> getEntrezGeneIds(Collection<CanonicalGene> genes) {
        Set<Long> entrezGeneIds = new HashSet<Long>();
        for (CanonicalGene gene : genes) {
            entrezGeneIds.add(gene.getEntrezGeneId());
        }
        return entrezGeneIds;
    }
    
    public Set<CanonicalGene> getCbioCancerGenes() {
        return Collections.unmodifiableSet(cbioCancerGenes);
    }
    
    public boolean isCbioCancerGene(CanonicalGene gene) {
        return cbioCancerGenes.contains(gene);
    }

    /**
     * Gets an ArrayList of All Genes.
     * @return Array List of All Genes.
     */
    public ArrayList<CanonicalGene> getAllGenes () {
        return new ArrayList<CanonicalGene>(entrezIdMap.values());
    }

    /**
     * Deletes all Gene Records in the Database.
     * @throws DaoException Database Error.
     */
    public void deleteAllRecords() throws DaoException {
        DaoGene.deleteAllRecords();
    }
    
    /**
     * Suggests gene symbols closest in Levenshtein distance to supplied gene symbol
     * @param geneId an Entrez Gene ID or HUGO symbol or gene alias
     * @return Array of suggested gene symbols
     */
    public Collection<String> getGeneSuggestions(String geneId) {
        Set<String> results = new HashSet<String>();
        if (geneId.length() < 3) //provide no suggestions for < 3-lettered symbols
            return results;
    
        int min = 4; // minimum Levenshtein distance to be a reasonable suggestion
        geneId = geneId.toUpperCase();
        
        for (String key : geneSymbolMap.keySet()) {
            int score = StringUtilsLevenshtein.getLevenshteinDistance(key, geneId);
            if (score < min) {
                results.clear();
                min = score;
                results.add(key);
            } else if (score == min) {
                results.add(key);
            }
        }
        return results;
    }
}
