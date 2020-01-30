package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.CosmicMutation;

public interface CosmicCountMapper {
    List<CosmicMutation> getCosmicCountsByKeywords(List<String> keywords);
}
