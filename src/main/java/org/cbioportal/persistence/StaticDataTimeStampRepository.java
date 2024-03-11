package org.cbioportal.persistence;

import java.util.List;
import org.cbioportal.model.TableTimestampPair;

public interface StaticDataTimeStampRepository {
  List<TableTimestampPair> getTimestamps(List<String> tables);
}
