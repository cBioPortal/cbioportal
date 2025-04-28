package org.cbioportal.infrastructure.repository.clickhouse.cancerstudy;

import org.cbioportal.domain.cancerstudy.CancerStudyMetadata;
import org.cbioportal.domain.cancerstudy.repository.CancerStudyRepository;
import org.cbioportal.shared.SortAndSearchCriteria;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository implementation for accessing cancer study metadata from ClickHouse.
 * This class delegates database queries to {@link ClickhouseCancerStudyMapper}.
 */
@Repository
@Profile("clickhouse")
public class ClickhouseCancerStudyRepository implements CancerStudyRepository {

    private final ClickhouseCancerStudyMapper cancerStudyMapper;

    /**
     * Constructs a new {@code ClickhouseCancerStudyRepository} with the required mapper.
     *
     * @param cancerStudyMapper the mapper responsible for executing ClickHouse queries
     */
    public ClickhouseCancerStudyRepository(ClickhouseCancerStudyMapper cancerStudyMapper) {
        this.cancerStudyMapper = cancerStudyMapper;
    }

    /**
     * Retrieves detailed metadata for all cancer studies.
     *
     * @param sortAndSearchCriteria the criteria used for sorting and searching the cancer study metadata.
     *                              This includes parameters such as sort direction, sort by field, and search keywords.
     * @return a list of {@link CancerStudyMetadata} containing detailed metadata for each study
     */
    @Override
    public List<CancerStudyMetadata> getCancerStudiesMetadata(SortAndSearchCriteria sortAndSearchCriteria) {
        return cancerStudyMapper.getCancerStudiesMetadata(sortAndSearchCriteria, List.of());
    }

    /**
     * Retrieves a summarized version of cancer study metadata.
     *
     * @param sortAndSearchCriteria the criteria used for sorting and searching the cancer study metadata.
     *                              This includes parameters such as sort direction, sort by field, and search keywords.
     * @return a list of {@link CancerStudyMetadata} containing summarized metadata for each study
     */
    @Override
    public List<CancerStudyMetadata> getCancerStudiesMetadataSummary(SortAndSearchCriteria sortAndSearchCriteria) {
        return cancerStudyMapper.getCancerStudiesMetadataSummary(sortAndSearchCriteria, List.of());
    }

    /**
     * @param studyViewFilterContext
     * @return
     */
    @Override
    public List<String> getFilteredStudyIds(StudyViewFilterContext studyViewFilterContext) {
        return cancerStudyMapper.getFilteredStudyIds(studyViewFilterContext);
    }
}
