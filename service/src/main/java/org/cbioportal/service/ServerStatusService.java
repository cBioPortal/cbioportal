package org.cbioportal.service;

import org.cbioportal.service.impl.ServerStatusServiceImpl.ServerStatusMessage;

public interface ServerStatusService {
    ServerStatusMessage getServerStatus();
}
