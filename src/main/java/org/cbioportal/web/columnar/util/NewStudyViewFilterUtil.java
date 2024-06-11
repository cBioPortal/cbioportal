package org.cbioportal.web.columnar.util;


import org.cbioportal.web.parameter.StudyViewFilter;

public class NewStudyViewFilterUtil {

    public static void removeSelfFromFilter(String attributeId, StudyViewFilter studyViewFilter) {
        if (studyViewFilter!= null && studyViewFilter.getClinicalDataFilters() != null) {
            studyViewFilter.getClinicalDataFilters().removeIf(f -> f.getAttributeId().equals(attributeId));
        }
    }

    public static void removeSelfCustomDataFromFilter(String attributeId, StudyViewFilter studyViewFilter) {
        if (studyViewFilter != null && studyViewFilter.getCustomDataFilters() != null) {
            studyViewFilter.getCustomDataFilters().removeIf(f -> f.getAttributeId().equals(attributeId));
        }
    }
}
