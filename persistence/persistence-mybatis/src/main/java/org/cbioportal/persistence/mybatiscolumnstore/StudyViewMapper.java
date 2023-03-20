package org.cbioportal.persistence.mybatiscolumnstore;

import org.cbioportal.model.Sample;
import org.cbioportal.webparam.StudyViewFilter;

import java.util.List;

public interface StudyViewMapper {
    List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter);
}
