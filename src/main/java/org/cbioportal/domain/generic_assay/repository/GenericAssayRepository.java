package org.cbioportal.domain.generic_assay.repository;

import java.util.List;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;
import org.cbioportal.legacy.web.parameter.GenericAssayDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;

/** Repository interface for retrieving molecular profiles and assay-related data. */
public interface GenericAssayRepository {

  /**
   * Retrieves all generic assay molecular profiles.
   *
   * @return A list of {@link MolecularProfile} representing generic assay profiles.
   */
  List<MolecularProfile> getGenericAssayProfiles();

  /**
   * Retrieves molecular profiles filtered by alteration type based on the study view filter
   * context.
   *
   * @param studyViewFilterContext The filter criteria for the study view.
   * @param alterationType The type of alteration to filter the molecular profiles.
   * @return A list of {@link MolecularProfile} matching the criteria.
   */
  List<MolecularProfile> getFilteredMolecularProfilesByAlterationType(
      StudyViewFilterContext studyViewFilterContext, String alterationType);

  /**
   * Retrieves bin counts for generic assay data based on the study view filter context and
   * specified bin filters.
   *
   * @param studyViewFilterContext The filter criteria for the study view.
   * @param genericAssayDataBinFilters A list of bin filters to apply to the assay data.
   * @return A list of {@link ClinicalDataCount} representing bin counts for the generic assay data.
   */
  List<ClinicalDataCount> getGenericAssayDataBinCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<GenericAssayDataBinFilter> genericAssayDataBinFilters);

  /**
   * Retrieves counts for generic assay data based on the study view filter context and specified
   * data filters.
   *
   * @param studyViewFilterContext The filter criteria for the study view.
   * @param genericAssayDataFilters A list of data filters to apply to the assay data.
   * @return A list of {@link GenericAssayDataCountItem} representing assay data counts.
   */
  List<GenericAssayDataCountItem> getGenericAssayDataCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<GenericAssayDataFilter> genericAssayDataFilters);

  /**
   * Retrieves distinct generic assay entity stable IDs associated with the given molecular profile
   * IDs via generic_assay_profile_entity_derived.
   *
   * @param molecularProfileIds the list of molecular profile stable IDs
   * @return a list of distinct entity stable IDs
   */
  List<String> getGenericAssayStableIdsByProfileIds(List<String> molecularProfileIds);

  /**
   * Retrieves generic assay meta data (with pre-aggregated properties) for the given entity stable
   * IDs from the generic_assay_meta_derived table.
   *
   * @param stableIds the list of entity stable IDs
   * @return a list of {@link GenericAssayMeta} with properties pre-populated
   */
  List<GenericAssayMeta> getGenericAssayMetaByStableIds(List<String> stableIds);

  /**
   * Retrieves generic assay meta data for entities belonging to the given molecular profile IDs in
   * a single query. Resolves profile → entity via generic_assay_data_derived, then joins with
   * generic_assay_meta_derived for pre-aggregated properties. When {@code stableIds} is non-null,
   * results are further filtered to only those stable IDs.
   *
   * @param profileIds the list of molecular profile stable IDs (must be non-empty)
   * @param stableIds optional additional filter; pass {@code null} to return all entities
   * @return a list of {@link GenericAssayMeta} with properties pre-populated
   */
  List<GenericAssayMeta> getGenericAssayMetaByProfileIds(
      List<String> profileIds, List<String> stableIds);
}
