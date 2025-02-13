package org.cbioportal.generic_assay.usecase;

import org.cbioportal.generic_assay.repository.GenericAssayRepository;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.web.parameter.GenericAssayDataBinFilter;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetGenericAssayDataBinCounts {
    private final GenericAssayRepository genericAssayRepository;
    public GetGenericAssayDataBinCounts(GenericAssayRepository genericAssayRepository) {
        this.genericAssayRepository = genericAssayRepository;
    }

    public List<ClinicalDataCount> execute(StudyViewFilterContext studyViewFilterContext,
                                           List<GenericAssayDataBinFilter> genericAssayDataBinFilters){
        return genericAssayRepository.getGenericAssayDataBinCounts(studyViewFilterContext, genericAssayDataBinFilters);
    }
}
