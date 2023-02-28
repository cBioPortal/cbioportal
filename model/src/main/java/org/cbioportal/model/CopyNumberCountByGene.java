package org.cbioportal.model;

import java.io.Serializable;

public class CopyNumberCountByGene extends AlterationCountByGene implements Serializable {
    
    private Integer alteration;
    private String cytoband;

    public Integer getAlteration() {
        return alteration;
    }

    public void setAlteration(Integer alteration) {
        this.alteration = alteration;
    }

	public String getCytoband() {
		return cytoband;
	}

	public void setCytoband(String cytoband) {
		this.cytoband = cytoband;
	}
}
