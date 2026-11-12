package org.cbioportal.infrastructure.repository.clickhouse.geneset_data;

import java.util.List;
import org.cbioportal.domain.geneset_data.repository.GenesetDataRepository;
import org.cbioportal.legacy.model.GenesetMolecularData;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseGenesetDataRepository implements GenesetDataRepository {

  private final ClickhouseGenesetDataMapper mapper;

  public ClickhouseGenesetDataRepository(ClickhouseGenesetDataMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<GenesetMolecularData> getGenesetData(
      String molecularProfileId, List<String> sampleIds, List<String> genesetIds) {
    return mapper.getGenesetData(molecularProfileId, sampleIds, genesetIds);
  }
}
