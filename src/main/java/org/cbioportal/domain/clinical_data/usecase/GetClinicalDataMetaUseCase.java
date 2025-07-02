package org.cbioportal.domain.clinical_data.usecase;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.domain.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.legacy.web.parameter.ClinicalDataIdentifier;
import org.cbioportal.legacy.web.parameter.ClinicalDataMultiStudyFilter;
import org.cbioportal.shared.enums.ClinicalDataType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class GetClinicalDataMetaUseCase {

  private final ClinicalDataRepository clinicalDataRepository;

  public GetClinicalDataMetaUseCase(ClinicalDataRepository clinicalDataRepository) {
    this.clinicalDataRepository = clinicalDataRepository;
  }

  public Integer execute(
      ClinicalDataMultiStudyFilter clinicalDataMultiStudyFilter,
      ClinicalDataType clinicalDataType) {
    List<String> uniqueIds = new ArrayList<>();
    for (ClinicalDataIdentifier identifier : clinicalDataMultiStudyFilter.getIdentifiers()) {
      uniqueIds.add(identifier.getStudyId() + '_' + identifier.getEntityId());
    }
    List<String> attributeIds = clinicalDataMultiStudyFilter.getAttributeIds();

    return clinicalDataRepository.getClinicalDataCount(uniqueIds, attributeIds, clinicalDataType);
  }
}
