/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.web_api;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.http.params.CoreConnectionPNames;

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
    
    /**
     * Get a HttpClient
     * @param timeOut milliseconds
     * @return 
     */
    public static HttpClient getHttpClient(int timeOut) {
        if (timeOut<=0) {
            return new HttpClient(getConnectionManager());
        } else {
            HttpClientParams params = new HttpClientParams();
            params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeOut);
            params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, timeOut);
            return new HttpClient(params, getConnectionManager());
        }
    }
}
