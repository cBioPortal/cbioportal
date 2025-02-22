package org.cbioportal.legacy.service;

import java.util.List;
import org.cbioportal.legacy.model.Gistic;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;

public interface SignificantCopyNumberRegionService {

  List<Gistic> getSignificantCopyNumberRegions(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws StudyNotFoundException;

  BaseMeta getMetaSignificantCopyNumberRegions(String studyId) throws StudyNotFoundException;
}
