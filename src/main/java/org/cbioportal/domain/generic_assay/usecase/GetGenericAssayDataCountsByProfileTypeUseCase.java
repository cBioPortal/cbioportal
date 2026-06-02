package org.cbioportal.domain.generic_assay.usecase;

import java.util.List;
import org.cbioportal.domain.generic_assay.repository.GenericAssayRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.springframework.stereotype.Service;

@Service
public class GetGenericAssayDataCountsByProfileTypeUseCase {
  private final GenericAssayRepository genericAssayRepository;

  public GetGenericAssayDataCountsByProfileTypeUseCase(
      GenericAssayRepository genericAssayRepository) {
    this.genericAssayRepository = genericAssayRepository;
  }

  public List<GenericAssayDataCountItem> execute(
      StudyViewFilterContext studyViewFilterContext, String profileType) {
    return genericAssayRepository.getGenericAssayDataCountsByProfileType(
        studyViewFilterContext, profileType);
  }
}
