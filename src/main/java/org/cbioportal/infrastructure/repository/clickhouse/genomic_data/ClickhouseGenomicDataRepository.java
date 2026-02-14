package org.cbioportal.infrastructure.repository.clickhouse.genomic_data;

import java.util.List;
import java.util.Map;
import org.cbioportal.domain.genomic_data.repository.GenomicDataRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenomicDataCount;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.web.parameter.GenomicDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseGenomicDataRepository implements GenomicDataRepository {

  private final ClickhouseGenomicDataMapper mapper;

  public ClickhouseGenomicDataRepository(ClickhouseGenomicDataMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<GenomicDataCount> getMolecularProfileSampleCounts(
      StudyViewFilterContext studyViewFilterContext) {
    return mapper.getMolecularProfileSampleCounts(studyViewFilterContext);
  }

  @Override
  public List<ClinicalDataCount> getGenomicDataBinCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<GenomicDataBinFilter> genomicDataBinFilters) {
    return mapper.getGenomicDataBinCounts(studyViewFilterContext, genomicDataBinFilters);
  }

  @Override
  public List<GenomicDataCountItem> getCNACounts(
      StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters) {
    return mapper.getCNACounts(studyViewFilterContext, genomicDataFilters);
  }

  @Override
  public Map<String, Integer> getMutationCounts(
      StudyViewFilterContext studyViewFilterContext, GenomicDataFilter genomicDataFilter) {
    return mapper.getMutationCounts(studyViewFilterContext, genomicDataFilter);
  }

  @Override
  public List<GenomicDataCountItem> getMutationCountsByType(
      StudyViewFilterContext studyViewFilterContext,
      List<GenomicDataFilter> genomicDataFilters,
      boolean includeSampleIds,
      String hugoGeneSymbol) {
    return mapper.getMutationCountsByType(
        studyViewFilterContext, genomicDataFilters, includeSampleIds, hugoGeneSymbol);
  }
}
