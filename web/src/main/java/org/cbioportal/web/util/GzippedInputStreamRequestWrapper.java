package org.cbioportal.web.util;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class GzippedInputStreamRequestWrapper extends HttpServletRequestWrapper {
    public static final int BUFFER_SIZE = 1024;
    private final ByteArrayInputStream inputStream;

    GzippedInputStreamRequestWrapper(final HttpServletRequest request, int maxInflatedRequestBodySize) throws IOException {
        super(request);
        inputStream = inflate(request.getInputStream(), maxInflatedRequestBodySize);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStream() {
            @Override
            public int read() {
                return inputStream.read();
            }

            @Override
            public int readLine(byte[] b, int off, int len) throws IOException {
                return super.readLine(b, off, len);
            }

            @Override
            public boolean isFinished() {
                return true;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read(byte[] bytes) throws IOException {
                return super.read(bytes);
            }

            @Override
            public int read(byte[] bytes, int i, int i1) throws IOException {
                return super.read(bytes, i, i1);
            }

            @Override
            public void close() throws IOException {
                super.close();
                inputStream.close();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * Inflates the compressed input stream all at once. Previously, we wrapped the compressed ServletInputStream
     * stream with a GZIPInputStream, and read from that gradually. For some reason, that approach resulted in
     * very long inflation times. By reading out everything from the GZIPInputStream here in the constructor, we
     * managed to get down to the sub second performance you would expect with gzipping.
     */
    private ByteArrayInputStream inflate(ServletInputStream requestStream, int maxInflatedRequestBodySize) throws IOException {
        GZIPInputStream gzipInputStream = new GZIPInputStream(requestStream, BUFFER_SIZE);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        long byteCount = 0;

        while((len = gzipInputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
            byteCount += BUFFER_SIZE;

            if (byteCount > maxInflatedRequestBodySize) {
                throw new IOException(
                    "Inflated request body too large (> "
                        + maxInflatedRequestBodySize
                        + " bytes)"
                );
            }
        }

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

}