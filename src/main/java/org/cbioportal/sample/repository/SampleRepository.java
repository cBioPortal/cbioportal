package org.cbioportal.sample.repository;

import org.cbioportal.sample.Sample;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;

public interface SampleRepository {
    List<Sample> getFilteredSamples(StudyViewFilterContext studyViewFilterContext);
    int getFilteredSamplesCount(StudyViewFilterContext studyViewFilterContext);
}
