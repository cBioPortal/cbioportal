package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

public class AlterationEnrichment implements Serializable {

    @NotNull
    private Integer entrezGeneId;
    @NotNull
    private String hugoGeneSymbol;
    private String cytoband;
    @NotNull
    private Integer alteredInSet1Count;
    @NotNull
    private Integer alteredInSet2Count;
    @NotNull
    private String logRatio;
    @NotNull
    private BigDecimal pValue;
    @NotNull
    private Integer profiledInSet1Count;
    @NotNull
    private Integer profiledInSet2Count;
    
    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getHugoGeneSymbol() {
        return hugoGeneSymbol;
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    public String getCytoband() {
        return cytoband;
    }

    public void setCytoband(String cytoband) {
        this.cytoband = cytoband;
    }

    public Integer getAlteredInSet1Count() {
        return alteredInSet1Count;
    }

    public void setAlteredInSet1Count(Integer alteredInSet1Count) {
        this.alteredInSet1Count = alteredInSet1Count;
    }

    public Integer getAlteredInSet2Count() {
        return alteredInSet2Count;
    }

    public void setAlteredInSet2Count(Integer alteredInSet2Count) {
        this.alteredInSet2Count = alteredInSet2Count;
    }

    public String getLogRatio() {
        return logRatio;
    }

    public void setLogRatio(String logRatio) {
        this.logRatio = logRatio;
    }

    public BigDecimal getpValue() {
        return pValue;
    }

    public void setpValue(BigDecimal pValue) {
        this.pValue = pValue;
    }

    public Integer getProfiledInSet1Count() {
        return profiledInSet1Count;
    }

    public void setProfiledInSet1Count(Integer profiledInSet1Count) {
        this.profiledInSet1Count = profiledInSet1Count;
    }

    public Integer getProfiledInSet2Count() {
        return profiledInSet2Count;
    }

    public void setProfiledInSet2Count(Integer profiledInSet2Count) {
        this.profiledInSet2Count = profiledInSet2Count;
    }
}
