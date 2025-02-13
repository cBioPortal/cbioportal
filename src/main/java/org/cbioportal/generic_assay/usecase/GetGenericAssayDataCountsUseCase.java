package org.cbioportal.generic_assay.usecase;

import org.cbioportal.generic_assay.repository.GenericAssayRepository;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetGenericAssayDataCountsUseCase {
    private final GenericAssayRepository genericAssayRepository;

    public GetGenericAssayDataCountsUseCase(GenericAssayRepository genericAssayRepository) {
        this.genericAssayRepository = genericAssayRepository;
    }

    public List<GenericAssayDataCountItem> execute(StudyViewFilterContext studyViewFilterContext,
                                                   List<GenericAssayDataFilter> genericAssayDataFilters){
        return genericAssayRepository.getGenericAssayDataCounts(studyViewFilterContext, genericAssayDataFilters);
    }
}
