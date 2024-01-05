package org.cbioportal.persistence.mysql;

import org.cbioportal.model.MutSig;
import org.cbioportal.model.meta.BaseMeta;
import org.springframework.context.annotation.Profile;

import java.util.List;

public interface SignificantlyMutatedGeneMapper {

    List<MutSig> getSignificantlyMutatedGenes(String studyId, String projection, Integer limit, Integer offset,
                                              String sortBy, String direction);

    BaseMeta getMetaSignificantlyMutatedGenes(String studyId);
}
