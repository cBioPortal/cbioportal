package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test for MutationDiagramDataServlet.
 */
public class MutationDiagramDataServletTest {
    private MutationDiagramDataServlet dataServlet;

    @Mock
    private DomainService domainService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private IdMappingService idMappingService;
    @Mock
    private MutationService mutationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        dataServlet = new MutationDiagramDataServlet(domainService, idMappingService, mutationService);
    }

    @Test
    public void testConstructor() {
        assertNotNull(dataServlet);
    }

    @Test
    public void testDoPost() throws Exception {
        when(request.getParameter("hugoGeneSymbol")).thenReturn("PIK3CA");
        when(idMappingService.getUniProtId("PIK3CA")).thenReturn("P42336");
        when(domainService.getDomains("P42336")).thenReturn(Collections.<Domain>emptyList());
        when(mutationService.getMutations("PIK3CA")).thenReturn(Collections.<Mutation>emptyList());
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        dataServlet.doPost(request, response);
        assertEquals("{\"length\":0,\"label\":\"PIK3CA\\/P42336\"}", stringWriter.toString());
    }

    @Test
    public void testSerializable() throws Exception {
    }
}

