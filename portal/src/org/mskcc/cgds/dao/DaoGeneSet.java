package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.CanonicalGene;

import java.util.Comparator;

class GeneComparator implements Comparator {

    public int compare(Object o, Object o1) {
        CanonicalGene gene0 = (CanonicalGene) o;
        CanonicalGene gene1 = (CanonicalGene) o1;
        return (gene0.getHugoGeneSymbolAllCaps().compareTo(gene1.getHugoGeneSymbolAllCaps()));
    }
}
