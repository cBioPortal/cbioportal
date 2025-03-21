package org.cbioportal.domain.sample.repository;

import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.shared.enums.ProjectionType;

import java.util.List;

/**
 * Repository interface for performing operations related to sample data.
 * This interface defines methods for retrieving filtered samples and their respective counts
 * based on the filter criteria provided in the study view filter context.
 */
public interface SampleRepository {

    /**
     * Retrieves the samples that match the filter criteria specified in the study view filter context.
     *
     * @param studyViewFilterContext the context containing the filter criteria for the study view
     * @return a list of {@link Sample} representing the samples that match the filter criteria
     */
    List<Sample> getFilteredSamples(StudyViewFilterContext studyViewFilterContext);

    /**
     * Retrieves the total count of samples that match the filter criteria specified in the study view filter context.
     *
     * @param studyViewFilterContext the context containing the filter criteria for the study view
     * @return the total count of filtered samples
     */
    int getFilteredSamplesCount(StudyViewFilterContext studyViewFilterContext);

    List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, ProjectionType projection);

    List<Sample> fetchSamplesBySampleListIds(List<String> sampleListIds, ProjectionType projection);

    BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds);

    BaseMeta fetchMetaSamplesBySampleListIds(List<String> sampleListIds);

    List<Sample> getAllSamplesInStudy(
        String studyId,
        ProjectionType projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    );

    BaseMeta getMetaSamplesInStudy(String studyId);

    Sample getSampleInStudy(
        String studyId,
        String sampleId
    );

    List<Sample> getAllSamplesOfPatientInStudy(
        String studyId,
        String patientId,
        ProjectionType projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    );

    BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId);

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
}

