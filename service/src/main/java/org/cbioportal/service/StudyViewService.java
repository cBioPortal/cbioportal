package org.cbioportal.service;

import org.cbioportal.model.GenomicDataCount;

import java.util.List;

public interface StudyViewService {
    List<GenomicDataCount> getGenomicDataCounts(List<String> studyIds, List<String> sampleIds);
}
