package org.cbioportal.service;

import java.util.List;
import java.util.Map;
import org.cbioportal.service.util.CustomDataSession;

public interface CustomDataService {
  Map<String, CustomDataSession> getCustomDataSessions(List<String> attributes);
}
