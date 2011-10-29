package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoMutation;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ExtendedMutation;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Unit test for CgdsMutationService.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ DaoGeneOptimized.class, DaoMutation.class })
public final class CgdsMutationServiceTest extends AbstractMutationServiceTest {
    @Mock
    private CanonicalGene gene;
    @Mock
    private DaoGeneOptimized geneDao;
    @Mock
    private DaoMutation mutationDao;
    @Mock
    private ExtendedMutation extendedMutation;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(DaoGeneOptimized.class);
        PowerMockito.mockStatic(DaoMutation.class);
        try {
            PowerMockito.when(DaoGeneOptimized.getInstance()).thenReturn(geneDao);
            PowerMockito.when(DaoMutation.getInstance()).thenReturn(mutationDao);
        }
        catch (DaoException e) {
            fail(e.getMessage());
        }
        super.setUp();
    }

    @Override
    protected MutationService createMutationService() {
        return new CgdsMutationService(geneDao, mutationDao);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructorNullGeneDao() {
        new CgdsMutationService(null, mutationDao);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructorNullMutationDao() {
        new CgdsMutationService(geneDao, null);
    }

    @Test
    public void testGetMutations() throws DaoException {
        when(geneDao.getGene("DVL1")).thenReturn(gene);
        when(gene.getEntrezGeneId()).thenReturn(1855L);
        ArrayList<ExtendedMutation> extendedMutations = new ArrayList<ExtendedMutation>(); // :|
        extendedMutations.add(extendedMutation);
        when(mutationDao.getMutations(1, 1855L)).thenReturn(extendedMutations);
        when(extendedMutation.getAminoAcidChange()).thenReturn("K123G");

        List<Mutation> mutations = mutationService.getMutations("DVL1");
        assertNotNull(mutations);
        assertEquals(1, mutations.size());

        Mutation mutation = mutations.get(0);
        assertEquals(123, mutation.getLocation());
        assertEquals(1, mutation.getCount());
        assertNull(mutation.getLabel());
    }

    @Test(expected=NullPointerException.class)
    // todo:  should probably return an empty list instead of NPE
    public void testGetMutationsInvalidGeneSymbol() {
        when(geneDao.getGene("DVL1")).thenReturn(null);
        mutationService.getMutations("DVL1");
    }

    @Test
    public void testGetMutationsDaoException() throws DaoException {
        when(geneDao.getGene("DVL1")).thenReturn(gene);
        when(gene.getEntrezGeneId()).thenReturn(1855L);
        when(mutationDao.getMutations(1, 1855L)).thenThrow(new DaoException("message"));
        List<Mutation> mutations = mutationService.getMutations("DVL1");
        assertNotNull(mutations);
        assertTrue(mutations.isEmpty());
    }
}
