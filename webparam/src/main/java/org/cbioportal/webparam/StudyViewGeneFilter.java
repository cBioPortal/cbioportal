package org.cbioportal.webparam;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class StudyViewGeneFilter implements Serializable {

    private Set<String> molecularProfileIds;
    private List<List<GeneFilterQuery>> geneQueries;

    public Set<String> getMolecularProfileIds() {
        return molecularProfileIds;
    }

    public void setMolecularProfileIds(Set<String> molecularProfileIds) {
        this.molecularProfileIds = molecularProfileIds;
    }

    public List<List<GeneFilterQuery>> getGeneQueries() {
        return geneQueries;
    }

    public void setGeneQueries(List<List<GeneFilterQuery>> geneQueries) {
        this.geneQueries = geneQueries;
    }

}