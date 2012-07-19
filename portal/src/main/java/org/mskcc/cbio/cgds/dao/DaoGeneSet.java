package org.mskcc.cbio.cgds.dao;

import org.mskcc.cgds.model.CanonicalGene;

import java.util.Comparator;

/**
 * Compares two genes by their HUGO Symbols--ignores case
 */
class GeneComparator implements Comparator {

    public int compare(Object o, Object o1) {
        CanonicalGene gene0 = (CanonicalGene) o;
        CanonicalGene gene1 = (CanonicalGene) o1;
        return (gene0.getHugoGeneSymbolAllCaps().compareTo(gene1.getHugoGeneSymbolAllCaps()));
    }
}
