package org.mskcc.portal.remote;

import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

/**
 * Singleton Instance of the Connection Manager.
 */
public class ConnectionManager {
    private static MultiThreadedHttpConnectionManager connectionManager = null;

    /**
     * Gets the Global Connection Manager.
     *
     * @return MultiThreadedHttpConnectionManager Object.
     */
    public static MultiThreadedHttpConnectionManager getConnectionManager() {
        if (connectionManager == null) {
            connectionManager = new MultiThreadedHttpConnectionManager();
            connectionManager.getParams().setDefaultMaxConnectionsPerHost(10);
            connectionManager.getParams().setConnectionTimeout(5000);
        }
        return connectionManager;
    }
}
