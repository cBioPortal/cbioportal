package org.cbioportal.infrastructure.repository.clickhouse.discrete_copy_number;

import java.util.List;
import org.cbioportal.domain.discrete_copy_number.repository.DiscreteCopyNumberRepository;
import org.cbioportal.legacy.model.DiscreteCopyNumberData;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseDiscreteCopyNumberRepository implements DiscreteCopyNumberRepository {

  private final ClickhouseDiscreteCopyNumberMapper mapper;

  public ClickhouseDiscreteCopyNumberRepository(ClickhouseDiscreteCopyNumberMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<DiscreteCopyNumberData> getDiscreteCopyNumbers(
      String molecularProfileId,
      String sampleListId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes,
      boolean includeGene) {
    return mapper.getDiscreteCopyNumbers(
        molecularProfileId, sampleListId, sampleIds, entrezGeneIds, alterationTypes, includeGene);
  }

  @Override
  public int countDiscreteCopyNumbers(
      String molecularProfileId,
      String sampleListId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes) {
    return mapper.countDiscreteCopyNumbers(
        molecularProfileId, sampleListId, sampleIds, entrezGeneIds, alterationTypes);
  }
}
