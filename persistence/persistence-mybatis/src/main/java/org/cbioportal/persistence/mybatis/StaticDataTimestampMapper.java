package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.TableTimestampPair;

public interface StaticDataTimestampMapper {
    List<TableTimestampPair> getTimestamps(List<String> tables, String dbName);
}
