package org.cbioportal.persistence.mybatiscolumnstore;

import org.cbioportal.model.Sample;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.webparam.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StudyViewMyBatisRepository implements StudyViewRepository {

    @Autowired
    private StudyViewMapper studyViewMapper;
    
    @Override
    public List<Sample> getFilteredSamplesFromColumnstore(StudyViewFilter studyViewFilter) {
        studyViewMapper.filteredSamples(studyViewFilter);
    }
}