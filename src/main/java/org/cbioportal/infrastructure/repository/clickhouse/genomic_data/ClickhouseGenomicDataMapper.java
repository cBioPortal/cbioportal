package org.cbioportal.infrastructure.repository.clickhouse.genomic_data;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenomicDataCount;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.web.parameter.GenomicDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;

/**
 * Mapper interface for retrieving genomic data from ClickHouse. This interface provides methods to
 * fetch genomic data counts, including mutation counts and CNAs, based on the study view filter
 * context and genomic data filters.
 */
public interface ClickhouseGenomicDataMapper {

  /**
   * Retrieves the molecular profile sample counts based on the study view filter context.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @return a list of genomic data counts by molecular profile
   */
  List<GenomicDataCount> getMolecularProfileSampleCounts(
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);

  /**
   * Retrieves the genomic data bin counts based on the study view filter context and genomic data
   * bin filters.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @param genomicDataBinFilters the list of genomic data bin filters
   * @return a list of genomic data bin counts
   */
  List<ClinicalDataCount> getGenomicDataBinCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<GenomicDataBinFilter> genomicDataBinFilters);

  /**
   * Retrieves CNAs counts based on the study view filter context and genomic data filters.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @param genomicDataFilters the list of genomic data filters
   * @return a list of CNA counts
   */
  List<GenomicDataCountItem> getCNACounts(
      StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters);

  /**
   * Retrieves mutation counts based on the study view filter context and genomic data filter.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @param genomicDataFilter the genomic data filter
   * @return a map of mutation counts by gene
   */
  Map<String, Integer> getMutationCounts(
      StudyViewFilterContext studyViewFilterContext, GenomicDataFilter genomicDataFilter);

  /**
   * Retrieves mutation counts by type based on the study view filter context and genomic data
   * filters.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @param genomicDataFilters the list of genomic data filters
   * @return a list of mutation counts by type
   */
  List<GenomicDataCountItem> getMutationCountsByType(
      StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters);
}
