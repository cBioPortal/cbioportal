package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class CopyNumberGeneFilter implements Serializable {

    private List<CopyNumberGeneFilterElement> alterations;

    public List<CopyNumberGeneFilterElement> getAlterations() {
        return alterations;
    }

    public void setAlterations(List<CopyNumberGeneFilterElement> alterations) {
        this.alterations = alterations;
    }
}
