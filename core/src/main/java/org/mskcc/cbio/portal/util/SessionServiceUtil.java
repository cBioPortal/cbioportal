/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.httpclient.HttpException;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.JsonNode;

import java.nio.charset.Charset;

import java.io.StringWriter;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;


import java.util.Map;

/**
 * 
 * @author Manda Wilson
 */
public class SessionServiceUtil {

    private static Log LOG = LogFactory.getLog(SessionServiceUtil.class);

    /**
     * Returns an ServletRequest parameter map for a given sessionId.  
     * Returns null if the session was not found.
     *
     * @param sessionId
     * @return an ServletRequest parameter map
     * @throws HttpException if session service API returns with a response code that is not 404 or 200
     * @throws MalformedURLException 
     * @throws IOException
     * @see ServletRequest#getParameterMap
     */
    public static Map<String, String[]> getSession(String sessionId)
        throws HttpException, MalformedURLException, IOException {
        LOG.debug("SessionServiceUtil.getSession()");
        Map<String, String[]> parameterMap = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(GlobalProperties.getSessionServiceUrl() + "main_session/" + sessionId);
            LOG.debug("SessionServiceUtil.getSession(): url = '" + url + "'");
            conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringWriter stringWriter = new StringWriter();
                IOUtils.copy(conn.getInputStream(), stringWriter, Charset.forName("UTF-8"));
                String contentString = stringWriter.toString();
                LOG.debug("SessionServiceUtil.getSession(): response = '" + contentString + "'");
                JsonNode json = new ObjectMapper().readTree(contentString); 
                LOG.debug("SessionServiceUtil.getSession(): response.data = '" + json.get("data").getValueAsText() + "'");
                parameterMap = new ObjectMapper().readValue(json.get("data").toString(), new TypeReference<Map<String, String[]>>(){});
            } else {
                LOG.warn("SessionServiceUtil.getSession(): conn.getResponseCode() = '" + conn.getResponseCode() + "'");
                if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    return null;
                } else if (conn.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    throw new HttpException("Internal server error");
                } else {
                    throw new HttpException("Unexpected error, response code '" + conn.getResponseCode() +"'");
                }
            } 
        } catch (MalformedURLException mfue) {  
            LOG.warn("SessionServiceUtil.getSession(): MalformedURLException = '" + mfue.getMessage() + "'");
            throw mfue;
        } catch (IOException ioe) {
            LOG.warn("SessionServiceUtil.getSession(): IOException = '" + ioe.getMessage() + "'");
            throw ioe;
        } finally { 
            if (conn != null) {
                conn.disconnect();
            }
        }
        return parameterMap;
    }
}
