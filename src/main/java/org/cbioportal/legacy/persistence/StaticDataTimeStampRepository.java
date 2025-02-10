package org.cbioportal.legacy.persistence;

import org.cbioportal.legacy.model.TableTimestampPair;

import java.util.Date;
import java.util.List;

public interface StaticDataTimeStampRepository {
    List<TableTimestampPair> getTimestamps(List<String> tables);
}
