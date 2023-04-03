package org.cbioportal.persistence;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.Sample;
import org.cbioportal.webparam.StudyViewFilter;

import java.util.List;

public interface StudyViewRepository {
    List<Sample> getFilteredSamplesFromColumnstore(StudyViewFilter studyViewFilter);

    List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter);
}
