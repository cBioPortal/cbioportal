package org.cbioportal.genomic_data.usecase;

import org.cbioportal.genomic_data.repository.GenomicDataRepository;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetCNACountsByGeneSpecificUseCase {

    private final GenomicDataRepository repository;

    public GetCNACountsByGeneSpecificUseCase(GenomicDataRepository repository) {
        this.repository = repository;
    }

    public List<GenomicDataCountItem> execute(StudyViewFilterContext studyViewFilterContext,
                                              List<GenomicDataFilter> genomicDataFilters){
        return repository.getCNACounts(studyViewFilterContext, genomicDataFilters);
    }
}
