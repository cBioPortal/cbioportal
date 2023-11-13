package org.cbioportal.service;

import org.cbioportal.service.util.CustomDataSession;

import java.util.List;
import java.util.Map;

public interface CustomDataService {
    Map<String, CustomDataSession> getCustomDataSessions(List<String> attributes);
}
