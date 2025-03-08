package org.cbioportal.infrastructure.repository.cancerstudy;

import org.cbioportal.domain.cancerstudy.CancerStudyMetadata;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.shared.SortAndSearchCriteria;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository implementation for accessing cancer study metadata from DB.
 * This class delegates database queries to {@link V2CancerStudyMapper}.
 */
@Repository
public class V2CancerStudyRepository implements org.cbioportal.domain.cancerstudy.repository.CancerStudyRepository {
    
    private final V2CancerStudyMapper v2CancerStudyMapper;

    /**
     * Constructs a new {@code CancerStudyRepository} with the required mapper.
     *
     * @param v2CancerStudyMapper the mapper responsible for executing DB queries
     */
    public V2CancerStudyRepository(V2CancerStudyMapper v2CancerStudyMapper) {
        this.v2CancerStudyMapper = v2CancerStudyMapper;
    }

    /**
     * Retrieves detailed metadata for all cancer studies.
     * @param sortAndSearchCriteria the criteria used for sorting and searching the cancer study metadata.
     * This includes parameters such as sort direction, sort by field, and search keywords.
     *
     * @return a list of {@link CancerStudyMetadata} containing detailed metadata for each study
     */
    @Override
    public List<CancerStudyMetadata> getCancerStudyMetadata(SortAndSearchCriteria sortAndSearchCriteria) {
        return v2CancerStudyMapper.getCancerStudiesMetadata(sortAndSearchCriteria, List.of());
    }

    /**
     * Retrieves a summarized version of cancer study metadata.
     *
     * @param sortAndSearchCriteria the criteria used for sorting and searching the cancer study metadata.
     * This includes parameters such as sort direction, sort by field, and search keywords.
     * @return a list of {@link CancerStudyMetadata} containing summarized metadata for each study
     */
    @Override
    public List<CancerStudyMetadata> getCancerStudiesMetadataSummary(SortAndSearchCriteria sortAndSearchCriteria) {
        return v2CancerStudyMapper.getCancerStudiesMetadataSummary(sortAndSearchCriteria, List.of());
    }

    /**
     * @param studyViewFilterContext
     * @return
     */
    @Override
    public List<String> getFilteredStudyIds(StudyViewFilterContext studyViewFilterContext) {
        return v2CancerStudyMapper.getFilteredStudyIds(studyViewFilterContext);
    }

    @Override
    public Optional<CancerStudyMetadata> getCancerStudyMetadata(String cancerStudyId) {
        List<CancerStudyMetadata> cancerStudyMetadataList = v2CancerStudyMapper.getCancerStudiesMetadataSummary(new SortAndSearchCriteria(null, null, null), List.of(cancerStudyId));
        return cancerStudyMetadataList.isEmpty() ? Optional.empty() : Optional.of(cancerStudyMetadataList.getFirst());
    }
}
