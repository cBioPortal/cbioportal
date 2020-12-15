package org.mskcc.cbio.portal.util;

import com.google.common.io.CountingInputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class GzippedInputStreamRequestWrapper extends HttpServletRequestWrapper {
    private final int maxInflatedRequestBodySize;
    private final CountingInputStream inputStream;

    GzippedInputStreamRequestWrapper(final HttpServletRequest request, int maxInflatedRequestBodySize) throws IOException {
        super(request);
        this.maxInflatedRequestBodySize = maxInflatedRequestBodySize;
        inputStream = new CountingInputStream(new GZIPInputStream(request.getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                checkByteCount();
                return inputStream.read();
            }

            @Override
            public int readLine(byte[] b, int off, int len) throws IOException {
                checkByteCount();
                return super.readLine(b, off, len);
            }

            @Override
            public int read(byte[] bytes) throws IOException {
                checkByteCount();
                return super.read(bytes);
            }

            @Override
            public int read(byte[] bytes, int i, int i1) throws IOException {
                checkByteCount();
                return super.read(bytes, i, i1);
            }
            
            private void checkByteCount() throws IOException {
                if (inputStream.getCount() > maxInflatedRequestBodySize) {
                    throw new IOException(
                        "Inflated request body too large (> "
                            + maxInflatedRequestBodySize
                            + " bytes)"
                    );
                }
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
}
