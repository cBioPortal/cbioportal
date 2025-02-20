package org.cbioportal.legacy.web.columnar.util;


import org.cbioportal.legacy.web.parameter.ClinicalDataFilter;
import org.cbioportal.legacy.web.parameter.MutationOption;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;

import java.util.List;
@Deprecated(forRemoval = true)
public abstract class NewStudyViewFilterUtil {

    private NewStudyViewFilterUtil() {}

    public static void removeClinicalDataFilter(String attributeId, List<ClinicalDataFilter> dataFilterList ) {
        if (dataFilterList != null) {
            dataFilterList.removeIf(f -> f.getAttributeId().equals(attributeId));
        }
    }

    public static void removeSelfFromGenomicDataFilter(String hugoGeneSymbol, String profileType,
                                                 StudyViewFilter studyViewFilter) {
        if (studyViewFilter != null && studyViewFilter.getGenomicDataFilters() != null) {
            studyViewFilter.getGenomicDataFilters().removeIf(f ->
                    f.getHugoGeneSymbol().equals(hugoGeneSymbol) && f.getProfileType().equals(profileType)
            );
        }
    }

    public static void removeSelfFromGenericAssayFilter(String stableId, StudyViewFilter studyViewFilter) {
        if (studyViewFilter != null && studyViewFilter.getGenericAssayDataFilters() != null) {
            studyViewFilter.getGenericAssayDataFilters().removeIf(f -> f.getStableId().equals(stableId));
        }
    }

    public static void removeSelfFromMutationDataFilter(String hugoGeneSymbol, String profileType,
                                                  MutationOption categorization, StudyViewFilter studyViewFilter) {
        if (studyViewFilter != null && studyViewFilter.getMutationDataFilters() != null) {
            studyViewFilter.getMutationDataFilters().removeIf(f ->
                    f.getHugoGeneSymbol().equals(hugoGeneSymbol) &&
                            f.getProfileType().equals(profileType) &&
                            f.getCategorization().equals(categorization)
            );
        }
    }
    public static SampleIdentifier buildSampleIdentifier(String studyId, String sampleId) {
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setStudyId(studyId);
        sampleIdentifier.setSampleId(sampleId);
        return sampleIdentifier;
    }
}
