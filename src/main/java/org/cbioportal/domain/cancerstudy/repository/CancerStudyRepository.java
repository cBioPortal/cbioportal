package org.cbioportal.domain.cancerstudy.repository;

import org.cbioportal.domain.cancerstudy.CancerStudyMetadata;
import org.cbioportal.shared.SortAndSearchCriteria;
import org.cbioportal.domain.studyview.StudyViewFilterContext;

import java.util.List;

/**
 * Repository interface for accessing and managing cancer study data.
 * <p>
 * This repository provides methods to retrieve metadata and summary information
 * about cancer studies. It serves as an abstraction layer between the domain
 * logic and the data source, allowing for flexible and maintainable data access.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * {@code
 * // Inject the repository into a service
 * private final CancerStudyRepository cancerStudyRepository;
 *
 * public CancerStudyService(CancerStudyRepository cancerStudyRepository) {
 *     this.cancerStudyRepository = cancerStudyRepository;
 * }
 *
 * // Retrieve metadata for all cancer studies
 * List<CancerStudyMetadata> metadata = cancerStudyRepository.getCancerStudiesMetadata();
 *
 * // Retrieve summary metadata for cancer studies
 * List<CancerStudyMetadata> summaryMetadata = cancerStudyRepository.getCancerStudiesMetadataSummary();
 * }
 * </pre>
 * </p>
 *
 * @see CancerStudyMetadata
 */
public interface CancerStudyRepository {

    /**
     * Retrieves a list of metadata for all cancer studies.
     * <p>
     * This method returns detailed metadata for all available cancer studies,
     * including information such as study identifiers, descriptions, and
     * associated data sources. The metadata can be used for comprehensive
     * analysis or display purposes.
     * </p>
     * <p>
     * <b>Note:</b> The returned list may be large, depending on the number of
     * cancer studies in the database. Consider using pagination or filtering
     * if performance is a concern.
     * </p>
     *
     * @return a list of {@link CancerStudyMetadata} objects containing detailed
     *         metadata for all cancer studies. The list may be empty if no
     *         studies are found.
     */
    List<CancerStudyMetadata> getCancerStudiesMetadata(SortAndSearchCriteria sortAndSearchCriteria);

    /**
     * Retrieves a list of summary metadata for cancer studies.
     * <p>
     * This method returns a lightweight representation of cancer study metadata,
     * containing only the most essential fields. It is suitable for scenarios
     * where a high-level overview of the studies is sufficient, such as displaying
     * a list of studies in a UI or performing quick lookups.
     * </p>
     * <p>
     * <b>Note:</b> The summary metadata typically excludes detailed information
     * to reduce the size of the response and improve performance.
     * </p>
     *
     * @return a list of {@link CancerStudyMetadata} objects containing summary
     *         metadata for cancer studies. The list may be empty if no studies
     *         are found.
     */
    List<CancerStudyMetadata> getCancerStudiesMetadataSummary(SortAndSearchCriteria sortAndSearchCriteria);

    List<String> getFilteredStudyIds(StudyViewFilterContext studyViewFilterContext);
}
