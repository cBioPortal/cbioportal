package org.cbioportal.infrastructure.repository.clickhouse.generic_assay;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.ResultHandler;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;
import org.cbioportal.legacy.web.parameter.GenericAssayDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;

/**
 * Mapper interface for retrieving generic assay data from ClickHouse. This interface provides
 * methods to fetch molecular profiles and generic assay data counts, as well as filtered molecular
 * profiles by alteration type.
 */
public interface ClickhouseGenericAssayMapper {

  /**
   * Retrieves the list of molecular profiles from the generic assay data.
   *
   * @return a list of molecular profiles
   */
  List<MolecularProfile> getGenericAssayProfiles();

  /**
   * Retrieves the filtered molecular profiles based on the study view filter context and alteration
   * type.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @param alterationType the alteration type (e.g., mutation, CNA)
   * @return a list of filtered molecular profiles
   */
  List<MolecularProfile> getFilteredMolecularProfilesByAlterationType(
      StudyViewFilterContext studyViewFilterContext, String alterationType);

  /**
   * Retrieves the generic assay data bin counts based on the study view filter context and the
   * provided bin filters.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @param genericAssayDataBinFilters the list of bin filters
   * @return a list of generic assay data bin counts
   */
  List<ClinicalDataCount> getGenericAssayDataBinCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<GenericAssayDataBinFilter> genericAssayDataBinFilters);

  /**
   * Retrieves the generic assay data counts based on the study view filter context and the provided
   * data filters.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @param genericAssayDataFilters the list of data filters
   * @return a list of generic assay data counts
   */
  List<GenericAssayDataCountItem> getGenericAssayDataCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<GenericAssayDataFilter> genericAssayDataFilters);

  /**
   * Retrieves distinct generic assay entity stable IDs associated with the given molecular profile
   * IDs.
   *
   * @param molecularProfileIds the list of molecular profile stable IDs
   * @return a list of distinct entity stable IDs
   */
  List<String> getGenericAssayStableIdsByProfileIds(List<String> molecularProfileIds);

  /**
   * Streams generic assay meta data (with pre-aggregated properties) for the given entity stable
   * IDs from the generic_assay_meta_derived table. Each mapped row is passed to {@code handler} as
   * it is read, so the full result set is never materialized in memory.
   *
   * @param stableIds the list of entity stable IDs
   * @param handler invoked once per {@link GenericAssayMeta} row
   */
  void getGenericAssayMetaByStableIds(
      @Param("list") List<String> stableIds, ResultHandler<GenericAssayMeta> handler);

  /**
   * Resolves profile IDs → entity stable IDs via generic_assay_profile_entity_derived and joins
   * with generic_assay_meta_derived in a single query, streaming each mapped row to {@code handler}
   * as it is read rather than materializing the full result set.
   *
   * @param profileIds the list of molecular profile stable IDs
   * @param stableIds optional additional stable ID filter; {@code null} means no filter
   * @param handler invoked once per {@link GenericAssayMeta} row
   */
  void getGenericAssayMetaByProfileIds(
      @Param("profileIds") List<String> profileIds,
      @Param("stableIds") List<String> stableIds,
      ResultHandler<GenericAssayMeta> handler);
}
