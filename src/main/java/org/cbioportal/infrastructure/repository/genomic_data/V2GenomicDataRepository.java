package org.cbioportal.infrastructure.repository.genomic_data;

import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenomicDataCount;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.web.parameter.GenomicDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class V2GenomicDataRepository implements org.cbioportal.domain.genomic_data.repository.GenomicDataRepository {

    private final V2GenomicDataMapper mapper;

    public V2GenomicDataRepository(V2GenomicDataMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getMolecularProfileSampleCounts(studyViewFilterContext);
    }

    @Override
    public List<ClinicalDataCount> getGenomicDataBinCounts(StudyViewFilterContext studyViewFilterContext, List<GenomicDataBinFilter> genomicDataBinFilters) {
        return mapper.getGenomicDataBinCounts(studyViewFilterContext, genomicDataBinFilters);
    }

    @Override
    public List<GenomicDataCountItem> getCNACounts(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters) {
        return mapper.getCNACounts(studyViewFilterContext, genomicDataFilters);
    }

    @Override
    public Map<String, Integer> getMutationCounts(StudyViewFilterContext studyViewFilterContext,
                                                  GenomicDataFilter genomicDataFilter) {
        return mapper.getMutationCounts(studyViewFilterContext, genomicDataFilter);
    }

    @Override
    public List<GenomicDataCountItem> getMutationCountsByType(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters) {
        return mapper.getMutationCountsByType(studyViewFilterContext, genomicDataFilters);
    }
}
