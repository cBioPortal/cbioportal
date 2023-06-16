package org.cbioportal.model;

import java.util.ArrayList;
import java.util.List;

public class ClinicalDataCollection {

    /**
     * Paginated resource
     */
    private List<ClinicalData> sampleClinicalData = new ArrayList<>();

    /**
     * Patient info associated with paginated samples
     */
    private List<ClinicalData> patientClinicalData = new ArrayList<>();
    
    public List<ClinicalData> getSampleClinicalData() {
        return sampleClinicalData;
    }

    public void setSampleClinicalData(List<ClinicalData> sampleClinicalData) {
        this.sampleClinicalData = sampleClinicalData;
    }

    public List<ClinicalData> getPatientClinicalData() {
        return patientClinicalData;
    }

    public void setPatientClinicalData(List<ClinicalData> patientClinicalData) {
        this.patientClinicalData = patientClinicalData;
    }
}
