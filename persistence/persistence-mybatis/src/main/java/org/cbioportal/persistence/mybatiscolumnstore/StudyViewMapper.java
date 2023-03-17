package org.cbioportal.persistence.mybatiscolumnstore;

import org.cbioportal.webparam.StudyViewFilter;

public interface StudyViewMapper {
    void filteredSamples(StudyViewFilter studyViewFilter);
}
