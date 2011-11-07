package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
public final class MutationDiagramDataServletTest {
    private MutationDiagramDataServlet dataServlet;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private DomainService domainService;
    private IdMappingService idMappingService;
    private MutationService mutationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        domainService = mock(DomainService.class, withSettings().serializable());
        idMappingService = mock(IdMappingService.class, withSettings().serializable());
        mutationService = mock(MutationService.class, withSettings().serializable());
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
        verify(response).setContentType("application/json");
        assertEquals("{\"id\":\"PIK3CA\",\"length\":800,\"label\":\"PIK3CA\\/P42336\"}", stringWriter.toString());
    }

    @Test
    public void testSerializable() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(dataServlet);
        out.close();
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        Object dest = in.readObject();
        in.close();
        assertNotNull((MutationDiagramDataServlet) dest);
    }
}

