package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Unit test for MutationDiagram.
 */
public final class MutationDiagramTest {
    private Domain domain;
    private Mutation mutation;
    private MutationDiagram mutationDiagram;
    private static final List<Domain> EMPTY_DOMAINS = Collections.emptyList();
    private static final List<Mutation> EMPTY_MUTATIONS = Collections.emptyList();

    @Before
    public void setUp() {
        domain = new Domain("label", 0, 42);
        mutation = new Mutation(42, 2);
        mutationDiagram = new MutationDiagram("label", 42, EMPTY_DOMAINS, EMPTY_MUTATIONS);
    }

    @Test
    public void testConstructor() {
        assertNotNull(mutationDiagram);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructorNullLabel() {
        new MutationDiagram(null, 42, EMPTY_DOMAINS, EMPTY_MUTATIONS);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructorNullDomains() {
        new MutationDiagram("label", 42, null, EMPTY_MUTATIONS);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructorNullMutations() {
        new MutationDiagram("label", 42, EMPTY_DOMAINS, null);
    }

    @Test
    public void testLabel() {
        assertEquals("label", mutationDiagram.getLabel());
    }

    @Test
    public void testLength() {
        assertEquals(42, mutationDiagram.getLength());
    }

    @Test
    public void testDomainsEmpty() {
        assertNotNull(mutationDiagram.getDomains());
        assertTrue(mutationDiagram.getDomains().isEmpty());
    }

    @Test
    public void testDomains() {
        mutationDiagram = new MutationDiagram("label", 42, ImmutableList.of(domain), EMPTY_MUTATIONS);
        assertNotNull(mutationDiagram.getDomains());
        assertFalse(mutationDiagram.getDomains().isEmpty());
        assertTrue(mutationDiagram.getDomains().contains(domain));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testDomainsIsImmutable() {
        mutationDiagram.getDomains().clear();
    }

    @Test
    public void testMutationsEmpty() {
        assertNotNull(mutationDiagram.getMutations());
        assertTrue(mutationDiagram.getMutations().isEmpty());
    }

    @Test
    public void testMutations() {
        mutationDiagram = new MutationDiagram("label", 42, EMPTY_DOMAINS, ImmutableList.of(mutation));
        assertNotNull(mutationDiagram.getMutations());
        assertFalse(mutationDiagram.getMutations().isEmpty());
        assertTrue(mutationDiagram.getMutations().contains(mutation));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testMutationsIsImmutable() {
        mutationDiagram.getMutations().clear();
    }

    @Test
    public void testToJSONStringEmpty() {
        assertEquals("{\"length\":42,\"label\":\"label\"}", mutationDiagram.toJSONString());
    }

    @Test
    public void testToJSONString() {
        mutationDiagram = new MutationDiagram("label", 42, ImmutableList.of(domain), ImmutableList.of(mutation));
        assertEquals("{\"mutations\":[{\"count\":2,\"location\":42}],\"domains\":[{\"start\":0,\"label\":\"label\",\"end\":42}],\"length\":42,\"label\":\"label\"}", mutationDiagram.toJSONString());
    }
}

