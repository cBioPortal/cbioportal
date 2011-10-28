package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Unit test for ListMultimapMutationService.
 */
public final class ListMultimapMutationServiceTest extends AbstractMutationServiceTest {
    private static final Mutation D123K = new Mutation("D123K", 265, 7);
    private static final ListMultimap<String, Mutation> MUTATIONS = ArrayListMultimap.create();

    @BeforeClass
    public static void populateMutations() {
        MUTATIONS.put("DVL1", new Mutation(42, 4));
        MUTATIONS.put("DVL1", new Mutation(197, 3));
        MUTATIONS.put("DVL1", D123K);
        MUTATIONS.put("BRCA2", new Mutation(99, 2));
    }

    @Override
    protected MutationService createMutationService() {
        return new ListMultimapMutationService(MUTATIONS);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructorNullMutations() {
        new ListMultimapMutationService(null);
    }

    @Test
    public void testGetMutations() {
        List<Mutation> mutations = mutationService.getMutations("DVL1");
        assertNotNull(mutations);
        assertFalse(mutations.isEmpty());
        assertEquals(3, mutations.size());
        assertTrue(mutations.contains(D123K));
    }

   @Test
    public void testGetMutationsHugoGeneSymbolNotFound() {
        List<Mutation> mutations = mutationService.getMutations("notFound");
        assertNotNull(mutations);
        assertTrue(mutations.isEmpty());
    }
}