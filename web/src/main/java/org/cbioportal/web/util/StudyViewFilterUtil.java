package org.cbioportal.web.util;

import com.google.common.collect.Range;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudyViewFilterUtil 
{
    public void extractStudyAndSampleIds(List<SampleIdentifier> sampleIdentifiers, List<String> studyIds, List<String> sampleIds)
    {
        for (SampleIdentifier sampleIdentifier : sampleIdentifiers) {
            studyIds.add(sampleIdentifier.getStudyId());
            sampleIds.add(sampleIdentifier.getSampleId());
        }
    }
    
    public void removeSelfFromFilter(String attributeId, StudyViewFilter studyViewFilter)
    {
        if (studyViewFilter!= null && studyViewFilter.getClinicalDataEqualityFilters() != null) {
            studyViewFilter.getClinicalDataEqualityFilters().removeIf(f -> f.getAttributeId().equals(attributeId));
        }
        
        if (studyViewFilter!= null && studyViewFilter.getClinicalDataIntervalFilters() != null) {
            studyViewFilter.getClinicalDataIntervalFilters().removeIf(f -> f.getAttributeId().equals(attributeId));
        }
    }

    public Range<Double> calcRange(Double start, boolean startInclusive, Double end, boolean endInclusive)
    {
        // check for invalid filter (no start or end provided)
        if (start == null && end == null) {
            return null;
        }
        else if (start == null) {
            if (endInclusive) {
                return Range.atMost(end);
            }
            else {
                return Range.lessThan(end);
            }
        }
        else if (end == null) {
            if (startInclusive) {
                return Range.atLeast(start);
            }
            else {
                return Range.greaterThan(start);
            }
        }
        else if (startInclusive) {
            if (endInclusive) {
                return Range.closed(start, end);
            }
            else {
                return Range.closedOpen(start, end);
            }
        }
        else {
            if (endInclusive) {
                return Range.openClosed(start, end);
            }
            else {
                return Range.open(start, end);
            }
        }
    }
}
