package org.cbioportal.legacy.service;

import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.service.util.CustomDataSession;

public interface CustomDataService {
  Map<String, CustomDataSession> getCustomDataSessions(List<String> attributes);
}
