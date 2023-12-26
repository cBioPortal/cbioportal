package org.cbioportal.persistence.mysql;

import org.cbioportal.model.TableTimestampPair;
import org.springframework.context.annotation.Profile;

import java.util.List;

public interface StaticDataTimestampMapper {
    List<TableTimestampPair> getTimestamps(List<String> tables, String dbName);
}
