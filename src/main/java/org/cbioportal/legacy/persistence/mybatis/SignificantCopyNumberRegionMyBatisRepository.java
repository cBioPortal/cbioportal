package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.Gistic;
import org.cbioportal.legacy.model.GisticToGene;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.SignificantCopyNumberRegionRepository;
import org.cbioportal.legacy.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SignificantCopyNumberRegionMyBatisRepository
    implements SignificantCopyNumberRegionRepository {

  @Autowired private SignificantCopyNumberRegionMapper significantCopyNumberRegionMapper;

  @Override
  public List<Gistic> getSignificantCopyNumberRegions(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {

    return significantCopyNumberRegionMapper.getSignificantCopyNumberRegions(
        studyId,
        projection,
        pageSize,
        PaginationCalculator.offset(pageSize, pageNumber),
        sortBy,
        direction);
  }

  @Override
  public BaseMeta getMetaSignificantCopyNumberRegions(String studyId) {

    return significantCopyNumberRegionMapper.getMetaSignificantCopyNumberRegions(studyId);
  }

  @Override
  public List<GisticToGene> getGenesOfRegions(List<Long> gisticRoiIds) {

    return significantCopyNumberRegionMapper.getGenesOfRegions(gisticRoiIds);
  }
}
