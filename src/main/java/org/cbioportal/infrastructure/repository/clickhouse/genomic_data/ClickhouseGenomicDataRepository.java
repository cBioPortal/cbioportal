package org.cbioportal.infrastructure.repository.clickhouse.genomic_data;

import org.cbioportal.genomic_data.repository.GenomicDataRepository;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenomicDataCount;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.web.parameter.GenomicDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Profile("clickhouse")
public class ClickhouseGenomicDataRepository implements GenomicDataRepository {

    private final ClickhouseGenomicDataMapper mapper;

    public ClickhouseGenomicDataRepository(ClickhouseGenomicDataMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * @param studyViewFilterContext
     * @return
     */
    @Override
    public List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getMolecularProfileSampleCounts(studyViewFilterContext);
    }

    /**
     * @param studyViewFilterContext
     * @param genomicDataBinFilters
     * @return
     */
    @Override
    public List<ClinicalDataCount> getGenomicDataBinCounts(StudyViewFilterContext studyViewFilterContext, List<GenomicDataBinFilter> genomicDataBinFilters) {
        return mapper.getGenomicDataBinCounts(studyViewFilterContext, genomicDataBinFilters);
    }

    /**
     * @param studyViewFilterContext
     * @param genomicDataFilters
     * @return
     */
    @Override
    public List<GenomicDataCountItem> getCNACounts(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters) {
        return mapper.getCNACounts(studyViewFilterContext, genomicDataFilters);
    }

    /**
     * @param studyViewFilterContext
     * @param genomicDataFilter
     * @return
     */
    @Override
    public Map<String, Integer> getMutationCounts(StudyViewFilterContext studyViewFilterContext,
                                                  GenomicDataFilter genomicDataFilter) {
        return mapper.getMutationCounts(studyViewFilterContext, genomicDataFilter);
    }

    /**
     * @param studyViewFilterContext
     * @param genomicDataFilters
     * @return
     */
    @Override
    public List<GenomicDataCountItem> getMutationCountsByType(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters) {
        return mapper.getMutationCountsByType(studyViewFilterContext, genomicDataFilters);
    }
}
