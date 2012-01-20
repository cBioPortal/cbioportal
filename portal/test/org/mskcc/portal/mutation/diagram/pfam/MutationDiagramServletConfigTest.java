package org.mskcc.portal.mutation.diagram.pfam;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mskcc.portal.mutation.diagram.IdMappingService;
import org.mskcc.portal.mutation.diagram.MutationService;

import com.google.inject.Injector;

/**
 * Unit test for MutationDiagramServletConfig.
 */
public final class MutationDiagramServletConfigTest {
    private MutationDiagramServletConfig servletConfig;

    @Before
    public void setUp() {
        servletConfig = new MutationDiagramServletConfig();
    }

    @Test
    public void testConstructor() {
        assertNotNull(servletConfig);
    }

    @Test
    public void testInjector() {
        Injector injector = servletConfig.getInjector();
        assertNotNull(injector.getBinding(FeatureService.class));
        assertNotNull(injector.getBinding(IdMappingService.class));
        assertNotNull(injector.getBinding(MutationService.class));
        // todo:  assert servlet bindings are present
    }
}