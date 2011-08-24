package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.CanonicalGene;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;

/**
 * A Utility Class that speeds access to Gene Info.
 *
 * @author Ethan Cerami
 */
public class DaoGeneOptimized {
    private static DaoGeneOptimized daoGeneOptimized;
    private HashMap<String, CanonicalGene> geneSymbolMap = new HashMap <String, CanonicalGene>();
    private HashMap<Long, CanonicalGene> entrezIdMap = new HashMap <Long, CanonicalGene>();

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
            geneSymbolMap.put(currentGene.getHugoGeneSymbol(), currentGene);
            entrezIdMap.put(currentGene.getEntrezGeneId(), currentGene);
        }
    }

    /**
     * Adds a new Gene Record to the Database.
     * @param gene  Canonical Gene Object.
     * @return number of records successfully added.
     * @throws DaoException Database Error.
     */
    public int addGene(CanonicalGene gene) throws DaoException {
        geneSymbolMap.put(gene.getHugoGeneSymbol(), gene);
        entrezIdMap.put(gene.getEntrezGeneId(), gene);
        DaoGene daoGene = DaoGene.getInstance();
        return daoGene.addGene(gene);
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
        return geneSymbolMap.get(hugoGeneSymbol);
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