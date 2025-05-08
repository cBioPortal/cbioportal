package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.MutSig;
import org.cbioportal.legacy.model.meta.BaseMeta;

public interface SignificantlyMutatedGeneMapper {

  List<MutSig> getSignificantlyMutatedGenes(
      String studyId,
      String projection,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction);

  BaseMeta getMetaSignificantlyMutatedGenes(String studyId);
}
