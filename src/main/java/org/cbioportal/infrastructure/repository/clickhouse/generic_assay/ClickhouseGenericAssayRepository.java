package org.cbioportal.infrastructure.repository.clickhouse.generic_assay;

import java.util.List;
import org.cbioportal.domain.generic_assay.repository.GenericAssayRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;
import org.cbioportal.legacy.web.parameter.GenericAssayDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseGenericAssayRepository implements GenericAssayRepository {

  private final ClickhouseGenericAssayMapper mapper;

  public ClickhouseGenericAssayRepository(ClickhouseGenericAssayMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<MolecularProfile> getGenericAssayProfiles() {
    return mapper.getGenericAssayProfiles();
  }

  @Override
  public List<MolecularProfile> getFilteredMolecularProfilesByAlterationType(
      StudyViewFilterContext studyViewFilterContext, String alterationType) {
    return mapper.getFilteredMolecularProfilesByAlterationType(
        studyViewFilterContext, alterationType);
  }

  @Override
  public List<ClinicalDataCount> getGenericAssayDataBinCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<GenericAssayDataBinFilter> genericAssayDataBinFilters) {
    return mapper.getGenericAssayDataBinCounts(studyViewFilterContext, genericAssayDataBinFilters);
  }

  @Override
  public List<GenericAssayDataCountItem> getGenericAssayDataCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<GenericAssayDataFilter> genericAssayDataFilters) {
    return mapper.getGenericAssayDataCounts(studyViewFilterContext, genericAssayDataFilters);
  }

  @Override
  public List<GenericAssayDataCountItem> getGenericAssayDataCountsByProfileType(
      StudyViewFilterContext studyViewFilterContext, String profileType) {
    return mapper.getGenericAssayDataCountsByProfileType(studyViewFilterContext, profileType);
  }

  @Override
  public List<String> getGenericAssayStableIdsByProfileIds(List<String> molecularProfileIds) {
    return mapper.getGenericAssayStableIdsByProfileIds(molecularProfileIds);
  }

  @Override
  public List<GenericAssayMeta> getGenericAssayMetaByStableIds(
      List<String> stableIds, String searchTerm, Integer pageSize, Integer offset) {
    return mapper.getGenericAssayMetaByStableIds(stableIds, searchTerm, pageSize, offset);
  }

  @Override
  public Integer countGenericAssayMetaByStableIds(List<String> stableIds, String searchTerm) {
    return mapper.countGenericAssayMetaByStableIds(stableIds, searchTerm);
  }

  @Override
  public List<GenericAssayMeta> getGenericAssayMetaByProfileIds(
      List<String> profileIds,
      List<String> stableIds,
      String searchTerm,
      Integer pageSize,
      Integer offset) {
    return mapper.getGenericAssayMetaByProfileIds(
        profileIds, stableIds, searchTerm, pageSize, offset);
  }

  @Override
  public Integer countGenericAssayMetaByProfileIds(
      List<String> profileIds, List<String> stableIds, String searchTerm) {
    return mapper.countGenericAssayMetaByProfileIds(profileIds, stableIds, searchTerm);
  }
}
