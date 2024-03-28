package org.cbioportal.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleClinicalDataCollection {

    private Map<String, List<ClinicalData>> byUniqueSampleKey = new HashMap<>(); 
    
    public Map<String, List<ClinicalData>> getByUniqueSampleKey() {
        return byUniqueSampleKey;
    }

    public void setByUniqueSampleKey(Map<String, List<ClinicalData>> byUniqueSampleKey) {
        this.byUniqueSampleKey = byUniqueSampleKey;
    }
    
}
