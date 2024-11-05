package org.cbioportal.service.util;

import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StudyViewColumnarServiceUtil {
    
    public static List<ClinicalDataCountItem> mergeClinicalDataCounts(List<ClinicalDataCountItem> items) {
        items.forEach(attr -> {
            Map<String, List<ClinicalDataCount>> countsPerType = attr.getCounts().stream()
                .collect(Collectors.groupingBy(ClinicalDataCount::getValue));
            List<ClinicalDataCount> res = countsPerType.entrySet().stream().map((entry) -> {
                ClinicalDataCount mergedCount = new ClinicalDataCount();
                mergedCount.setAttributeId(attr.getAttributeId());
                mergedCount.setValue(entry.getKey());
                mergedCount.setCount(entry.getValue().stream().mapToInt(ClinicalDataCount::getCount).sum());
                return mergedCount;
            }).collect(Collectors.toList());
            attr.setCounts(res);
        });
        return items;
    }
    
    
}