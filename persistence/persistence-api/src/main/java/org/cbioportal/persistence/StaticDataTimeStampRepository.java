package org.cbioportal.persistence;

import org.cbioportal.model.TableTimestampPair;

import java.util.Date;
import java.util.List;

public interface StaticDataTimeStampRepository {
    List<TableTimestampPair> getTimestamps(List<String> tables);
}
