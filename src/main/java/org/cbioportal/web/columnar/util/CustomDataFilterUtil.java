package org.cbioportal.web.columnar.util;

import com.google.common.collect.Range;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.service.CustomDataService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.util.CustomDataSession;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.CustomDatatype;
import org.cbioportal.web.util.DataBinHelper;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CustomDataFilterUtil {
    private final SampleService sampleService;
    private final StudyViewFilterUtil studyViewFilterUtil;
    private final CustomDataService customDataService;
    private final DataBinHelper dataBinHelper;

    @Autowired
    public CustomDataFilterUtil(StudyViewRepository studyViewRepository, SampleService sampleService, StudyViewFilterUtil studyViewFilterUtil, CustomDataService customDataService, DataBinHelper dataBinHelper) {
        this.sampleService = sampleService;
        this.studyViewFilterUtil = studyViewFilterUtil;
        this.customDataService = customDataService;
        this.dataBinHelper = dataBinHelper;
    }

    Function<Sample, SampleIdentifier> sampleToSampleIdentifier = new Function<Sample, SampleIdentifier>() {

        public SampleIdentifier apply(Sample sample) {
            return studyViewFilterUtil.buildSampleIdentifier(sample.getCancerStudyIdentifier(), sample.getStableId());
        }
    };
    
    public List<CustomSampleIdentifier> extractCustomDataSamples(final StudyViewFilter studyViewFilter) {
        if (studyViewFilter == null) {
            return null;
        }

        List<CustomSampleIdentifier> customSampleIdentifiers = new ArrayList<>();

        if (CollectionUtils.isEmpty(studyViewFilter.getCustomDataFilters())) {
            return null;
        }

        final List<String> attributeIds = studyViewFilter.getCustomDataFilters().stream()
            .map(ClinicalDataFilter::getAttributeId)
            .collect(Collectors.toList());

        final Map<String, CustomDataSession> customDataSessions = customDataService.getCustomDataSessions(attributeIds);

        Map<String, CustomDataSession> customDataSessionById = customDataSessions
            .values()
            .stream()
            .collect(Collectors.toMap(
                CustomDataSession::getId,
                Function.identity()
            ));

        MultiKeyMap<String, String> customDataByStudySampleSession = new MultiKeyMap<>();

        customDataSessionById.values().forEach(customDataSession -> customDataSession
            .getData()
            .getData()
            .forEach(datum -> {
                String value = datum.getValue().toUpperCase();
                if (value.equals("NAN") || value.equals("N/A")) {
                    value = "NA";
                }
                CustomSampleIdentifier customSampleIdentifier = new CustomSampleIdentifier();
                customSampleIdentifier.setStudyId(datum.getStudyId());
                customSampleIdentifier.setSampleId(datum.getSampleId());
                customSampleIdentifier.setAttributeId(customDataSession.getId());
                customSampleIdentifiers.add(customSampleIdentifier);
                customDataByStudySampleSession.put(datum.getStudyId(), datum.getSampleId(), customDataSession.getId(), value);
            })
        );

        List<ClinicalDataFilter> equalityFilters = new ArrayList<>();
        List<ClinicalDataFilter> intervalFilters = new ArrayList<>();

        studyViewFilter.getCustomDataFilters().forEach(filter -> {
            String attributeId = filter.getAttributeId();
            if (!customDataSessionById.containsKey(attributeId)) {
                return;
            }
            if (customDataSessionById
                .get(attributeId)
                .getData()
                .getDatatype()
                .equals(CustomDatatype.STRING.name())
            ) {
                equalityFilters.add(filter);
            } else {
                intervalFilters.add(filter);
            }
        });

        List<CustomSampleIdentifier> filtered = new ArrayList<>();
        customSampleIdentifiers.forEach(customSampleIdentifier -> {
            int equalityFilterCount = studyViewFilterUtil.getFilteredCountByDataEquality(equalityFilters, customDataByStudySampleSession,
                customSampleIdentifier.getSampleId(), customSampleIdentifier.getStudyId(), false);
            int intervalFilterCount = getFilteredCountByDataInterval(intervalFilters, customDataByStudySampleSession,
                customSampleIdentifier.getSampleId(), customSampleIdentifier.getStudyId());
            if (equalityFilterCount == equalityFilters.size()
                && intervalFilterCount == intervalFilters.size()
            ) {
                filtered.add(customSampleIdentifier);
            }
            else {
                customSampleIdentifier.setIsFilteredOut(true);
                filtered.add(customSampleIdentifier);
            }
        });

        return filtered;
    }

    private <S> Integer getFilteredCountByDataInterval(List<ClinicalDataFilter> attributes, MultiKeyMap<String, S> clinicalDataMap,
                                                       String entityId, String studyId) {
        int count = 0;

        for (ClinicalDataFilter filter : attributes) {
            if (clinicalDataMap.containsKey(studyId, entityId, filter.getAttributeId())) {
                String attrValue = (String) clinicalDataMap.get(studyId, entityId, filter.getAttributeId());
                Range<BigDecimal> rangeValue = calculateRangeValueForAttr(attrValue);

                // find range filters
                List<Range<BigDecimal>> ranges = filter.getValues().stream()
                    .map(this::calculateRangeValueForFilter)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                // find special value filters
                List<String> specialValues = filter.getValues().stream()
                    .filter(f -> f.getValue() != null)
                    .map(f -> f.getValue().toUpperCase())
                    .collect(Collectors.toList());

                if (rangeValue != null) {
                    if (ranges.stream().anyMatch(r -> r.encloses(rangeValue))) {
                        count++;
                    }
                }
                else if (specialValues.contains(attrValue.toUpperCase())) {
                    count++;
                }
            } else if (containsNA(filter)) {
                count++;
            }
        }

        return count;
    }

    private Range<BigDecimal> calculateRangeValueForAttr(String attrValue) {
        if (attrValue == null) {
            return null;
        }

        BigDecimal min = null;
        BigDecimal max = null;

        String value = attrValue.trim();

        String lte = "<=";
        String lt = "<";
        String gte = ">=";
        String gt = ">";

        boolean startInclusive = true;
        boolean endInclusive = true;

        try {
            if (value.startsWith(lte)) {
                max = new BigDecimal(value.substring(lte.length()));
            }
            else if (value.startsWith(lt)) {
                max = new BigDecimal(value.substring(lt.length()));
                endInclusive = false;
            }
            else if (value.startsWith(gte)) {
                min = new BigDecimal(value.substring(gte.length()));
            }
            else if (value.startsWith(gt)) {
                min = new BigDecimal(value.substring(gt.length()));
                startInclusive = false;
            }
            else {
                min = max = new BigDecimal(attrValue);
            }
        } catch (Exception e) {
            // invalid range -- TODO: also support ranges like 20-30?
            return null;
        }

        return dataBinHelper.calcRange(min, startInclusive, max, endInclusive);
    }

    private Range<BigDecimal> calculateRangeValueForFilter(DataFilterValue filterValue) {
        BigDecimal start = filterValue.getStart();
        BigDecimal end = filterValue.getEnd();

        // default: (start, end]
        boolean startInclusive = false;
        boolean endInclusive = true;

        // special case: end == start (both inclusive)
        if (end != null && end.equals(start)) {
            startInclusive = true;
        }

        return dataBinHelper.calcRange(start, startInclusive, end, endInclusive);
    }

    private Boolean containsNA(ClinicalDataFilter filter) {
        return filter.getValues().stream().anyMatch(
            r -> r.getValue() != null && r.getValue().toUpperCase().equals("NA"));
    }
}
