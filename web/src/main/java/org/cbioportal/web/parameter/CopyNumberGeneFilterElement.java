package org.cbioportal.web.parameter;

import javax.validation.constraints.AssertTrue;

public class CopyNumberGeneFilterElement {

    private Integer entrezGeneId;
    private Integer alteration;

    @AssertTrue
    private boolean isAlterationMinusTwoOrTwo() {
        return alteration == -2 || alteration == 2;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public Integer getAlteration() {
        return alteration;
    }

    public void setAlteration(Integer alteration) {
        this.alteration = alteration;
    }
}
