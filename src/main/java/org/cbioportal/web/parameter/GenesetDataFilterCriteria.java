package org.cbioportal.web.parameter;

import java.util.List;

/**
 * Wrapper class for specifying filter criteria for GeneticData items
 * in the GeneticDataController services.
 *
 * @author pieter
 *
 */
public class GenesetDataFilterCriteria {

    //The list of identifiers for the gene sets of interest.
    private List<String> genesetIds;
    //Identifier of pre-defined sample list with samples to query. E.g. brca_tcga_all
    private String sampleListId;
    //Full list of samples or patients to query, E.g. list with TCGA-AR-A1AR-01, TCGA-BH-A1EO-01...
    private List<String> sampleIds;

    public List<String> getGenesetIds() {
        return genesetIds;
    }

    public void setGenesetIds(List<String> genesetIds) {
        this.genesetIds = genesetIds;
    }

    public String getSampleListId() {
        return sampleListId;
    }

    public void setSampleListId(String sampleListId) {
        this.sampleListId = sampleListId;
    }

    public List<String> getSampleIds() {
        return sampleIds;
    }

    public void setSampleIds(List<String> sampleIds) {
        this.sampleIds = sampleIds;
    }
}
