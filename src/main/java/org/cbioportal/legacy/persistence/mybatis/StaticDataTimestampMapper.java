package org.cbioportal.legacy.persistence.mybatis;

import org.cbioportal.legacy.model.TableTimestampPair;

import java.util.List;

public interface StaticDataTimestampMapper {
    List<TableTimestampPair> getTimestamps(List<String> tables, String dbName);
}
