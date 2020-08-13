/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.model;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoSangerCensus;

import java.util.*;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class to wrap Entrez Gene ID, HUGO Gene Symbols,etc.
 */
public class CanonicalGene extends Gene implements Serializable {
    public static final String MIRNA_TYPE = "miRNA";
    public static final String PHOSPHOPROTEIN_TYPE = "phosphoprotein";
    private int geneticEntityId;
    @JsonProperty("entrez_gene_id")
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
    private long entrezGeneId;
    @JsonProperty("hugo_gene_symbol")
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
    private String hugoGeneSymbol;
    private Set<String> aliases;
    private double somaticMutationFrequency;
    private String type;

    /**
     * @deprecated: hardly used (2 places that are not relevant, 
     * potentially dead code), should be deprecated 
     * 
     * @param hugoGeneSymbol
     */
    public CanonicalGene(String hugoGeneSymbol) {
        this(hugoGeneSymbol, null);
    }

    /**
     * This constructor can be used to get a rather empty object 
     * representing this gene symbol. 
     * 
     * Note: Its most important use is for data loading of "phosphogenes"
     * (ImportTabDelimData.importPhosphoGene) where a dummy gene record
     * is generated on the fly for this entity. TODO this constructor needs
     * to be deprecated once "phosphogenes" become a genetic entity on their own. 
     * 
     * @param hugoGeneSymbol
     */
    public CanonicalGene(String hugoGeneSymbol, Set<String> aliases) {
        this(-1, -1, hugoGeneSymbol, aliases);
    }

    /** 
     * This constructor can be used when geneticEntityId is not yet known, 
     * e.g. in case of a new gene (like when adding new genes in ImportGeneData), or is
     * not needed (like when retrieving mutation data)
     * 
     * @param entrezGeneId
     * @param hugoGeneSymbol
     */
    public CanonicalGene(long entrezGeneId, String hugoGeneSymbol) {
        this(-1, entrezGeneId, hugoGeneSymbol, null);
    }

    /**
     * This constructor can be used when geneticEntityId is not yet known, 
     * e.g. in case of a new gene (like when adding new genes in ImportGeneData), or is
     * not needed (like when retrieving mutation data)
     * 
     * @param entrezGeneId
     * @param hugoGeneSymbol
     * @param aliases
     */
    public CanonicalGene(long entrezGeneId, String hugoGeneSymbol, Set<String> aliases) {
    	this(-1, entrezGeneId, hugoGeneSymbol, aliases);
    }

    public CanonicalGene(int geneticEntityId, long entrezGeneId, String hugoGeneSymbol, Set<String> aliases) {
   		this.geneticEntityId = geneticEntityId;
    	this.entrezGeneId = entrezGeneId;
        this.hugoGeneSymbol = hugoGeneSymbol;
        setAliases(aliases);
    }

    @JsonIgnore
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonIgnore
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
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

    @JsonIgnore
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
    public int getGeneticEntityId() {
    	return geneticEntityId;
    }

    public void setGeneticEntityId(int geneticEntityId) {
        this.geneticEntityId = geneticEntityId;
    }

    public long getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(long entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    @JsonIgnore
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
    public String getHugoGeneSymbolAllCaps() {
        return hugoGeneSymbol.toUpperCase();
    }

    @JsonIgnore
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
    public String getStandardSymbol() {
        return getHugoGeneSymbolAllCaps();
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    @JsonIgnore
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
    public boolean isMicroRNA() {
        return MIRNA_TYPE.equals(type);
    }

    @JsonIgnore
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
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

    @JsonIgnore
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
    public double getSomaticMutationFrequency() {
        return somaticMutationFrequency;
    }

    public void setSomaticMutationFrequency(double somaticMutationFrequency) {
        this.somaticMutationFrequency = somaticMutationFrequency;
    }

    @JsonIgnore
    // significant speedup to jackson json writing in
    // DumpPortalInfo.java with this field ignored (not needed)
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
