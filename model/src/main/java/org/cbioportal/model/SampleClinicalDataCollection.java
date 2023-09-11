package org.cbioportal.model;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SampleClinicalDataCollection {

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder().withoutPadding();
    public static final String DELIMITER = ":";

    private Map<String, List<ClinicalData>> sampleClinicalData = new HashMap<>(); 
    
    public SampleClinicalDataCollection() {};
    
    public SampleClinicalDataCollection(List<ClinicalData> tableClinicalData) {
        sampleClinicalData = tableClinicalData.stream().collect(Collectors.groupingBy(clinicalData -> 
            calculateBase64(clinicalData.getSampleId(), clinicalData.getStudyId())
        ));
    }

    public Map<String, List<ClinicalData>> getSampleClinicalData() {
        return sampleClinicalData;
    }

    public void setSampleClinicalData(Map<String, List<ClinicalData>> sampleClinicalData) {
        this.sampleClinicalData = sampleClinicalData;
    }

    private String calculateBase64(String firstInput, String secondInput) {
        return BASE64_ENCODER.encodeToString((firstInput + DELIMITER + secondInput).getBytes());
    }
    
}
