package org.cbioportal.domain.cancerstudy.usecase;

import org.cbioportal.domain.cancerstudy.CancerStudyMetadata;
import org.cbioportal.domain.cancerstudy.repository.CancerStudyRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class responsible for retrieving cancer study metadata by id.
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
     * Executes the use case to retrieve cancer study metadata by id.
     */
    public Optional<CancerStudyMetadata> execute(String cancerStudyId) {
        return this.studyRepository.getCancerStudyMetadata(cancerStudyId);
    }
}
