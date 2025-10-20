package org.cbioportal.legacy.persistence.mybatis;

import org.cbioportal.legacy.model.InfoDb;
import org.cbioportal.legacy.persistence.InfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class InfoMyBatisRepository implements InfoRepository {

  @Autowired private InfoMapper infoMapper;

  @Override
  public InfoDb getInfo() {
    return infoMapper.getInfo();
  }
}
