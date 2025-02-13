package org.cbioportal.genomic_data.usecase;

import org.cbioportal.genomic_data.repository.GenomicDataRepository;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.web.parameter.GenomicDataBinFilter;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetGenomicDataBinCountsUseCase {
    private final GenomicDataRepository genomicDataRepository;

    public GetGenomicDataBinCountsUseCase(GenomicDataRepository genomicDataRepository) {
        this.genomicDataRepository = genomicDataRepository;
    }

    public List<ClinicalDataCount> execute(StudyViewFilterContext studyViewFilterContext,
                                           List<GenomicDataBinFilter> genomicDataFilters){
        return genomicDataRepository.getGenomicDataBinCounts(studyViewFilterContext, genomicDataFilters);
    }
}
