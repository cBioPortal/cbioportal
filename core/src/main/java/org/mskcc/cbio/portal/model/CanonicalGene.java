/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.model;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoSangerCensus;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class to wrap Entrez Gene ID, HUGO Gene Symbols,etc.
 */
public class CanonicalGene extends Gene {
    public static final String MIRNA_TYPE = "miRNA";
    public static final String PHOSPHOPROTEIN_TYPE = "phosphoprotein";
    private long entrezGeneId;
    private String hugoGeneSymbol;
    private Set<String> aliases;
    private double somaticMutationFrequency;
    private String cytoband;
    private int length;
    private String type;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * 
     * @return gene length; 0 if no available
     */
    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getCytoband() {
        return cytoband;
    }

    public void setCytoband(String cytoband) {
        this.cytoband = cytoband;
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
        return MIRNA_TYPE.equals(type);
    }
    
    public boolean isPhosphoProtein() {
        return PHOSPHOPROTEIN_TYPE.equals(type);
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