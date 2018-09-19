package org.cbioportal.web.util;

import org.apache.commons.collections.map.MultiKeyMap;
import com.google.common.collect.Range;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.SampleService;
import org.cbioportal.web.parameter.ClinicalDataIntervalFilter;
import org.cbioportal.web.parameter.ClinicalDataIntervalFilterValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ClinicalDataIntervalFilterApplier extends ClinicalDataFilterApplier<ClinicalDataIntervalFilter>
{
    @Autowired
    public ClinicalDataIntervalFilterApplier(PatientService patientService, 
                                             ClinicalDataService clinicalDataService, 
                                             SampleService sampleService,
                                             StudyViewFilterUtil studyViewFilterUtil) 
    {
        super(patientService, clinicalDataService, sampleService, studyViewFilterUtil);
    }

    @Override
    public Integer apply(List<ClinicalDataIntervalFilter> attributes,
                         MultiKeyMap clinicalDataMap,
                         String entityId,
                         String studyId,
                         Boolean negateFilters)
    {
        int count = 0;

        for (ClinicalDataIntervalFilter filter : attributes) {
            List<ClinicalData> entityClinicalData = (List<ClinicalData>)clinicalDataMap.get(entityId, studyId);
            if (entityClinicalData != null) {
                Optional<ClinicalData> clinicalData = entityClinicalData.stream().filter(c -> c.getAttrId()
                    .equals(filter.getAttributeId())).findFirst();
                if (clinicalData.isPresent()) 
                {
                    String attrValue = clinicalData.get().getAttrValue();
                    Range<Double> rangeValue = calculateRangeValueForAttr(attrValue);

                    // find range filters
                    List<Range<Double>> ranges = filter.getValues().stream()
                        .map(this::calculateRangeValueForFilter)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                    // find special value filters
                    List<String> specialValues = filter.getValues().stream()
                        .filter(f -> f.getValue() != null)
                        .map(f -> f.getValue().toUpperCase())
                        .collect(Collectors.toList());

                    if (rangeValue != null) {
                        if (negateFilters ^ ranges.stream().anyMatch(r -> r.encloses(rangeValue))) {
                            count++;
                        }
                    } 
                    else if (negateFilters ^ specialValues.contains(attrValue.toUpperCase())) {
                        count++;
                    }
                } else if (negateFilters ^ containsNA(filter)) {
                    count++;
                }
            } else if (negateFilters ^ containsNA(filter)) {
                count++;
            }
        }

        return count;
    }

    private Range<Double> calculateRangeValueForAttr(String attrValue)
    {
        if (attrValue == null) {
            return null;
        }
        
        Double min = null;
        Double max = null;
        
        String value = attrValue.trim();
        
        String lte = "<=";
        String lt = "<";
        String gte = ">=";
        String gt = ">";
        
        boolean startInclusive = true;
        boolean endInclusive = true;
        
        try {
            if (value.startsWith(lte)) {
                max = Double.parseDouble(value.substring(lte.length()));
            }
            else if (value.startsWith(lt)) {
                max = Double.parseDouble(value.substring(lt.length()));
                endInclusive = false;
            }
            else if (value.startsWith(gte)) {
                min = Double.parseDouble(value.substring(gte.length()));
            }
            else if (value.startsWith(gt)) {
                min = Double.parseDouble(value.substring(gt.length()));
                startInclusive = false;
            }
            else {
                min = max = Double.parseDouble(attrValue);
            }
        } catch (Exception e) {
            // invalid range -- TODO: also support ranges like 20-30?
            return null;
        }
        
        return studyViewFilterUtil.calcRange(min, startInclusive, max, endInclusive);
    }

    private Range<Double> calculateRangeValueForFilter(ClinicalDataIntervalFilterValue filterValue) 
    {
        Double start = filterValue.getStart();
        Double end = filterValue.getEnd();

        // default: (start, end]
        boolean startInclusive = false;
        boolean endInclusive = true;
        
        // special case: end == start (both inclusive)
        if (end != null && end.equals(start)) {
            startInclusive = true;
        }
        
        // TODO also add startInclusive and endInclusive as a filterValue parameter?
        
        return studyViewFilterUtil.calcRange(start, startInclusive, end, endInclusive);
    }

    private Boolean containsNA(ClinicalDataIntervalFilter filter)
    {
        return filter.getValues().stream().anyMatch(
            r -> r.getValue() != null && r.getValue().toUpperCase().equals("NA"));
    }
}
