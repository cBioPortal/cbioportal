package org.cbioportal.persistence.mybatis;

import java.util.List;

import org.cbioportal.model.ResourceData;
import org.cbioportal.persistence.ResourceDataRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceDataMyBatisRepository implements ResourceDataRepository {

    @Autowired
    private ResourceDataMapper resourceDataMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<ResourceData> getAllResourceDataOfSampleInStudy(String studyId, String sampleId, String resourceId,
            String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {

        return resourceDataMapper.getResourceDataOfSampleInStudy(studyId, sampleId, resourceId, projection, pageSize,
                offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public List<ResourceData> getAllResourceDataOfPatientInStudy(String studyId, String patientId, String resourceId,
            String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {

        return resourceDataMapper.getResourceDataOfPatientInStudy(studyId, patientId, resourceId, projection, pageSize,
                offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public List<ResourceData> getAllResourceDataForStudy(String studyId, String resourceId, String projection,
            Integer pageSize, Integer pageNumber, String sortBy, String direction) {

        return resourceDataMapper.getResourceDataForStudy(studyId, resourceId, projection, pageSize,
                offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

}
