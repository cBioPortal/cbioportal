package org.mskcc.cgds.model;

import java.util.Collections;
import java.util.Set;

/**
 * Class to wrap Entrez Gene ID, HUGO Gene Symbols,etc.
 */
public class CanonicalGene extends Gene {
    private long entrezGeneId;
    private String hugoGeneSymbol;
    private Set<String> aliases;
    private double somaticMutationFrequency;

    public CanonicalGene(long entrezGeneId, String hugoGeneSymbol) {
        this(entrezGeneId, hugoGeneSymbol, null);
    }

    public CanonicalGene(long entrezGeneId, String hugoGeneSymbol, Set<String> aliases) {
        this.entrezGeneId = entrezGeneId;
        this.hugoGeneSymbol = hugoGeneSymbol;
        this.aliases = aliases;
    }

    public Set<String> getAliases() {
        if (aliases==null) {
            return Collections.emptySet();
        }
        return aliases;
    }

    public void setAliases(Set<String> aliases) {
        this.aliases = aliases;
    }

    public long getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(long entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getHugoGeneSymbolAllCaps() {
        return hugoGeneSymbol.toUpperCase();
    }

    public String getStandardSymbol() {
        return getHugoGeneSymbolAllCaps();
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    @Override
    public String toString() {
        return this.getHugoGeneSymbolAllCaps();
    }

    @Override
    public boolean equals(Object obj0) {
        if (!(obj0 instanceof CanonicalGene)) {
            return false;
        }
        
        CanonicalGene gene0 = (CanonicalGene) obj0;
        if (gene0.getEntrezGeneId() == entrezGeneId) {
            return true;
        }
        return false;
    }

    public double getSomaticMutationFrequency() {
        return somaticMutationFrequency;
    }

    public void setSomaticMutationFrequency(double somaticMutationFrequency) {
        this.somaticMutationFrequency = somaticMutationFrequency;
    }

    @Override
    public int hashCode() {
        return (int) entrezGeneId;
    }
}