/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.model;

/**
 *
 * @author heinsz
 */

import java.io.Serializable;
import java.util.List;

public class GenePanel  implements Serializable{
    
    private Integer internalId;
    private String stableId;
    private String description;
    private List<Gene> genes;
    
    public GenePanel() {}
    
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
    
    public List<Gene> getGenes() {
        return genes;
    }
    
    public void setGenes(List<Gene> genes) {
        this.genes = genes;
    }
    
}
