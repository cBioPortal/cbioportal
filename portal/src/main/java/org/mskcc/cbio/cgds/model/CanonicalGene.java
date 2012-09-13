package org.mskcc.cbio.cgds.model;

import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoSangerCensus;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class to wrap Entrez Gene ID, HUGO Gene Symbols,etc.
 */
public class CanonicalGene extends Gene {
    private long entrezGeneId;
    private String hugoGeneSymbol;
    private Set<String> aliases;
    private double somaticMutationFrequency;

    public CanonicalGene(String hugoGeneSymbol) {
        this(-1, hugoGeneSymbol);
    }

    public CanonicalGene(long entrezGeneId, String hugoGeneSymbol) {
        this(entrezGeneId, hugoGeneSymbol, null);
    }

    public CanonicalGene(String hugoGeneSymbol, Set<String> aliases) {
        this(-1, hugoGeneSymbol, aliases);
    }

    public CanonicalGene(long entrezGeneId, String hugoGeneSymbol, Set<String> aliases) {
        this.entrezGeneId = entrezGeneId;
        this.hugoGeneSymbol = hugoGeneSymbol;
        setAliases(aliases);
    }

    public Set<String> getAliases() {
        if (aliases==null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(aliases);
    }

    public void setAliases(Set<String> aliases) {
        if (aliases==null) {
            this.aliases = null;
            return;
        }
        
        Map<String,String> map = new HashMap<String,String>(aliases.size());
        for (String alias : aliases) {
            map.put(alias.toUpperCase(), alias);
        }
        
        this.aliases = new HashSet<String>(map.values());
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
    
    public boolean isMicroRNA() {
        String hugo = getHugoGeneSymbolAllCaps();
        return hugo.startsWith("MIR-") || hugo.startsWith("LET-");
    }
    
    public boolean isPhosphoProtein() {
        String hugo = this.getHugoGeneSymbolAllCaps();
        return hugo.matches("[A-Za-z0-9]+_P[STY][0-9]+.*");
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

    public boolean isSangerGene() throws DaoException {
        DaoSangerCensus daoSangerCensus = DaoSangerCensus.getInstance();

        String hugo = this.getHugoGeneSymbolAllCaps();

        return daoSangerCensus.getCancerGeneSet().containsKey(hugo);
    }

    @Override
    public int hashCode() {
        return (int) entrezGeneId;
    }
}