package org.cbioportal.domain.genomic_data.usecase;

import org.cbioportal.domain.genomic_data.repository.GenomicDataRepository;
import org.cbioportal.legacy.model.GenomicDataCount;
import org.cbioportal.legacy.service.util.StudyViewColumnarServiceUtil;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
/**
 * Use case for retrieving molecular profile sample counts from the repository.
 * This class encapsulates the business logic for fetching molecular profile
 * sample counts and merging them into a final result.
 */
public class GetMolecularProfileSampleCountsUseCase {

    private final GenomicDataRepository genomicDataRepository;

    /**
     * Constructs a {@code GetMolecularProfileSampleCountsUseCase} with the provided repository.
     *
     * @param genomicDataRepository the repository to be used for fetching molecular profile sample counts
     */
    public GetMolecularProfileSampleCountsUseCase(GenomicDataRepository genomicDataRepository) {
        this.genomicDataRepository = genomicDataRepository;
    }

    /**
     * Executes the use case to retrieve and merge molecular profile sample counts.
     *
     * @param studyViewFilterContext the context of the study view filter to apply
     * @return a list of {@link GenomicDataCount} representing the molecular profile sample counts
     */
    public List<GenomicDataCount> execute(StudyViewFilterContext studyViewFilterContext) {
        return StudyViewColumnarServiceUtil.mergeGenomicDataCounts(genomicDataRepository.getMolecularProfileSampleCounts(studyViewFilterContext));
    }
}
