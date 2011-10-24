package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for mutation.
 */
public final class MutationTest {
    private Mutation mutation;

    @Before
    public void setUp() {
        mutation = new Mutation("label", 0, 42);
    }

    @Test
    public void testConstructor() {
        assertNotNull(mutation);
    }

    @Test
    public void testConstructorNullLabel() {
        assertEquals(null, new Mutation(null, 0, 42).getLabel());
    }

    @Test
    public void testLocationCountConstructorNullLabel() {
        assertEquals(null, new Mutation(0, 42).getLabel());
    }

    @Test
    public void testLabel() {
        assertEquals("label", mutation.getLabel());
    }

    @Test
    public void testLocation() {
        assertEquals(0, mutation.getLocation());
    }

    @Test
    public void testCount() {
        assertEquals(42, mutation.getCount());
    }

    @Test
    public void testToJSONString() {
        // empty
    }
}