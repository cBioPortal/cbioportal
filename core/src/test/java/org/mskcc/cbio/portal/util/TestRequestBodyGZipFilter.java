package org.mskcc.cbio.portal.util;

import com.google.common.net.HttpHeaders;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Collectors;

public class TestRequestBodyGZipFilter {
    // This is the byte representation of a gzipped json file with the following contents: {"field":"value"}
    private static final byte[] GZIPPED_JSON_FILE_BYTES = {
        31, -117, 8, 8, -58, -49, -50, 95, 0, 3, 103, 122, 105, 112, 95, 102, 105, 108, 116, 101, 114, 46, 106, 115,
        111, 110, 0, -85, 86, 74, -53, 76, -51, 73, 81, -78, 82, 42, 75, -52, 41, 77, 85, -86, -27, 2, 0, 47, 99,
        109, 9, 18, 0, 0, 0
    };

    /**
     * When I filter a request that doesn't have the gzip header,
     * the request should not be wrapped, and chain.doFilter should
     * be called on the same request object passed to the method.
     */
    @Test
    public void testDoFilterEnabledNotGZipped() throws Exception {
        RequestBodyGZipFilter subject = createRequestBodyGZipFilter(true, 50000000);

        HttpServletRequest request = createHttpServletRequest(null, HttpMethod.POST);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);

        subject.doFilter(request, response, chain);
        
        // request should not be wrapped
        Mockito.verify(chain, Mockito.times(1)).doFilter(request, response);
    }

    /**
     * When I filter a request that has the gzip header, but the gzip
     * feature is disabled, the doFilter method should throw an exception.
     */
    @Test(expected = IllegalStateException.class)
    public void testDoFilterDisabledGZipped() throws Exception {
        RequestBodyGZipFilter subject = createRequestBodyGZipFilter(false, 50000000);

        HttpServletRequest request = createHttpServletRequest("gzip", HttpMethod.POST);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);

        subject.doFilter(request, response, chain);
    }

    /**
     * When I filter a request that has the gzip header and the gzip
     * feature is enabled, but the method is something other than POST,
     * the doFilter method should throw an exception.
     */
    @Test(expected = IllegalStateException.class)
    public void testDoFilterEnabledGZippedBadMethod() throws Exception {
        RequestBodyGZipFilter subject = createRequestBodyGZipFilter(true, 50000000);

        HttpServletRequest request = createHttpServletRequest("gzip", HttpMethod.GET);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);

        subject.doFilter(request, response, chain);
    }

    /**
     * When I filter a request that has the gzip header and the gzip
     * feature is enabled and the method is POST, the doFilter method
     * should wrap the request with a GzippedInputStreamWrapper.
     * The BufferedReader for this GzippedInputStreamWrapper should return
     * the contents of the gzipped file used to create the original input stream.
     */
    @Test
    public void testDoFilterEnabledGZipped() throws Exception {
        RequestBodyGZipFilter subject = createRequestBodyGZipFilter(true, 50000000);
        
        GzippedJsonFileInputStream stream = new GzippedJsonFileInputStream();
        HttpServletRequest request = createHttpServletRequest("gzip", HttpMethod.POST);
        Mockito.when(request.getInputStream()).thenReturn(stream);
        
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);
        
        subject.doFilter(request, response, chain);

        // Request should be wrapped, so the request should now be a GzippedInputStreamWrapper
        ArgumentCaptor<GzippedInputStreamRequestWrapper> requestArg =
            ArgumentCaptor.forClass(GzippedInputStreamRequestWrapper.class);
        ArgumentCaptor<HttpServletResponse> responseArg = ArgumentCaptor.forClass(HttpServletResponse.class);
        Mockito.verify(chain, Mockito.times(1)).doFilter(requestArg.capture(), responseArg.capture());
        GzippedInputStreamRequestWrapper wrappedStream = requestArg.getValue();
        
        String actualRequestBody = wrappedStream.getReader().lines().collect(Collectors.joining());
        String expectedRequestBody = "{\"field\":\"value\"}";
        Assert.assertEquals(expectedRequestBody, actualRequestBody);
    }

    /**
     * When I filter a request that has the gzip header and the gzip
     * feature is enabled and the method is POST, but the unzipped message
     * body is larger than maxInflatedRequestBodySize, the doFilter method
     * should wrap the request with a GzippedInputStreamWrapper.
     * The BufferedReader for this GzippedInputStreamWrapper should throw an
     * IOException as it is read. This exception gets turned into
     * an UncheckedIOException by something, so we'll check for that instead.
     */
    @Test(expected = UncheckedIOException.class)
    public void testDoFilterEnabledGZippedButTooLong() throws Exception {
        RequestBodyGZipFilter subject = createRequestBodyGZipFilter(true, 1);

        GzippedJsonFileInputStream stream = new GzippedJsonFileInputStream();
        HttpServletRequest request = createHttpServletRequest("gzip", HttpMethod.POST);
        Mockito.when(request.getInputStream()).thenReturn(stream);

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);
        
        subject.doFilter(request, response, chain);

        // Request should be wrapped, so the request should now be a GzippedInputStreamWrapper
        ArgumentCaptor<GzippedInputStreamRequestWrapper> requestArg =
            ArgumentCaptor.forClass(GzippedInputStreamRequestWrapper.class);
        ArgumentCaptor<HttpServletResponse> responseArg = ArgumentCaptor.forClass(HttpServletResponse.class);
        Mockito.verify(chain, Mockito.times(1)).doFilter(requestArg.capture(), responseArg.capture());
        GzippedInputStreamRequestWrapper wrappedStream = requestArg.getValue();

        //this should cause an IOException
        wrappedStream.getReader().lines().collect(Collectors.joining());
    }


    private RequestBodyGZipFilter createRequestBodyGZipFilter(boolean enabled, int requestBodySize) {
        RequestBodyGZipFilter subject = new RequestBodyGZipFilter();
        ReflectionTestUtils.setField(subject, "maxInflatedRequestBodySize", requestBodySize);
        ReflectionTestUtils.setField(subject, "enabled", enabled);
        return subject;
    }
    
    private HttpServletRequest createHttpServletRequest(String gzip, String httpMethod) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader(HttpHeaders.CONTENT_ENCODING)).thenReturn(gzip);
        Mockito.when(request.getMethod()).thenReturn(httpMethod);
        return request;
    }

    /**
     * TestInputStream wraps a FileInputStream so that it can be ServletInputStream
     * and passed to Filter::doFilter.
     */
    private static class GzippedJsonFileInputStream extends ServletInputStream {
        private final ByteArrayInputStream byteInputStream;

        GzippedJsonFileInputStream() {
            this.byteInputStream = new ByteArrayInputStream(GZIPPED_JSON_FILE_BYTES);
        }

        @Override
        public int read() throws IOException {
            return byteInputStream.read();
        }
    }
}