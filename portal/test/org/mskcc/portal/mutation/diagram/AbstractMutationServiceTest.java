package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Abstract unit test for implementations of MutationService.
 */
public abstract class AbstractMutationServiceTest {
    protected MutationService mutationService;

    /**
     * Create and return a new instance of an implementation of MutationService to test.
     *
     * @return a new instance of an implementation of MutationService to test
     */
    protected abstract MutationService createMutationService();

    @Before
    public void setUp() {
        mutationService = createMutationService();
    }

    @Test
    public final void testCreateMutationService() {
        assertNotNull(mutationService);
    }

    @Test(expected=NullPointerException.class)
    public final void testGetMutationsNullHugoGeneSymbol() {
        mutationService.getMutations(null);
    }
}