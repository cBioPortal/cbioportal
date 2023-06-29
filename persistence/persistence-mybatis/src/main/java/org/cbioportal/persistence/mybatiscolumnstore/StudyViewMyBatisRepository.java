package org.cbioportal.persistence.mybatiscolumnstore;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.persistence.enums.ClinicalAttributeDataType;
import org.cbioportal.webparam.CategorizedClinicalDataCountFilter;
import org.cbioportal.webparam.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StudyViewMyBatisRepository implements StudyViewRepository {

    @Autowired
    private StudyViewMapper studyViewMapper;
    
    @Override
    public List<Sample> getFilteredSamplesFromColumnstore(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        return studyViewMapper.getFilteredSamples(studyViewFilter, categorizedClinicalDataCountFilter);
    }
    
    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        return studyViewMapper.getMutatedGenes(studyViewFilter, categorizedClinicalDataCountFilter);
    }

    @Override
    public List<String> getClinicalDataAttributeNames(ClinicalAttributeDataSource clinicalAttributeDataSource, ClinicalAttributeDataType dataType) {
        String tableName = clinicalAttributeDataSource.getValue().toLowerCase() + "_clinical_attribute_" + dataType.getValue().toLowerCase();
        return studyViewMapper.getClinicalAttributeNames(tableName);
    }


}