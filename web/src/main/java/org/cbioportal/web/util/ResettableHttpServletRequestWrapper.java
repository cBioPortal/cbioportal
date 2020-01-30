package org.cbioportal.web.util;

import java.io.*;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ochoaa
 */
public class ResettableHttpServletRequestWrapper
    extends HttpServletRequestWrapper {
    private final Logger LOG = LoggerFactory.getLogger(
        ResettableHttpServletRequestFilter.class
    );

    private String rawData;
    private HttpServletRequest request;
    private ResettableServletInputStream servletStream;

    public ResettableHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.request = request;
        this.servletStream = new ResettableServletInputStream();
    }

    public void resetInputStream() {
        servletStream.stream = new ByteArrayInputStream(rawData.getBytes());
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (rawData == null) {
            rawData = "";
            BufferedReader reader = this.request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                rawData += line;
            }
            this.servletStream.stream =
                new ByteArrayInputStream(rawData.getBytes());
        }
        return servletStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (rawData == null) {
            rawData = "";
            BufferedReader reader = this.request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                rawData += line;
            }
            this.servletStream.stream =
                new ByteArrayInputStream(rawData.getBytes());
        }
        LOG.debug("getReader(): " + rawData);
        return new BufferedReader(new InputStreamReader(servletStream));
    }

    private class ResettableServletInputStream extends ServletInputStream {
        private InputStream stream;

        @Override
        public int read() throws IOException {
            return stream.read();
        }
    }
}
