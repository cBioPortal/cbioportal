package org.cbioportal.domain.sample.usecase;

import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


@Service
@Profile("clickhouse")
/**
 * Use case for retrieving the count of filtered samples.
 * This class interacts with the {@link SampleRepository} to fetch the number of samples
 * that match the given filter criteria specified in the study view filter context.
 */
public class GetFilteredSamplesCountUseCase {

    private final SampleRepository sampleRepository;

    /**
     * Constructs a {@code GetFilteredSamplesCountUseCase} with the provided {@link SampleRepository}.
     *
     * @param sampleRepository the repository to be used for retrieving the filtered samples count
     */
    public GetFilteredSamplesCountUseCase(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    /**
     * Executes the use case to retrieve the count of filtered samples based on the given study view filter context.
     *
     * @param studyViewFilterContext the context of the study view filter to apply
     * @return the count of filtered samples
     */
    public int execute(StudyViewFilterContext studyViewFilterContext) {
        return sampleRepository.getFilteredSamplesCount(studyViewFilterContext);
    }
}

