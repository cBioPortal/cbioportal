package org.cbioportal.web.parameter;

import java.io.Serializable;

public class GenomicDataBinFilter extends DataBinFilter implements Serializable {
    private String hugoGeneSymbol;
    private String profileType;

    public String getHugoGeneSymbol() {
        return hugoGeneSymbol;
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    public String getProfileType() {
        return profileType;
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }

}
