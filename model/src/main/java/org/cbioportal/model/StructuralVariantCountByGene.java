package org.cbioportal.model;

import java.io.Serializable;

public class StructuralVariantCountByGene extends AlterationCountByGene implements Serializable {
    private String partnerGene;

    public String getPartnerGene() {
        return partnerGene;
    }

    public void setPartnerGene(String partnerGene) {
        this.partnerGene = partnerGene;
    }

}
