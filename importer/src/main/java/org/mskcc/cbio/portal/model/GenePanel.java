/*
 * Copyright (c) 2016 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mskcc.cbio.portal.model;

/**
 *
 * @author heinsz
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.*;

public class GenePanel  implements Serializable {

    @JsonIgnore
    private Integer internalId;
    @JsonProperty("genePanelId")
    private String stableId;
    private String description;
    private Set<CanonicalGene> genes;

    public Integer getInternalId() {
        return internalId;
    }

    public void setInternalId(Integer internalId) {
        this.internalId = internalId;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<CanonicalGene> getGenes() {
        return genes;
    }

    public void setGenes(Set<CanonicalGene> genes) {
        this.genes = genes;
    }

}
