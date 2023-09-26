package org.cbioportal.web.util;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.zip.GZIPInputStream;

public class GzippedInputStreamRequestWrapper extends HttpServletRequestWrapper {
    private final HttpServletRequest originalRequest;
    
    private final int maxInflatedRequestBodySize;

    GzippedInputStreamRequestWrapper(final HttpServletRequest request, int maxInflatedRequestBodySize) throws IOException {
        super(request);
        originalRequest = request;
        this.maxInflatedRequestBodySize = maxInflatedRequestBodySize;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new GZIPServletInputStream(originalRequest.getInputStream(), this.maxInflatedRequestBodySize); 
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }


    static class GZIPServletInputStream extends ServletInputStream {

        private final GZIPInputStream gzipInputStream;
        private final int maxInflatedRequestBodySize; 
        private int bytesRead = 0;

        public GZIPServletInputStream(ServletInputStream inputStream, int maxInflatedRequestBodySize) throws IOException {
            this.gzipInputStream = new GZIPInputStream(inputStream);
            this.maxInflatedRequestBodySize = maxInflatedRequestBodySize;
        }

        @Override
        public int read() throws IOException {
            int data = gzipInputStream.read();
            if (data != -1) {
                bytesRead++;
                if (bytesRead > maxInflatedRequestBodySize) {
                    throw new IOException(
                        "Inflated request body too large (> "
                            + maxInflatedRequestBodySize
                            + " bytes)"
                    );
                } 
            }
            return data;
        }

        @Override
        public boolean isFinished() {
            try {
                return gzipInputStream.available() == 0;
            } catch (IOException e) {
               return true; 
            }
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
    
        }

        @Override
        public void close() throws IOException {
            super.close();
            gzipInputStream.close();
        }

    }
}