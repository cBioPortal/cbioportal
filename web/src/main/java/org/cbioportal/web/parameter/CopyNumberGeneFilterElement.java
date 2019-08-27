package org.cbioportal.web.parameter;

import javax.validation.constraints.AssertTrue;

public class CopyNumberGeneFilterElement {

    private String hugoGeneSymbol;
    private Integer alteration;

    @AssertTrue
    private boolean isAlterationMinusTwoOrTwo() {
        return alteration == -2 || alteration == 2;
    }

    @AssertTrue
    private boolean isHugoGeneSymbolPresent() {
        return hugoGeneSymbol != null;
    }

    public String getHugoGeneSymbol() {
        return hugoGeneSymbol;
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    public Integer getAlteration() {
        return alteration;
    }

    public void setAlteration(Integer alteration) {
        this.alteration = alteration;
    }
}
