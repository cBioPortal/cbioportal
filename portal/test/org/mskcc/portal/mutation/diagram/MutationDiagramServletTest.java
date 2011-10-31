package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test for MutationDiagramServlet.
 */
public final class MutationDiagramServletTest {
    private MutationDiagramServlet mutationDiagramServlet;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    private ServletContext servletContext;
    @Mock
    private RequestDispatcher dispatcher;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        mutationDiagramServlet = new MutationDiagramServlet();
        try {
            mutationDiagramServlet.init(servletConfig);
        }
        catch (ServletException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testConstructor() {
        assertNotNull(mutationDiagramServlet);
    }

    @Test
    public void testDoPost() throws Exception {
        when(servletContext.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        mutationDiagramServlet.doPost(request, response);
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testSerializable() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(mutationDiagramServlet);
        out.close();
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        Object dest = in.readObject();
        in.close();
        assertNotNull((MutationDiagramServlet) dest);
    }
}

