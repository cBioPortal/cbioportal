package org.cbioportal.genomic_data.usecase;

import org.cbioportal.genomic_data.repository.GenomicDataRepository;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Profile("clickhouse")
public class GetMutationCountsUseCase {
    private final GenomicDataRepository repository;


    public GetMutationCountsUseCase(GenomicDataRepository repository) {
        this.repository = repository;
    }

    public Map<String, Integer> execute(StudyViewFilterContext studyViewFilterContext,
                                        GenomicDataFilter genomicDataFilter){
        return repository.getMutationCounts(studyViewFilterContext, genomicDataFilter);
    }
}
