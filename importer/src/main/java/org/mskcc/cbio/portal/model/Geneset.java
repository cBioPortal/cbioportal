/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.*;

/**
 * @author ochoaa
 * @author Sander Tan
 */

public class Geneset implements Serializable {

    @JsonIgnore
    private int id;
    @JsonIgnore
    private int geneticEntityId;
    @JsonProperty("genesetId")
    private String externalId;
    private String name;
    private String description;
    private String refLink;
    @JsonIgnore
    private Set<Long> genesetGeneIds;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the geneticEntityId
     */
    public int getGeneticEntityId() {
        return geneticEntityId;
    }

    /**
     * @param geneticEntityId the geneticEntityId to set
     */
    public void setGeneticEntityId(Integer geneticEntityId) {
        this.geneticEntityId = geneticEntityId;
    }

    /**
     * @return the externalId
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * @param externalId the externalId to set
     */
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the refLink
     */
    public String getRefLink() {
        return refLink;
    }

    /**
     * @param refLink the refLink to set
     */
    public void setRefLink(String refLink) {
        this.refLink = refLink;
    }

    /**
     * @return the genesetGenes
     */
    public Set<Long> getGenesetGeneIds() {
        return genesetGeneIds;
    }

    /**
     * @param genesetGenes the genesetGenes to set
     */
    public void setGenesetGenes(Set<Long> genesetGeneIds) {
        this.genesetGeneIds = genesetGeneIds;
    }
    
}
