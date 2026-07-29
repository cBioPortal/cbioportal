package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import org.cbioportal.domain.wsi.repository.WsiHierarchyRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseWsiHierarchyRepository implements WsiHierarchyRepository {

  private final ClickhouseWsiHierarchyMapper mapper;

  public ClickhouseWsiHierarchyRepository(ClickhouseWsiHierarchyMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public String getPatientHierarchy(String studyId, String patientId) {
    return mapper.getPatientHierarchy(studyId, patientId);
  }
}
