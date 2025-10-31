package org.cbioportal.legacy.service.impl;

import org.cbioportal.legacy.model.InfoDb;
import org.cbioportal.legacy.persistence.InfoRepository;
import org.cbioportal.legacy.service.InfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class InfoServiceImpl implements InfoService {

  private static final Logger logger = LoggerFactory.getLogger(InfoServiceImpl.class);

  @Autowired private InfoRepository infoRepository;

  @Override
  public InfoDb getInfoFromDb() {
    try {
      return infoRepository.getInfo();
    } catch (DataAccessException e) {
      logger.warn(
          "Failed to fetch info from database, likely due to schema mismatch. "
              + "Falling back to property-based configuration. Error: {}",
          e.getMessage());
      return null;
    } catch (Exception e) {
      logger.error("Unexpected error fetching info from database: {}", e.getMessage(), e);
      return null;
    }
  }
}
