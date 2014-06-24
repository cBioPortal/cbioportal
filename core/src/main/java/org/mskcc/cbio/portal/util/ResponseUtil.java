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

package org.mskcc.cbio.portal.util;

import org.apache.commons.httpclient.HttpMethodBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Response Utility Class.
 */
public class ResponseUtil {

    /**
     * Reads in the Response String from Server.
     *
     * @param method HTTP Method.
     * @return Response String.
     * @throws IOException IO Error.
     */
    public static String getResponseString(HttpMethodBase method) throws IOException {
		return getResponseString(method.getResponseBodyAsStream());
	}

    /**
     * Reads in the Response String from Server.
     *
     * @param inStream InputStream.
     * @return Response String.
     * @throws IOException IO Error.
     */
    public static String getResponseString(InputStream inStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
        StringBuffer buf = new StringBuffer();
        String line = in.readLine();
        while (line != null) {
            buf.append(line);
            buf.append("\n");
            line = in.readLine();
        }
        return buf.toString();
    }
}
