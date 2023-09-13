package org.cbioportal.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleClinicalDataCollection {

    private Map<String, List<ClinicalData>> sampleClinicalData = new HashMap<>(); 
    
    public Map<String, List<ClinicalData>> getSampleClinicalData() {
        return sampleClinicalData;
    }

    public void setSampleClinicalData(Map<String, List<ClinicalData>> sampleClinicalData) {
        this.sampleClinicalData = sampleClinicalData;
    }
    
}
