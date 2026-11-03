/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.*;

/**
 * Class to wrap Entrez Gene ID, HUGO Gene Symbols,etc.
 */
public class CanonicalGene extends Gene implements Serializable {
    public static final String MIRNA_TYPE = "miRNA";
    public static final String PHOSPHOPROTEIN_TYPE = "phosphoprotein";
    private int geneticEntityId;
    @JsonProperty("entrezGeneId")
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
    private long entrezGeneId;
    @JsonProperty("hugoGeneSymbol")
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

    @Override
    public int hashCode() {
        return (int) entrezGeneId;
    }
}
