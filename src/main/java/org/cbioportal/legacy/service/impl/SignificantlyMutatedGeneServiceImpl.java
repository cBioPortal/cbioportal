package org.cbioportal.legacy.service.impl;

import java.util.List;
import org.cbioportal.legacy.model.MutSig;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.SignificantlyMutatedGeneRepository;
import org.cbioportal.legacy.service.SignificantlyMutatedGeneService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SignificantlyMutatedGeneServiceImpl implements SignificantlyMutatedGeneService {

  @Autowired private SignificantlyMutatedGeneRepository significantlyMutatedGeneRepository;
  @Autowired private StudyService studyService;

  @Override
  public List<MutSig> getSignificantlyMutatedGenes(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws StudyNotFoundException {

    studyService.studyExists(studyId);

    return significantlyMutatedGeneRepository.getSignificantlyMutatedGenes(
        studyId, projection, pageSize, pageNumber, sortBy, direction);
  }

  @Override
  public BaseMeta getMetaSignificantlyMutatedGenes(String studyId) throws StudyNotFoundException {

    studyService.studyExists(studyId);

    return significantlyMutatedGeneRepository.getMetaSignificantlyMutatedGenes(studyId);
  }
}
