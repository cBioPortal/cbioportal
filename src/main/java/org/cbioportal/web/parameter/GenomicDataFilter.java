package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class GenomicDataFilter extends DataFilter implements Serializable {
    private String hugoGeneSymbol;
    private String profileType;

    public GenomicDataFilter() {}
    
    public GenomicDataFilter(String hugoGeneSymbol, String profileType) {
        this.hugoGeneSymbol = hugoGeneSymbol;
        this.profileType = profileType;
    }
    
    public GenomicDataFilter(String hugoGeneSymbol, String profileType, List<DataFilterValue> values) {
        this.hugoGeneSymbol = hugoGeneSymbol;
        this.profileType = profileType;
        this.setValues(values);
    }
    
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
