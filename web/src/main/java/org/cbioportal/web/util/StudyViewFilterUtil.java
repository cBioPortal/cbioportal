package org.cbioportal.web.util;

import com.google.common.collect.Range;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StudyViewFilterUtil {
    public void extractStudyAndSampleIds(List<SampleIdentifier> sampleIdentifiers, List<String> studyIds, List<String> sampleIds) {
        for (SampleIdentifier sampleIdentifier : sampleIdentifiers) {
            studyIds.add(sampleIdentifier.getStudyId());
            sampleIds.add(sampleIdentifier.getSampleId());
        }
    }

    public void removeSelfFromFilter(String attributeId, StudyViewFilter studyViewFilter) {
        if (studyViewFilter!= null && studyViewFilter.getClinicalDataFilters() != null) {
            studyViewFilter.getClinicalDataFilters().removeIf(f -> f.getAttributeId().equals(attributeId));
        }
    }

    public Range<BigDecimal> calcRange(BigDecimal start, boolean startInclusive, BigDecimal end, boolean endInclusive) {
        // check for invalid filter (no start or end provided)
        if (start == null && end == null) {
            return null;
        } else if (start == null) {
            if (endInclusive) {
                return Range.atMost(end);
            } else {
                return Range.lessThan(end);
            }
        } else if (end == null) {
            if (startInclusive) {
                return Range.atLeast(start);
            } else {
                return Range.greaterThan(start);
            }
        } else if (startInclusive) {
            if (endInclusive) {
                return Range.closed(start, end);
            } else {
                return Range.closedOpen(start, end);
            }
        } else {
            if (endInclusive) {
                return Range.openClosed(start, end);
            } else {
                return Range.open(start, end);
            }
        }
    }

    public String getCaseUniqueKey(String studyId, String caseId) {
        return studyId + caseId;
    }

    public Map<String, List<MolecularProfile>> categorizeMolecularPorfiles(List<MolecularProfile> molecularProfiles) {
        return molecularProfiles.stream().collect(Collectors.groupingBy(molecularProfile -> {
            return molecularProfile.getStableId().replace(molecularProfile.getCancerStudyIdentifier() + "_", "");
        }));
    }
}
