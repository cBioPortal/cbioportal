package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.TableTimestampPair;

import java.util.List;

public interface StaticDataTimestampMapper {
    List<TableTimestampPair> getTimestamps(List<String> tables, String dbName);
}
