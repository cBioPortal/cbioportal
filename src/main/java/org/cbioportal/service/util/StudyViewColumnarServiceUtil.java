package org.cbioportal.service.util;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StudyViewColumnarServiceUtil {
    
    private StudyViewColumnarServiceUtil() {}
    
    public static List<ClinicalDataCountItem> mergeClinicalDataCounts(
        List<ClinicalDataCountItem> items
    ) {
        items.forEach(attr -> {
            Map<String, List<ClinicalDataCount>> countsPerType = attr.getCounts().stream()
                .collect(Collectors.groupingBy(ClinicalDataCount::getValue));
            List<ClinicalDataCount> res = countsPerType.entrySet().stream().map(entry -> {
                ClinicalDataCount mergedCount = new ClinicalDataCount();
                mergedCount.setAttributeId(attr.getAttributeId());
                mergedCount.setValue(entry.getKey());
                mergedCount.setCount(entry.getValue().stream().mapToInt(ClinicalDataCount::getCount).sum());
                return mergedCount;
            }).toList();
            attr.setCounts(res);
        });
        return items;
    }

    public static List<ClinicalDataCountItem> addClinicalDataCountsForMissingAttributes(
        List<ClinicalDataCountItem> counts,
        List<ClinicalAttribute> attributes, 
        Integer filteredSampleCount,
        Integer filteredPatientCount
    ) {
        Map<String, ClinicalDataCountItem> map = counts.stream()
            .collect(Collectors.toMap(ClinicalDataCountItem::getAttributeId, item -> item));

        List<ClinicalDataCountItem> result = new ArrayList<>(counts);
        
        attributes.forEach(attr -> {
            Integer count = attr.getPatientAttribute().booleanValue() ? filteredPatientCount : filteredSampleCount;

            if (!map.containsKey(attr.getAttrId())) {
                ClinicalDataCountItem newItem = new ClinicalDataCountItem();
                newItem.setAttributeId(attr.getAttrId());
                ClinicalDataCount countObj = new ClinicalDataCount();
                countObj.setCount(count);
                countObj.setValue("NA");
                countObj.setAttributeId(attr.getAttrId());
                newItem.setCounts(List.of(countObj));
                result.add(newItem);
            }
        });

        return result;
    }
    
    
}