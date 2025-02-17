package org.cbioportal.sample.repository;

import org.cbioportal.sample.Sample;
import org.cbioportal.studyview.StudyViewFilterContext;

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
}

