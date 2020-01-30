package org.cbioportal.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.cbioportal.model.TableTimestampPair;

public interface StaticDataTimestampService {
    Map<String, String> getTimestamps(List<String> tables);

    Map<String, Date> getTimestampsAsDates(List<String> tables);
}
