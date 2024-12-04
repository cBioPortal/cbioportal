package org.cbioportal.service.util;

import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.DataFilter;
import org.cbioportal.web.parameter.DataFilterValue;
import org.cbioportal.web.parameter.GenericAssayDataFilter;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class StudyViewColumnarServiceUtil {
    
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

    public static List<GenomicDataCount> mergeGenomicDataCounts(List<GenomicDataCount> sampleCounts) {
        Map<String, List<GenomicDataCount>> countsPerType = sampleCounts.stream()
            .collect(Collectors.groupingBy(GenomicDataCount::getValue));

        List<GenomicDataCount> mergedCounts = new ArrayList<>();
        for (Map.Entry<String, List<GenomicDataCount>> entry : countsPerType.entrySet()) {
            var dc = new GenomicDataCount();
            dc.setValue(entry.getKey());
            dc.setLabel(entry.getValue().get(0).getLabel());
            Integer sum = entry.getValue().stream()
                .map(GenomicDataCount::getCount)
                .collect(Collectors.summingInt(Integer::intValue));
            dc.setCount(sum);
            mergedCounts.add(dc);
        }
        return mergedCounts;
    }

    public static List<CaseListDataCount> mergeCaseListCounts(List<CaseListDataCount> counts) {
        Map<String, List<CaseListDataCount>> countsPerListType = counts.stream()
            .collect((Collectors.groupingBy(CaseListDataCount::getValue)));

        // different cancer studies combined into one cohort will have separate case lists
        // of a given type (e.g. rppa).  We need to merge the counts for these
        // different lists based on the type and choose a label
        // this code just picks the first label, which assumes that the labels will match for a give type
        List<CaseListDataCount> mergedCounts = new ArrayList<>();
        for (Map.Entry<String,List<CaseListDataCount>> entry : countsPerListType.entrySet()) {
            var dc = new CaseListDataCount();
            dc.setValue(entry.getKey());
            // here just snatch the label of the first profile
            dc.setLabel(entry.getValue().get(0).getLabel());
            Integer sum = entry.getValue().stream()
                .map(x -> x.getCount())
                .collect(Collectors.summingInt(Integer::intValue));
            dc.setCount(sum);
            mergedCounts.add(dc);
        }
        return mergedCounts;
    }

    /**
     * Normalizes data counts by merging attribute values in a case-insensitive way.
     * For example attribute values "TRUE", "True", and 'true' will be merged into a single aggregated count.
     * This method assumes that all the counts in the given dataCounts list has the same attributeId.
     *
     * @param dataCounts list of data counts for a single attribute
     *
     * @return normalized list of data counts
     */
    public static List<ClinicalDataCount> normalizeDataCounts(List<ClinicalDataCount> dataCounts) {
        Collection<ClinicalDataCount> normalizedDataCounts = dataCounts
            .stream()
            .collect(
                Collectors.groupingBy(
                    c -> c.getValue().toLowerCase(),
                    Collectors.reducing(new ClinicalDataCount(), (count1, count2) -> {
                        // assuming attribute ids are the same for all data counts, just pick the first one
                        String attributeId =
                            count1.getAttributeId() != null
                                ? count1.getAttributeId()
                                : count2.getAttributeId();

                        // pick the value in a deterministic way by prioritizing lower case over upper case.
                        // for example, 'True' will be picked in case of 2 different values like 'TRUE', and 'True',
                        // and 'true' will be picked in case of 3 different values like 'TRUE', 'True', and 'true'
                        String value = count1.getValue() != null
                            ? count1.getValue()
                            : count2.getValue();
                        if (count1.getValue() != null && count2.getValue() != null) {
                            value = count1.getValue().compareTo(count2.getValue()) > 0
                                ? count1.getValue()
                                : count2.getValue();
                        }

                        // aggregate counts for the merged values
                        Integer count = (count1.getCount() != null ? count1.getCount(): 0) +
                            (count2.getCount() != null ? count2.getCount(): 0);

                        ClinicalDataCount aggregated = new ClinicalDataCount();
                        aggregated.setAttributeId(attributeId);
                        aggregated.setValue(value);
                        aggregated.setCount(count);
                        return aggregated;
                    })
                )
            )
            .values();

        return new ArrayList<>(normalizedDataCounts);
    }
    
    public static void mergeDataFilterNumericalValues(StudyViewFilter studyViewFilter) {
        if (studyViewFilter.getGenomicDataFilters() != null && !studyViewFilter.getGenomicDataFilters().isEmpty()) {
            List<GenomicDataFilter> mergedGenomicDataFilters = mergeDataFilters(studyViewFilter.getGenomicDataFilters());
            studyViewFilter.setGenomicDataFilters(mergedGenomicDataFilters);
        }

        if (studyViewFilter.getClinicalDataFilters() != null && !studyViewFilter.getClinicalDataFilters().isEmpty()) {
            List<ClinicalDataFilter> mergedClinicalDataFilters = mergeDataFilters(studyViewFilter.getClinicalDataFilters());
            studyViewFilter.setClinicalDataFilters(mergedClinicalDataFilters);
        }

        if (studyViewFilter.getGenericAssayDataFilters() != null && !studyViewFilter.getGenericAssayDataFilters().isEmpty()) {
            List<GenericAssayDataFilter> mergedGenericAssayDataFilters = mergeDataFilters(studyViewFilter.getGenericAssayDataFilters());
            studyViewFilter.setGenericAssayDataFilters(mergedGenericAssayDataFilters);
        }
    }

    /**
     * Merge the range of numerical bins in DataFilters to reduce the number of scans that runs on the database when filtering.
     */
    private static <T extends DataFilter> List<T> mergeDataFilters(List<T> filters) {
        List<T> mergedDataFilters = new ArrayList<>();

        for (T filter : filters) {
            List<DataFilterValue> mergedValues = new ArrayList<>();

            BigDecimal mergedStart = null, mergedEnd = null;
            for (DataFilterValue dataFilterValue : filter.getValues()) {
                // leave non-numerical values as they are
                if (dataFilterValue.getValue() != null) {
                    mergedValues.add(dataFilterValue);
                }
                // merge adjacent numerical bins
                else {
                    BigDecimal start = dataFilterValue.getStart();
                    BigDecimal end = dataFilterValue.getEnd();

                    if (mergedStart == null && mergedEnd == null) {
                        mergedStart = start;
                        mergedEnd = end;
                    }
                    else if (mergedEnd.equals(start)) mergedEnd = end;
                    else {
                        mergedValues.add(new DataFilterValue(mergedStart, mergedEnd, null));
                        mergedStart = start;
                        mergedEnd = end;
                    }
                }
            }

            mergedValues.add(new DataFilterValue(mergedStart, mergedEnd, null));
            filter.setValues(mergedValues);
            mergedDataFilters.add(filter);
        }

        return mergedDataFilters;
    }
}