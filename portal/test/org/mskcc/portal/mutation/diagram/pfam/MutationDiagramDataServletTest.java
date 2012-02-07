package org.mskcc.portal.mutation.diagram.pfam;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
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

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mskcc.portal.mutation.diagram.IdMappingService;
import org.mskcc.portal.mutation.diagram.Mutation;
import org.mskcc.portal.mutation.diagram.MutationService;

import com.google.common.collect.ImmutableList;

/**
 * Unit test for MutationDiagramDataServlet.
 */
public final class MutationDiagramDataServletTest {
    private MutationDiagramDataServlet dataServlet;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private ObjectMapper objectMapper;
    private FeatureService featureService;
    private IdMappingService idMappingService;
    private MutationService mutationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        featureService = mock(FeatureService.class, withSettings().serializable());
        idMappingService = mock(IdMappingService.class, withSettings().serializable());
        mutationService = mock(MutationService.class, withSettings().serializable());
        dataServlet = new MutationDiagramDataServlet(objectMapper, featureService, idMappingService, mutationService);
    }

    @Test
    public void testConstructor() {
        assertNotNull(dataServlet);
    }

    @Test
    public void testDoPost() throws Exception {
        when(request.getParameter("hugoGeneSymbol")).thenReturn("PIK3CA");
        when(idMappingService.getUniProtIds("PIK3CA")).thenReturn(ImmutableList.of("P42336"));
        //when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        //when(objectMapper.readValue(any(URL.class), any(CollectionType.class))).thenReturn(sequences);
        Sequence sequence = new Sequence();
        sequence.setLength(42);
        when(featureService.getFeatures("P42336")).thenReturn(ImmutableList.of(sequence));
        when(mutationService.getMutations("PIK3CA")).thenReturn(Collections.<Mutation>emptyList());
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        dataServlet.doPost(request, response);
        verify(response).setContentType("application/json");
        assertEquals("[{\"length\":42,\"markups\":null,\"motifs\":null,\"regions\":null,\"metadata\":{\"hugoGeneSymbol\":\"PIK3CA\",\"uniProtId\":\"P42336\"},\"options\":null}]", stringWriter.toString());
    }

    @Test
    @Ignore // (ObjectMapper is not serializable)
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

