package org.cbioportal.service.exception;

public class TreatmentNotFoundException extends Exception {
    
	private String stableId;

    public TreatmentNotFoundException(String treatmentId) {
        super();
        this.stableId = treatmentId;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String treatmentId) {
        this.stableId = treatmentId;
    }
}
