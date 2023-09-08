package org.cbioportal.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SampleClinicalDataCollection extends HashMap<String, List<ClinicalData>> {

    public SampleClinicalDataCollection() {};

    public SampleClinicalDataCollection(List<ClinicalData> tableClinicalData) {
        Map<String, List<ClinicalData>> bySampelId = tableClinicalData.stream().collect(Collectors.groupingBy(ClinicalData::getSampleId));
        super.putAll(bySampelId);
    }

}
