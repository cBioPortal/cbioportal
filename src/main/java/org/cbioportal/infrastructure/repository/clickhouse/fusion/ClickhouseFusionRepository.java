package org.cbioportal.infrastructure.repository.clickhouse.fusion;

import java.util.List;
import org.cbioportal.domain.fusion.FusionFilter;
import org.cbioportal.domain.fusion.repository.FusionRepository;
import org.cbioportal.legacy.model.StructuralVariant;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseFusionRepository implements FusionRepository {

  private final ClickhouseFusionMapper mapper;

  public ClickhouseFusionRepository(ClickhouseFusionMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<StructuralVariant> fetchFusions(FusionFilter filter) {
    return mapper.getFusions(
        filter.studyIds(), filter.molecularProfileIds(), filter.entrezGeneIds());
  }
}
