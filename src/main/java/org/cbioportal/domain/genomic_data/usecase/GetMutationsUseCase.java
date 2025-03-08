package org.cbioportal.domain.genomic_data.usecase;

import org.cbioportal.domain.genomic_data.repository.GenomicDataRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/**
 * A use case class responsible for retrieving mutations based on the provided study view filter context
 * and genomic data filter. This class acts as an intermediary between the application logic and the data repository,
 * delegating the data retrieval to the {@link GenomicDataRepository}.
 */
public class GetMutationsUseCase {
    private final GenomicDataRepository repository;

    /**
     * Constructs a new instance of {@link GetMutationsUseCase}.
     *
     * @param repository the repository used to access genomic data.
     *                   Must not be {@code null}.
     */
    public GetMutationsUseCase(GenomicDataRepository repository) {
        this.repository = repository;
    }

    /**
     * Executes the use case to retrieve mutations based on the provided study view filter context
     * and genomic data filter.
     *
     * @param studyViewFilterContext the context containing study view filter criteria.
     *                               Must not be {@code null}.
     * @return a map where the key is a string representing a mutation type and the value is the count of mutations.
     */
    public List<Mutation> execute(StudyViewFilterContext studyViewFilterContext) {
        return repository.getMutations(studyViewFilterContext);
    }
}
