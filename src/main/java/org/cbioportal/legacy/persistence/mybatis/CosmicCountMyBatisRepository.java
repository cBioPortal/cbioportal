package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.CosmicMutation;
import org.cbioportal.legacy.persistence.CosmicCountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CosmicCountMyBatisRepository implements CosmicCountRepository {

  @Autowired private CosmicCountMapper cosmicCountMapper;

  public List<CosmicMutation> fetchCosmicCountsByKeywords(List<String> keywords) {

    return cosmicCountMapper.getCosmicCountsByKeywords(keywords);
  }
}
