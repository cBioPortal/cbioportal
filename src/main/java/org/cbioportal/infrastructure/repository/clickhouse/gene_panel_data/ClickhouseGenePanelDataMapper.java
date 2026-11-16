package org.cbioportal.infrastructure.repository.clickhouse.gene_panel_data;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.GenePanelData;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;

public interface ClickhouseGenePanelDataMapper {

  List<GenePanelData> getGenePanelDataByProfileIds(
      @Param("profileIds") Collection<String> profileIds);

  List<GenePanelData> getGenePanelDataByProfileSamplePairs(
      @Param("profileIds") Collection<String> profileIds,
      @Param("identifiers") List<SampleMolecularIdentifier> identifiers);
}
