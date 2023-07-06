package org.cbioportal.web.columnstore.util;

import org.cbioportal.webparam.StudyViewFilter;

public class NewStudyViewFilterUtil {

    public static void removeSelfFromFilter(String attributeId, StudyViewFilter studyViewFilter) {
        if (studyViewFilter!= null && studyViewFilter.getClinicalDataFilters() != null) {
            studyViewFilter.getClinicalDataFilters().removeIf(f -> f.getAttributeId().equals(attributeId));
        }
    }
}
