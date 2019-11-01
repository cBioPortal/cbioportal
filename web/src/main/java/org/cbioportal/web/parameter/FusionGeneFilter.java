package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class FusionGeneFilter implements Serializable {

    private List<String> hugoGeneSymbols;

    public List<String> getHugoGeneSymbols() {
        return hugoGeneSymbols;
    }

    public void setHugoGeneSymbols(List<String> hugoGeneSymbols) {
        this.hugoGeneSymbols = hugoGeneSymbols;
    }

}
