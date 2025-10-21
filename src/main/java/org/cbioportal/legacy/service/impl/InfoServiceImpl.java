package org.cbioportal.legacy.service.impl;

import org.cbioportal.legacy.model.InfoDb;
import org.cbioportal.legacy.persistence.InfoRepository;
import org.cbioportal.legacy.service.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InfoServiceImpl implements InfoService {

  @Autowired private InfoRepository infoRepository;

  @Override
  public InfoDb getInfoFromDb() {
    return infoRepository.getInfo();
  }
}
