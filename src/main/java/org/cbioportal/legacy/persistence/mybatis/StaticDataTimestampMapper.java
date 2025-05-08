package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.TableTimestampPair;

public interface StaticDataTimestampMapper {
  List<TableTimestampPair> getTimestamps(List<String> tables, String dbName);
}
