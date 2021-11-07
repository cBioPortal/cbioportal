/*
 * Copyright (c) 2019 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
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

package org.cbioportal.test.integration.security.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpHelper {

    public static class HttpResponse {
        public final String body;
        public final int code;
        public final Map<String, List<String>> headers;

        private HttpResponse(int code, String body, Map<String, List<String>> headers) {
            this.code = code;
            this.body = body;
            this.headers = headers;
        }
    }

    public static HttpResponse sendGetRequest(String url, String bearerToken, String cookie) throws IOException {
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        if (bearerToken != null) {
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
        }
        if (cookie != null) {
            connection.setRequestProperty("Cookie", cookie);
        }
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        Map<String, List<String>> headers = connection.getHeaderFields();

        InputStream inputStream = getInputStream(connection);
        
        String bodyContent = inputStream != null ? readString(inputStream) : null;

        return new HttpResponse(responseCode, bodyContent, headers);
    }

    public static HttpResponse sendPostRequest(String url, String bearerToken, String cookie,
                                               String body) throws IOException {
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        if (bearerToken != null) {
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
        }
        if (cookie != null) {
            connection.setRequestProperty("Cookie", cookie);
        }
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        writeString(body, connection);
        int responseCode = connection.getResponseCode();
        Map<String, List<String>> headers = connection.getHeaderFields();

        InputStream inputStream = getInputStream(connection);
        String bodyContent = readString(inputStream);

        return new HttpResponse(responseCode, bodyContent, headers);
    }

    private static void writeString(String body, HttpURLConnection connection) throws IOException {
        try(OutputStream os = connection.getOutputStream()) {
            byte[] input = body.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
    }

    private static InputStream getInputStream(HttpURLConnection connection) {
        try {
            return connection.getInputStream();
        } catch (IOException e) {
            return connection.getErrorStream();
        }
    }

    private static String readString(InputStream inputStream) throws IOException {
        String bodyContent;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            StringWriter out = new StringWriter();
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            bodyContent = out.toString();
        }
        return bodyContent;
    }
}
