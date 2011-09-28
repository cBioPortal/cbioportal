package org.mskcc.cgds.model;

import java.util.Collections;
import java.util.Set;

public class CanonicalGene extends Gene {
    private long entrezGeneId;
    private String hugoGeneSymbol;
    private Set<String> aliases;
    private String name;
    private double somaticMutationFrequency;
    
    public CanonicalGene(long entrezGeneId, String hugoGeneSymbol) {
        this(entrezGeneId, hugoGeneSymbol, null, "");
    }

    public CanonicalGene(long entrezGeneId, String hugoGeneSymbol, Set<String> aliases, String name) {
        this.entrezGeneId = entrezGeneId;
        this.hugoGeneSymbol = hugoGeneSymbol;
        this.aliases = aliases;
        this.name = name;
    }

    public Set<String> getAliases() {
        if (aliases==null)
            return Collections.emptySet();
        return aliases;
    }

    public void setAliases(Set<String> aliases) {
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String toString() {
        return this.getHugoGeneSymbolAllCaps();
    }

    public boolean equals(Object obj0) {
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

    public int hashCode() {
        return (int) entrezGeneId;
    }
}