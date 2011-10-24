package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for Domain.
 */
public final class DomainTest {
    private Domain domain;

    @Before
    public void setUp() {
        domain = new Domain("label", 0, 42);
    }

    @Test
    public void testConstructor() {
        assertNotNull(domain);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructorNullLabel() {
        new Domain(null, 0, 42);
    }

    @Test
    public void testLabel() {
        assertEquals("label", domain.getLabel());
    }

    @Test
    public void testStart() {
        assertEquals(0, domain.getStart());
    }
 
    @Test
    public void testEnd() {
        assertEquals(42, domain.getEnd());
    }

    @Test
    public void testToJSONString() {
        // empty
    }
}