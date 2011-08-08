package org.mskcc.portal.util;

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
