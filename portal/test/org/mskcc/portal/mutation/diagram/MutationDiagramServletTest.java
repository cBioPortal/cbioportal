package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertNotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test for MutationDiagramServlet.
 */
public class MutationDiagramServletTest {
    private MutationDiagramServlet mutationDiagramServlet;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mutationDiagramServlet = new MutationDiagramServlet();
    }

    @Test
    public void testConstructor() {
        assertNotNull(mutationDiagramServlet);
    }

    @Test
    public void testDoPost() throws Exception {
        mutationDiagramServlet.doPost(request, response);
    }

    @Test
    public void testSerializable() throws Exception {
        // empty
    }
}

