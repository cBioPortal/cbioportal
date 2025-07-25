package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.CosmicMutation;

public interface CosmicCountMapper {

  List<CosmicMutation> getCosmicCountsByKeywords(List<String> keywords);
}
