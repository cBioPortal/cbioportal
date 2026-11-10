package org.cbioportal.infrastructure.repository.clickhouse.gene_panel_data;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.domain.gene_panel_data.repository.GenePanelDataRepository;
import org.cbioportal.legacy.model.GenePanelData;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseGenePanelDataRepository implements GenePanelDataRepository {

  private final ClickhouseGenePanelDataMapper mapper;

  public ClickhouseGenePanelDataRepository(ClickhouseGenePanelDataMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<GenePanelData> getGenePanelDataByProfileIds(Collection<String> molecularProfileIds) {
    return mapper.getGenePanelDataByProfileIds(molecularProfileIds);
  }

  @Override
  public List<GenePanelData> getGenePanelDataByProfileSamplePairs(
      List<SampleMolecularIdentifier> identifiers) {
    Set<String> profileIds =
        identifiers.stream()
            .map(SampleMolecularIdentifier::getMolecularProfileId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    return mapper.getGenePanelDataByProfileSamplePairs(profileIds, identifiers);
  }
}
