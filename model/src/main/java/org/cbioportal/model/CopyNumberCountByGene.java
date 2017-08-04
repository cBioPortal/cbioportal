package org.cbioportal.model;

import java.io.Serializable;

public class CopyNumberCountByGene extends AlterationCountByGene implements Serializable {
    
    private Integer alteration;
    
    public Integer getAlteration() {
        return alteration;
    }

    public void setAlteration(Integer alteration) {
        this.alteration = alteration;
    }
}
