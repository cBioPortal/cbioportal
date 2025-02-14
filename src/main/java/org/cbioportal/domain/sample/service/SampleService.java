package org.cbioportal.domain.sample.service;

import org.cbioportal.domain.sample.Sample;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.SampleNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.http.HttpHeaders;

import java.util.List;

public interface SampleService {
    BaseMeta fetchMetaSamples(SampleFilter sampleFilter);
    HttpHeaders fetchMetaSamplesHeaders(SampleFilter sampleFilter);
    
    List<Sample> fetchSamples(SampleFilter sampleFilter, ProjectionType projection);

    HttpHeaders getMetaSamplesInStudyHeaders(String studyId) throws StudyNotFoundException;
    BaseMeta getMetaSamplesInStudy(String studyId) throws StudyNotFoundException;

    List<Sample> getAllSamplesInStudy(
        String studyId,
        ProjectionType projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    ) throws StudyNotFoundException;

    Sample getSampleInStudy(
        String studyId,
        String sampleId
    ) throws SampleNotFoundException, StudyNotFoundException;

    BaseMeta getMetaSamplesOfPatientInStudy(
        String studyId,
        String patientId
    ) throws StudyNotFoundException, PatientNotFoundException;

    HttpHeaders getMetaSamplesOfPatientInStudyHeaders(
        String studyId,
        String patientId
    ) throws StudyNotFoundException, PatientNotFoundException;

    List<Sample> getAllSamplesOfPatientInStudy(
        String studyId,
        String patientId,
        ProjectionType projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    ) throws StudyNotFoundException, PatientNotFoundException;

    List<Sample> getAllSamples(
        String keyword,
        List<String> studyIds,
        ProjectionType projection,
        Integer pageSize,
        Integer pageNumber,
        String sort,
        String direction
    );

    BaseMeta getMetaSamples(String keyword, List<String> studyIds);

    HttpHeaders getMetaSamplesHeaders(String keyword, List<String> studyIds);
}
