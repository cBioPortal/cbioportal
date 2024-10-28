package org.cbioportal.persistence.mybatis;

import java.util.List;

import org.cbioportal.model.ResourceData;
import org.cbioportal.persistence.ResourceDataRepository;
import org.cbioportal.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceDataMyBatisRepository implements ResourceDataRepository {

    @Autowired
    private ResourceDataMapper resourceDataMapper;

    @Override
    public List<ResourceData> getAllResourceDataOfSampleInStudy(String studyId, String sampleId, String resourceId,
            String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {

        return resourceDataMapper.getResourceDataOfSampleInStudy(studyId, sampleId, resourceId, projection, pageSize,
                PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public List<ResourceData> getAllResourceDataOfPatientInStudy(String studyId, String patientId, String resourceId,
            String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {

        return resourceDataMapper.getResourceDataOfPatientInStudy(studyId, patientId, resourceId, projection, pageSize,
                PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public List<ResourceData> getAllResourceDataForStudy(String studyId, String resourceId, String projection,
            Integer pageSize, Integer pageNumber, String sortBy, String direction) {

        return resourceDataMapper.getResourceDataForStudy(studyId, resourceId, projection, pageSize,
                PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public List<ResourceData> getResourceDataForAllPatientsInStudy(String studyId, String resourceId, String projection,
            Integer pageSize, Integer pageNumber, String sortBy, String direction) {

        return resourceDataMapper.getResourceDataForAllPatientsInStudy(studyId, resourceId, projection, pageSize,
                PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }
    
    @Override
    public List<ResourceData> getResourceDataForAllSamplesInStudy(String studyId, String resourceId, String projection,
            Integer pageSize, Integer pageNumber, String sortBy, String direction) {

        return resourceDataMapper.getResourceDataForAllSamplesInStudy(studyId, resourceId, projection, pageSize,
                PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }

}
