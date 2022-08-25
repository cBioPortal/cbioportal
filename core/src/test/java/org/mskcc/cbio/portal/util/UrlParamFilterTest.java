package org.mskcc.cbio.portal.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidParameterException;

public class UrlParamFilterTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void shouldFilterMaliciousURL() throws ServletException, IOException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getParameter("configUrl")).thenReturn("asdf");
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);

        exceptionRule.expect(InvalidParameterException.class);
        new UrlParamFilter().doFilter(request, response, chain);
        
        Mockito.verify(chain, Mockito.times(0)).doFilter(request, response);
    }

    @Test
    public void shouldNotFilterRegularURL() throws ServletException, IOException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getParameter("configUrl")).thenReturn(null);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);

        new UrlParamFilter().doFilter(request, response, chain);

        Mockito.verify(chain, Mockito.times(1)).doFilter(request, response);
    }
}