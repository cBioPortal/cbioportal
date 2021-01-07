package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utilities for operations on sets and importing/updating gene panels.
 *
 * @author jtquach1
 */
public class GenePanelUtil {

    /**
     * Extracts a property value from a given propertyName in properties.
     * extractGenes is used instead for the propertyName "gene_list".
     *
     * @param propertyName   May be "stable_id" or "description".
     * @param properties
     * @param noSpaceAllowed noSpaceAllowed=false may be used for propertyName="description".
     * @return A String representing the propertyValue following the propertyName.
     * @throws IllegalArgumentException
     */
    public static String extractPropertyValue(String propertyName, Properties properties, boolean noSpaceAllowed) throws IllegalArgumentException {
        String propertyValue = properties.getProperty(propertyName);

        if (propertyValue == null) {
            throw new NullPointerException(propertyName + " does not exist in properties.");
        }

        String propertyValueTrimmed = propertyValue.trim();
        if (propertyValueTrimmed.isEmpty()) {
            throw new IllegalArgumentException(propertyName + " is not specified.");
        }

        if (noSpaceAllowed && propertyValueTrimmed.contains(" ")) {
            throw new IllegalArgumentException(propertyName + " cannot contain spaces: " + propertyValueTrimmed);
        }

        return propertyValueTrimmed;
    }

    /**
     * Extracts genes from the tab-delimited list after the "gene_list" parameter.
     * If updating, set allowEmptyGenePanel=true.
     *
     * @param properties
     * @param allowEmptyGenePanel
     * @return If a gene cannot be found, don't return any of the genes.
     */
    public static Set<CanonicalGene> extractGenes(Properties properties, Boolean allowEmptyGenePanel) {
        String propertyValue = properties.getProperty("gene_list").trim();
        if (propertyValue.length() == 0) {
            if (allowEmptyGenePanel) {
                return new HashSet<>();
            }
            throw new IllegalArgumentException("gene_list is not specified.");
        }

        String[] genes = propertyValue.split("\t");
        Set<CanonicalGene> canonicalGenes = new HashSet<>();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

        //Check if genes are duplicated and report them
        for (int i = 0; i < genes.length; i++) {
            for (int j = i + 1 ; j < genes.length; j++) {
                if (genes[i].equals(genes[j])) {
                    ProgressMonitor.logWarning("The following gene is duplicated: "+genes[i]);
                }
            }
        }

        for (String panelGene : genes) {
            try {
                long geneId = Long.parseLong(panelGene);
                CanonicalGene canonicalGene = daoGeneOptimized.getGene(geneId);

                if (canonicalGene != null) {
                    canonicalGenes.add(canonicalGene);
                } else {
                    ProgressMonitor.logWarning("Could not find gene in the database: " + geneId);
                }
            } catch (NumberFormatException e) {
                List<CanonicalGene> canonicalGenesList = daoGeneOptimized.getGene(panelGene, true);

                if (canonicalGenesList != null && !canonicalGenesList.isEmpty()) {
                    // we do not want multiple genes added to the gene panel object
                    // for a single gene symbol found in the data file 
                    canonicalGenes.add(canonicalGenesList.get(0));
                } else {
                    ProgressMonitor.logWarning("Could not find gene in the database: " + panelGene);
                }
            }
        }

        return (canonicalGenes.size() == genes.length) ? canonicalGenes : null;
    }

    /**
     * Gets a Pair representing the gene sets to add and to remove from a gene panel
     * in the database, since MySQL throws an error when trying to add duplicate genes.
     * add represents genes from incoming that original does not have.
     * remove represents genes from original that incoming does not have.
     *
     * @param incoming New genes to replace original
     * @param original Old genes currently in the database
     * @return Pair represents the unique genes from the incoming and original gene panels.
     */
    public static Pair getAddRemove(Set<CanonicalGene> incoming, Set<CanonicalGene> original) {
        Set<CanonicalGene> add = incoming.stream()
            .filter(e -> !original.contains(e))
            .collect(Collectors.toSet());
        Set<CanonicalGene> remove = original.stream()
            .filter(e -> !incoming.contains(e))
            .collect(Collectors.toSet());

        return new Pair(add, remove);
    }

    public static final class Pair {
        public final Set<CanonicalGene> add, remove;

        public Pair(Set<CanonicalGene> add, Set<CanonicalGene> remove) {
            this.add = add;
            this.remove = remove;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return Objects.equals(add, pair.add) &&
                Objects.equals(remove, pair.remove);
        }

        @Override
        public int hashCode() {
            return Objects.hash(add, remove);
        }
    }

}
