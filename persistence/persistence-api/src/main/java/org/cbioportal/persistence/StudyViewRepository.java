package org.cbioportal.persistence;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.persistence.enums.ClinicalAttributeDataType;
import org.cbioportal.webparam.CategorizedClinicalDataCountFilter;
import org.cbioportal.webparam.StudyViewFilter;

import java.util.List;

public interface StudyViewRepository {
    List<Sample> getFilteredSamplesFromColumnstore(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter);

    List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter);

    List<String> getClinicalDataAttributeNames(ClinicalAttributeDataSource clinicalAttributeDataSource, ClinicalAttributeDataType dataType);

}
