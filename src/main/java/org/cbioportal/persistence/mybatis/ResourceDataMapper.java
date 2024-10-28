package org.cbioportal.persistence.mybatis;

import java.util.List;

import org.cbioportal.model.ResourceData;

public interface ResourceDataMapper {

    List<ResourceData> getResourceDataOfSampleInStudy(String studyId, String sampleId, String resourceId,
            String projection, Integer limit, Integer offset, String sortBy, String direction);

    List<ResourceData> getResourceDataOfPatientInStudy(String studyId, String patientId, String resourceId,
            String projection, Integer limit, Integer offset, String sortBy, String direction);

    List<ResourceData> getResourceDataForStudy(String studyId, String resourceId, String projection, Integer limit,
            Integer offset, String sortBy, String direction);

    List<ResourceData> getResourceDataForAllPatientsInStudy(String studyId, String resourceId, String projection, Integer limit,
            Integer offset, String sortBy, String direction);

    List<ResourceData> getResourceDataForAllSamplesInStudy(String studyId, String resourceId, String projection, Integer limit,
            Integer offset, String sortBy, String direction);

}
