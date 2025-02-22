package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.TableTimestampPair;
import org.cbioportal.legacy.persistence.StaticDataTimeStampRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class StaticDataTimeStampMyBatisRepository implements StaticDataTimeStampRepository {
  @Value("${db.portal_db_name:}")
  private String dbName;

  @Autowired private StaticDataTimestampMapper staticDataTimestampMapper;

  @Override
  public List<TableTimestampPair> getTimestamps(List<String> tableNames) {
    return staticDataTimestampMapper.getTimestamps(tableNames, dbName);
  }
}
