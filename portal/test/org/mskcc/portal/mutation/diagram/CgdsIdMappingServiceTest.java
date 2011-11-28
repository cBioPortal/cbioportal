package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoUniProtIdMapping;
import org.mskcc.cgds.model.CanonicalGene;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableList;

/**
 * Unit test for CgdsIdMappingService.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ DaoGeneOptimized.class, DaoUniProtIdMapping.class })
public final class CgdsIdMappingServiceTest extends AbstractIdMappingServiceTest {
    @Mock
    private CanonicalGene gene;
    @Mock
    private DaoGeneOptimized geneDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(DaoGeneOptimized.class);
        PowerMockito.mockStatic(DaoUniProtIdMapping.class);
        try {
            PowerMockito.when(DaoGeneOptimized.getInstance()).thenReturn(geneDao);
        }
        catch (DaoException e) {
            fail(e.getMessage());
        }
        super.setUp();
    }

    @Override
    protected IdMappingService createIdMappingService() {
        return new CgdsIdMappingService(geneDao);
    }

    @Test
    public void testGetUniProtIdMappings() throws DaoException {
        when(geneDao.getGene("DVL1")).thenReturn(gene);
        when(gene.getEntrezGeneId()).thenReturn(1855L);
        PowerMockito.when(DaoUniProtIdMapping.getUniProtIds(1855)).thenReturn(ImmutableList.of("DVL1_HUMAN", "O"));

        List<String> uniProtIds = idMappingService.getUniProtIds("DVL1");
        assertNotNull(uniProtIds);
        assertFalse(uniProtIds.isEmpty());
        assertTrue(uniProtIds.contains("DVL1_HUMAN"));
        assertTrue(uniProtIds.contains("O"));
    }

    @Test
    public void testGetUniProtIdMappingsCanonicalGeneNotFound() {
        when(geneDao.getGene("DVL1")).thenReturn(null);

        List<String> uniProtIds = idMappingService.getUniProtIds("DVL1");
        assertNotNull(uniProtIds);
        assertTrue(uniProtIds.isEmpty());
    }

    @Test
    public void testGetUniProtIdMappingsNotFound() throws DaoException {
        when(geneDao.getGene("DVL1")).thenReturn(gene);
        when(gene.getEntrezGeneId()).thenReturn(1855L);
        PowerMockito.when(DaoUniProtIdMapping.getUniProtIds(1855)).thenReturn(Collections.<String>emptyList());

        List<String> uniProtIds = idMappingService.getUniProtIds("DVL1");
        assertNotNull(uniProtIds);
        assertTrue(uniProtIds.isEmpty());
    }

    @Test
    public void testGetUniProtIdMappingsDaoUniProtIdMappingDaoException() throws DaoException {
        when(geneDao.getGene("DVL1")).thenReturn(gene);
        when(gene.getEntrezGeneId()).thenReturn(1855L);
        PowerMockito.when(DaoUniProtIdMapping.getUniProtIds(1855)).thenThrow(new DaoException("message"));

        List<String> uniProtIds = idMappingService.getUniProtIds("DVL1");
        assertNotNull(uniProtIds);
        assertTrue(uniProtIds.isEmpty());
    }
}