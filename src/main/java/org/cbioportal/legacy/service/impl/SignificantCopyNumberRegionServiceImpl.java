package org.cbioportal.legacy.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.Gistic;
import org.cbioportal.legacy.model.GisticToGene;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.SignificantCopyNumberRegionRepository;
import org.cbioportal.legacy.service.SignificantCopyNumberRegionService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SignificantCopyNumberRegionServiceImpl implements SignificantCopyNumberRegionService {

  @Autowired private SignificantCopyNumberRegionRepository significantCopyNumberRegionRepository;
  @Autowired private StudyService studyService;

  @Override
  public List<Gistic> getSignificantCopyNumberRegions(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws StudyNotFoundException {

    studyService.studyExists(studyId);

    List<Gistic> gisticList =
        significantCopyNumberRegionRepository.getSignificantCopyNumberRegions(
            studyId, projection, pageSize, pageNumber, sortBy, direction);

    if (!projection.equals("ID") && !gisticList.isEmpty()) {

      List<GisticToGene> gisticToGeneList =
          significantCopyNumberRegionRepository.getGenesOfRegions(
              gisticList.stream().map(Gistic::getGisticRoiId).collect(Collectors.toList()));

      gisticList.forEach(
          g ->
              g.setGenes(
                  gisticToGeneList.stream()
                      .filter(p -> p.getGisticRoiId().equals(g.getGisticRoiId()))
                      .collect(Collectors.toList())));
    }

    return gisticList;
  }

  @Override
  public BaseMeta getMetaSignificantCopyNumberRegions(String studyId)
      throws StudyNotFoundException {

    studyService.studyExists(studyId);

    return significantCopyNumberRegionRepository.getMetaSignificantCopyNumberRegions(studyId);
  }
}
