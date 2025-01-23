package org.cbioportal.legacy.service;

import org.cbioportal.legacy.service.util.CustomDataSession;

import java.util.List;
import java.util.Map;

public interface CustomDataService {
    Map<String, CustomDataSession> getCustomDataSessions(List<String> attributes);
}
