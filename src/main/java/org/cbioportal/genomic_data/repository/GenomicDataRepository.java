package org.cbioportal.genomic_data.repository;

import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenomicDataCount;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.web.parameter.GenomicDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;
import java.util.Map;

public interface GenomicDataRepository {
    List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilterContext studyViewFilterContext);

    List<ClinicalDataCount> getGenomicDataBinCounts(StudyViewFilterContext studyViewFilterContext,
                                                    List<GenomicDataBinFilter> genomicDataBinFilters);

    List<GenomicDataCountItem> getCNACounts(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters);
    Map<String, Integer> getMutationCounts(StudyViewFilterContext studyViewFilterContext, GenomicDataFilter mutationFilters);
    List<GenomicDataCountItem> getMutationCountsByType(StudyViewFilterContext studyViewFilterContext,
                                                       List<GenomicDataFilter> genomicDataFilters);

}
