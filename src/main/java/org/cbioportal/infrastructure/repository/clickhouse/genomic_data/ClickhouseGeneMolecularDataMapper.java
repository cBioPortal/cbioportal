package org.cbioportal.infrastructure.repository.clickhouse.genomic_data;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.model.MolecularProfile;

public interface ClickhouseGeneMolecularDataMapper {

  List<GeneMolecularData> getGeneMolecularData(
      @Param("studyId") String studyId,
      @Param("molecularProfileId") String molecularProfileId,
      @Param("profileType") String profileType,
      @Param("entrezGeneIds") List<Integer> entrezGeneIds,
      @Param("sampleListId") String sampleListId,
      @Param("sampleUniqueIds") String[] sampleUniqueIds,
      @Param("includeGene") boolean includeGene);

  List<GeneMolecularData> getGeneMolecularDataInMultipleProfiles(
      @Param("profiles") List<MolecularProfile> profiles,
      @Param("entrezGeneIds") List<Integer> entrezGeneIds,
      @Param("sampleUniqueIds") String[] sampleUniqueIds,
      @Param("includeGene") boolean includeGene);
}
