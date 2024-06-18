package org.cbioportal.service;

import org.cbioportal.model.*;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.util.List;

public interface StudyViewColumnarService {

    List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter);

    List<AlterationCountByGene> getMutatedGenes(StudyViewFilter interceptedStudyViewFilter);

    List<ClinicalDataCountItem> getClinicalDataCounts(StudyViewFilter studyViewFilter, List<String> filteredAttributes);

    List<ClinicalData> getPatientClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds);

    List<ClinicalData> getSampleClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds);

    List<GenomicDataCount> getGenomicDataCounts(StudyViewFilter studyViewFilter);


}
