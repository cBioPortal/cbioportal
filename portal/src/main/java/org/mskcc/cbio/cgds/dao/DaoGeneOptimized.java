/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.cgds.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mskcc.cbio.cgds.model.CanonicalGene;

/**
 * A Utility Class that speeds access to Gene Info.
 *
 * @author Ethan Cerami
 */
public class DaoGeneOptimized {
    private static final String IMPACT_GENES_FILE = "/IMPACT_genes.txt";
        
    private static DaoGeneOptimized daoGeneOptimized;
    private HashMap<String, CanonicalGene> geneSymbolMap = new HashMap <String, CanonicalGene>();
    private HashMap<Long, CanonicalGene> entrezIdMap = new HashMap <Long, CanonicalGene>();
    private HashMap<String, List<CanonicalGene>> geneAliasMap = new HashMap<String, List<CanonicalGene>>();
    private Set<String> impactGenes = new HashSet<String>();

    /**
     * Private Constructor, to enforce singleton pattern.
     * 
     * @throws DaoException Database Error.
     */
    private DaoGeneOptimized () throws DaoException {
        DaoGene daoGene = DaoGene.getInstance();

        //  Automatically populate hashmap upon init
        ArrayList<CanonicalGene> globalGeneList = daoGene.getAllGenes();
        for (CanonicalGene currentGene:  globalGeneList) {
            cacheGene(currentGene);
        }
        
        try {
            if (geneSymbolMap.size()>10000) { 
                // only for deployed version; not for unit test and importing
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(getClass().getResourceAsStream(IMPACT_GENES_FILE)));
                for (String line=in.readLine(); line!=null; line=in.readLine()) {
                    if (geneSymbolMap.containsKey(line)) {
                        impactGenes.add(line);
                    } else {
                        System.err.println(line+" in the IMPACT gene list is not a HUGO gene symbol.");
                    }
                }
                in.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a new Gene Record to the Database. If the Entrez Gene ID is negative,
     * a fake Entrez Gene ID will be assigned.
     * @param gene  Canonical Gene Object.
     * @return number of records successfully added.
     * @throws DaoException Database Error.
     */
    public int addGene(CanonicalGene gene) throws DaoException {
        DaoGene daoGene = DaoGene.getInstance();
        int ret;
        if (gene.getEntrezGeneId()>0) {
            ret = daoGene.addGene(gene);
        } else {
            ret = daoGene.addGeneWithoutEntrezGeneId(gene);
        }
        cacheGene(gene);
        return ret;
    }
    
    public void deleteGene(CanonicalGene gene) throws DaoException {
        DaoGene daoGene = DaoGene.getInstance();
        daoGene.deleteGene(gene.getEntrezGeneId());
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
     * Loads the temp file maintained by the MySQLbulkLoader into the DMBS.
     *
     * @return number of records inserted
     * @throws DaoException Database Error.
     */
    public int flushGenesToDatabase() throws DaoException {
        DaoGene daoGene = DaoGene.getInstance();
        return daoGene.flushGenesToDatabase();
    }

    /**
     * Gets Global Singleton Instance.
     *
     * @return DaoGeneOptimized Singleton.
     * @throws DaoException Database Error.
     */
    public static DaoGeneOptimized getInstance() throws DaoException {
        if (daoGeneOptimized == null) {
            daoGeneOptimized = new DaoGeneOptimized();
        }
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
    
    /**
     * Look for gene that can be non-ambiguously determined.
     * @param geneId an Entrez Gene ID or HUGO symbol or gene alias
     * @return a gene that can be non-ambiguously determined, or null if cannot.
     */
    public CanonicalGene getNonAmbiguousGene(String geneId) {
        List<CanonicalGene> genes = guessGene(geneId);
        if (genes.isEmpty()) {
            return null;
        }
        
        if (genes.size()!=1) {
            StringBuilder sb = new StringBuilder("Ambiguous alias ");
            sb.append(geneId);
            sb.append(": corresponding entrez ids of ");
            for (CanonicalGene gene : genes) {
                sb.append(gene.getEntrezGeneId());
                sb.append(",");
            }
            sb.deleteCharAt(sb.length()-1);
            
            System.err.println(sb.toString());
            return null;
        }
        
        return genes.get(0);
    }
    
    public Set<String> getIMPACTGenes() {
        return Collections.unmodifiableSet(impactGenes);
    }
    
    public boolean isIMPACTGene(String hugoSymbolUpper) {
        return impactGenes.contains(hugoSymbolUpper);
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
        DaoGene daoGene = DaoGene.getInstance();
        daoGene.deleteAllRecords();
    }
}