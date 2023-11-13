package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.MutSig;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface SignificantlyMutatedGeneMapper {

    List<MutSig> getSignificantlyMutatedGenes(String studyId, String projection, Integer limit, Integer offset,
                                              String sortBy, String direction);

    BaseMeta getMetaSignificantlyMutatedGenes(String studyId);
}
