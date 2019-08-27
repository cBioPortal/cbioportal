package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.AssertTrue;

public class MutationGeneFilter implements Serializable {

    private List<String> hugoGeneSymbols;

    @AssertTrue
    private boolean areHugoGeneSymbolsPresent() {
        return hugoGeneSymbols != null && !hugoGeneSymbols.isEmpty();
    }

    public List<String> getHugoGeneSymbols() {
        return hugoGeneSymbols;
    }

    public void setHugoGeneSymbols(List<String> hugoGeneSymbols) {
        this.hugoGeneSymbols = hugoGeneSymbols;
    }
}
