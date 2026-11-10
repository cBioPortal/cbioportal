package org.cbioportal.infrastructure.repository.clickhouse.genomic_data;

import java.util.List;
import java.util.stream.IntStream;
import org.cbioportal.domain.genomic_data.repository.GeneMolecularDataRepository;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.model.MolecularProfile;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseGeneMolecularDataRepository implements GeneMolecularDataRepository {

  private final ClickhouseGeneMolecularDataMapper mapper;

  public ClickhouseGeneMolecularDataRepository(ClickhouseGeneMolecularDataMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<GeneMolecularData> getGeneMolecularData(
      String studyId,
      String molecularProfileId,
      String profileType,
      List<Integer> entrezGeneIds,
      String sampleListId,
      List<String> sampleIds,
      boolean includeGene) {
    // sample_unique_id in the derived tables is "<studyId>_<sampleId>"
    String[] sampleUniqueIds =
        sampleIds == null
            ? null
            : sampleIds.stream().map(id -> studyId + "_" + id).toArray(String[]::new);
    return mapper.getGeneMolecularData(
        studyId,
        molecularProfileId,
        profileType,
        entrezGeneIds,
        sampleListId,
        sampleUniqueIds,
        includeGene);
  }

  @Override
  public List<GeneMolecularData> getGeneMolecularDataInMultipleProfiles(
      List<MolecularProfile> profiles,
      List<Integer> entrezGeneIds,
      List<String> sampleIds,
      List<String> sampleStudyIds,
      boolean includeGene) {
    String[] sampleUniqueIds = null;
    if (sampleIds != null) {
      sampleUniqueIds =
          IntStream.range(0, sampleIds.size())
              .mapToObj(i -> sampleStudyIds.get(i) + "_" + sampleIds.get(i))
              .distinct()
              .toArray(String[]::new);
    }
    return mapper.getGeneMolecularDataInMultipleProfiles(
        profiles, entrezGeneIds, sampleUniqueIds, includeGene);
  }
}
