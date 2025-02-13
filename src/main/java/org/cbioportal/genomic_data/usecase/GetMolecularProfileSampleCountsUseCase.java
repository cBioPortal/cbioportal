package org.cbioportal.genomic_data.usecase;

import org.cbioportal.genomic_data.repository.GenomicDataRepository;
import org.cbioportal.legacy.model.GenomicDataCount;
import org.cbioportal.legacy.service.util.StudyViewColumnarServiceUtil;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetMolecularProfileSampleCountsUseCase {
    private final GenomicDataRepository genomicDataRepository;

    public GetMolecularProfileSampleCountsUseCase(GenomicDataRepository genomicDataRepository) {
        this.genomicDataRepository = genomicDataRepository;
    }

    public List<GenomicDataCount> execute(StudyViewFilterContext studyViewFilterContext){
        return StudyViewColumnarServiceUtil.mergeGenomicDataCounts(genomicDataRepository.getMolecularProfileSampleCounts(studyViewFilterContext));
    }
}
