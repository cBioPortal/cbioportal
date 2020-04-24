/*
 * Copyright (c) 2015 - 2019 Memorial Sloan-Kettering Cancer Center.
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

/**
 * A Utility Class that speeds access to Gene Info.
 *
 * @author Ethan Cerami
 */
public class DaoGeneOptimized {
    private static final String GENE_SYMBOL_DISAMBIGUATION_FILE = "/gene_symbol_disambiguation.txt";

    private static final DaoGeneOptimized daoGeneOptimized = new DaoGeneOptimized();
    //nb: make sure any map is also cleared in clearCache() method below:
    private final HashMap<String, CanonicalGene> geneSymbolMap = new HashMap <String, CanonicalGene>();
    private final HashMap<Long, CanonicalGene> entrezIdMap = new HashMap <Long, CanonicalGene>();
    private final HashMap<Integer, CanonicalGene> geneticEntityMap = new HashMap<Integer, CanonicalGene>();
    private final HashMap<String, List<CanonicalGene>> geneAliasMap = new HashMap<String, List<CanonicalGene>>();
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
            BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(GENE_SYMBOL_DISAMBIGUATION_FILE)));
            for (String line=in.readLine(); line!=null; line=in.readLine()) {
                if (line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.trim().split("\t",-1);
                CanonicalGene gene = getGene(Long.parseLong(parts[1]));
                if (gene==null) {
                    ProgressMonitor.logWarning(line+" in config file [resources" + GENE_SYMBOL_DISAMBIGUATION_FILE +
                            "]is not valid. You should either update this file or update the `gene` and `gene_alias` tables to fix this.");
                } else {
                    disambiguousGenes.put(parts[0], gene);
                }
            }
            in.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void clearCache() {
        geneSymbolMap.clear();
        entrezIdMap.clear();
        geneticEntityMap.clear();
        geneAliasMap.clear();
        disambiguousGenes.clear();
    }

    /**
     * Clear and fill cache again. Useful for unit tests and
     * for the Import procedure to update the genes table, clearing the
     * cache without the need to restart the webserver.
     */
    public synchronized void reCache() {
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
            ret = DaoGene.addOrUpdateGene(gene);
        } else {
            ret = DaoGene.addGeneWithoutEntrezGeneId(gene);
        }
        //only overwrite cache if ..?
        cacheGene(gene);
        return ret;
    }

    /**
     * Update Gene Record in the Database. It will also replace this
     * gene's aliases with the ones found in the given gene object.
     */
    public int updateGene(CanonicalGene gene)  throws DaoException {
        int ret = DaoGene.updateGene(gene);
        //recache:
        cacheGene(gene);
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
        geneticEntityMap.put(gene.getGeneticEntityId(), gene);

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
     * Return geneticEntityId from cache for given entrezGeneId
     *
     * @param entrezGeneId
     * @return
     */
    public static int getGeneticEntityId(long entrezGeneId) {
        //get entity id from cache:
        CanonicalGene gene = daoGeneOptimized.getGene(entrezGeneId);
        if (gene != null) {
            return gene.getGeneticEntityId();
        }
        else {
            throw new RuntimeException("Invalid entrezGeneId symbol. Not found in cache: " + entrezGeneId);
        }
    }

    /**
     * Return entrezGeneId from cache for given geneticEntityId
     *
     * @param geneticEntityId
     * @return
     */
    public static long getEntrezGeneId(int geneticEntityId) {
        //get entity id from cache:
        CanonicalGene gene = daoGeneOptimized.getGeneByEntityId(geneticEntityId);
        //since not every genetic entity will be a gene, this could be null (but would
        //be a programming error elsewhere, so throw exception):
        if (gene == null) {
            throw new RuntimeException("Genetic entity was not found in gene cache: " + geneticEntityId);
        }

        return daoGeneOptimized.getGeneByEntityId(geneticEntityId).getEntrezGeneId();
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
        if (gene != null) {
            return Collections.singletonList(gene);
        }
        if (searchInAliases) {
            return getGenesForAlias(geneSymbol);
        }
        return Collections.emptyList();
    }


    /**
     * Looks for all Gene records linked to a gene symbol in the gene_alias table.
     *
     * @param geneSymbol: HUGO Gene Symbol (alias)
     *
     * @return empty list if there are no linked Genes, or a unmodifiable list of linked genes
     */
    public List<CanonicalGene> getGenesForAlias(String geneSymbol) {
        List<CanonicalGene> genes = geneAliasMap.get(geneSymbol.toUpperCase());
        if (genes != null) {
            return Collections.unmodifiableList(genes);
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
     * Gets Gene By Entrez Gene ID.
     *
     * @param geneticEntityId Gene Entity ID.
     * @return Canonical Gene Object.
     */
    public CanonicalGene getGeneByEntityId(int geneticEntityId) {
        return geneticEntityMap.get(geneticEntityId);
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
            return Collections.unmodifiableList(genes);
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
        return getNonAmbiguousGene(geneId, true);
    }

    /**
     * Look for gene that can be non-ambiguously determined.
     * @param geneId an Entrez Gene ID or HUGO symbol or gene alias
     * @param chr chromosome
     * @return a gene that can be non-ambiguously determined, or null if cannot.
     */
    public CanonicalGene getNonAmbiguousGene(String geneId, String chr) {
        return getNonAmbiguousGene(geneId, true);
    }

    /**
     * Look for gene that can be non-ambiguously determined
     * @param geneId an Entrez Gene ID or HUGO symbol or gene alias
     * @param issueWarning if true and gene is not ambiguous, 
     * print all the Entrez Ids corresponding to the geneId provided
     * @return a gene that can be non-ambiguously determined, or null if cannot.
     */
    public CanonicalGene getNonAmbiguousGene(String geneId, boolean issueWarning) {
        List<CanonicalGene> genes = guessGene(geneId);
        if (genes.isEmpty()) {
            return null;
        }

        if (genes.size()==1) {
            return genes.get(0);
        }

        if (disambiguousGenes.containsKey(geneId)) {
            return disambiguousGenes.get(geneId);
        }
        if (issueWarning) {
            StringBuilder sb = new StringBuilder("Ambiguous alias ");
            sb.append(geneId);
            sb.append(": corresponding entrez ids of ");
            for (CanonicalGene gene : genes) {
                sb.append(gene.getEntrezGeneId());
                sb.append(",");
            }
            sb.deleteCharAt(sb.length()-1);

            ProgressMonitor.logWarning(sb.toString());
        }
        return null;

    }

    public Set<Long> getEntrezGeneIds(Collection<CanonicalGene> genes) {
        Set<Long> entrezGeneIds = new HashSet<Long>();
        for (CanonicalGene gene : genes) {
            entrezGeneIds.add(gene.getEntrezGeneId());
        }
        return entrezGeneIds;
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
     *
     * @deprecated  only used by deprecated code, so deprecating this as well.
     */
    public void deleteAllRecords() throws DaoException {
        DaoGene.deleteAllRecords();
    }

}
