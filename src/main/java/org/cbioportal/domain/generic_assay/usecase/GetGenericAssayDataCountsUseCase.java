package org.cbioportal.domain.generic_assay.usecase;

import org.cbioportal.domain.generic_assay.repository.GenericAssayRepository;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
/**
 * A use case class responsible for retrieving counts of generic assay data based on the provided filters.
 * This class acts as an intermediary between the application logic and the data repository,
 * delegating the data retrieval to the {@link GenericAssayRepository}.
 */
public class GetGenericAssayDataCountsUseCase {
    private final GenericAssayRepository genericAssayRepository;

    /**
     * Constructs a new instance of {@link GetGenericAssayDataCountsUseCase}.
     *
     * @param genericAssayRepository the repository used to access generic assay data.
     *                               Must not be {@code null}.
     */
    public GetGenericAssayDataCountsUseCase(GenericAssayRepository genericAssayRepository) {
        this.genericAssayRepository = genericAssayRepository;
    }

    /**
     * Executes the use case to retrieve counts of generic assay data based on the provided filters.
     *
     * @param studyViewFilterContext  the context containing study view filter criteria.
     *                                Must not be {@code null}.
     * @param genericAssayDataFilters a list of filters to apply to the generic assay data.
     *                                Must not be {@code null}.
     * @return a list of {@link GenericAssayDataCountItem} objects representing the counts of generic assay data
     * that match the provided filters. The list may be empty if no data matches the filters.
     */
    public List<GenericAssayDataCountItem> execute(StudyViewFilterContext studyViewFilterContext,
                                                   List<GenericAssayDataFilter> genericAssayDataFilters) {
        return genericAssayRepository.getGenericAssayDataCounts(studyViewFilterContext, genericAssayDataFilters);
    }
}
