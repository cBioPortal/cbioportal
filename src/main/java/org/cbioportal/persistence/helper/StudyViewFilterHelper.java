package org.cbioportal.persistence.helper;

import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.CustomSampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StudyViewFilterHelper {
    public static StudyViewFilterHelper build(@Nullable StudyViewFilter studyViewFilter,
            @Nullable List<CustomSampleIdentifier> customDataSamples) {
        if (Objects.isNull(studyViewFilter)) {
            studyViewFilter = new StudyViewFilter();
        }
        if (Objects.isNull(customDataSamples)) {
            customDataSamples = new ArrayList<>();
        }
        return new StudyViewFilterHelper(studyViewFilter, customDataSamples);
    }

    private final StudyViewFilter studyViewFilter;
    private final List<CustomSampleIdentifier> customDataSamples;

    private StudyViewFilterHelper(@NonNull StudyViewFilter studyViewFilter,
            @NonNull List<CustomSampleIdentifier> customDataSamples) {
        this.studyViewFilter = studyViewFilter;
       
        this.customDataSamples = customDataSamples;
    }

    public StudyViewFilter studyViewFilter() {
        return studyViewFilter;
    }

    public List<CustomSampleIdentifier> customDataSamples() {
        return this.customDataSamples;
    }

    public boolean isCategoricalClinicalDataFilter(ClinicalDataFilter clinicalDataFilter) {
        var filterValue = clinicalDataFilter.getValues().getFirst();
        return filterValue.getValue() != null;
    }

}
