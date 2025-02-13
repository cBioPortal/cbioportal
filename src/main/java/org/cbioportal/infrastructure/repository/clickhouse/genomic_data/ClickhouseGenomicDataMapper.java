package org.cbioportal.infrastructure.repository.clickhouse.genomic_data;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenomicDataCount;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.web.parameter.GenomicDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;
import java.util.Map;

public interface ClickhouseGenomicDataMapper {
    List<GenomicDataCount> getMolecularProfileSampleCounts(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);
    List<ClinicalDataCount> getGenomicDataBinCounts(StudyViewFilterContext studyViewFilterContext, List<GenomicDataBinFilter> genomicDataBinFilters);
    List<GenomicDataCountItem> getCNACounts(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters);
    Map<String, Integer> getMutationCounts(StudyViewFilterContext studyViewFilterContext,
                                           GenomicDataFilter genomicDataFilter);
    List<GenomicDataCountItem> getMutationCountsByType(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters);

}
