package org.cbioportal.cancerstudy.usecase;

import org.cbioportal.cancerstudy.CancerStudyMetadata;
import org.cbioportal.cancerstudy.repository.CancerStudyRepository;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Service class responsible for retrieving cancer study metadata based on the specified projection type.
 * <p>
 * This use case encapsulates the logic for fetching cancer study metadata from the repository
 * and returning the appropriate data based on the requested level of detail (projection).
 * It acts as an intermediary between the controller and the repository, ensuring that the
 * domain logic is decoupled from the data access layer.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * {@code
 * // Inject the use case into a controller or service
 * private final GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase;
 *
 * public CancerStudyController(GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase) {
 *     this.getCancerStudyMetadataUseCase = getCancerStudyMetadataUseCase;
 * }
 *
 * // Retrieve detailed metadata for cancer studies
 * List<CancerStudyMetadata> detailedMetadata = getCancerStudyMetadataUseCase.execute(ProjectionType.DETAILED);
 *
 * // Retrieve summary metadata for cancer studies
 * List<CancerStudyMetadata> summaryMetadata = getCancerStudyMetadataUseCase.execute(ProjectionType.SUMMARY);
 * }
 * </pre>
 * </p>
 *
 * @see CancerStudyRepository
 * @see ProjectionType
 * @see CancerStudyMetadata
 */
@Service
public final class GetCancerStudyMetadataUseCase {
    private final CancerStudyRepository studyRepository;


    /**
     * Constructs a new {@link GetCancerStudyMetadataUseCase} with the specified repository.
     *
     * @param studyRepository the repository used to access cancer study metadata.
     */
    public GetCancerStudyMetadataUseCase(CancerStudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    /**
     * Executes the use case to retrieve cancer study metadata based on the specified projection type.
     * <p>
     * This method determines the level of detail to fetch from the repository based on the
     * provided {@link ProjectionType}. It supports the following projections:
     * <ul>
     *     <li>{@link ProjectionType#DETAILED}: Fetches all available metadata for cancer studies.</li>
     *     <li>{@link ProjectionType#SUMMARY}: Fetches a summarized version of the metadata.</li>
     *     <li>Other projection types: Returns an empty list.</li>
     * </ul>
     * </p>
     *
     * @param projectionType the level of detail to fetch. Determines which repository method is called.
     * @return a list of {@link CancerStudyMetadata} objects based on the specified projection type.
     *         Returns an empty list if the projection type is not supported.
     *
     * @see ProjectionType
     * @see CancerStudyMetadata
     */
    public List<CancerStudyMetadata> execute(ProjectionType projectionType) {
        return switch (projectionType) {
            case DETAILED -> studyRepository.getCancerStudiesMetadata();
            case SUMMARY -> studyRepository.getCancerStudiesMetadataSummary();
            default -> Collections.emptyList();
        };
    }
}
