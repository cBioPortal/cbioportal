package org.cbioportal.web.columnar.util;


import org.cbioportal.web.parameter.ClinicalDataFilter;

import java.util.List;

public class NewStudyViewFilterUtil {

    public static void removeClinicalDataFilter(String attributeId, List<ClinicalDataFilter> dataFilterList ) {
        if (dataFilterList != null) {
            dataFilterList.removeIf(f -> f.getAttributeId().equals(attributeId));
        }
    }
}
