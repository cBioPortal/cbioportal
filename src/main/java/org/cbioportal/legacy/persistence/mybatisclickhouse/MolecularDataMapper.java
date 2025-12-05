package org.cbioportal.legacy.persistence.mybatisclickhouse;

import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.MolecularDataRowPerSample;

public interface MolecularDataMapper {

  List<MolecularDataRowPerSample> getGeneMolecularAlterationsPerSampleInMultipleMolecularProfiles(
      @Param("molecularProfileIds") Set<String> molecularProfileIds,
      @Param("entrezGeneIds") List<Integer> entrezGeneIds);

}
