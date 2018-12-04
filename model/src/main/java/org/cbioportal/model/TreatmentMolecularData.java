package org.cbioportal.model;

import java.io.Serializable;

public class TreatmentMolecularData extends MolecularData implements Serializable {

    private String treatmentId;
    
    public String getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(String treatmentId) {
        this.treatmentId = treatmentId;
    }

    @Override
    public String getStableId() {
        return getTreatmentId();
    }

}