package org.cbioportal.legacy.persistence;

import java.util.List;
import org.cbioportal.legacy.model.TableTimestampPair;

public interface StaticDataTimeStampRepository {
  List<TableTimestampPair> getTimestamps(List<String> tables);
}
