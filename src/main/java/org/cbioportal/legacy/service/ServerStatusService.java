package org.cbioportal.legacy.service;

import org.cbioportal.legacy.service.impl.ServerStatusServiceImpl.ServerStatusMessage;

public interface ServerStatusService {
    ServerStatusMessage getServerStatus();
}
