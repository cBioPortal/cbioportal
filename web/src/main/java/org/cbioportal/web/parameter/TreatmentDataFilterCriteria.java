package org.cbioportal.web.parameter;

import java.util.List;

/**
 * Wrapper class for specifying filter criteria for GeneticData items
 * in the GeneticDataController services.
 *
 * @author Pim van Nierop, pim@thehyve.nl
 *
 */
public class TreatmentDataFilterCriteria {

    //The list of identifiers for the treatments of interest.
    private List<String> treatmentIds;
    //Identifier of pre-defined sample list with samples to query. E.g. brca_tcga_all
    private String sampleListId;
    //Full list of samples or patients to query, E.g. list with TCGA-AR-A1AR-01, TCGA-BH-A1EO-01...
    private List<String> sampleIds;

    public List<String> getTreatmentIds() {
        return treatmentIds;
    }

    public void setTreatmentIds(List<String> treatmentIds) {
        this.treatmentIds = treatmentIds;
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
