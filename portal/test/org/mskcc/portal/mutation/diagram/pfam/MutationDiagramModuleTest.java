package org.mskcc.portal.mutation.diagram.pfam;

import static org.junit.Assert.assertNotNull;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.Before;
import org.junit.Test;
import org.mskcc.portal.mutation.diagram.IdMappingService;
import org.mskcc.portal.mutation.diagram.MutationService;

/**
 * Unit test for MutationDiagramModule.
 */
public final class MutationDiagramModuleTest {
    private Injector injector;

    @Before
    public void setUp() {
        injector = Guice.createInjector(new MutationDiagramModule());
    }    

    @Test
    public void testFeatureServiceBinding() {
        assertNotNull(injector.getBinding(FeatureService.class));
    }

    @Test
    public void testIdMappingServiceBinding() {
        assertNotNull(injector.getBinding(IdMappingService.class));
    }

    @Test
    public void testMutationServiceBinding() {
        assertNotNull(injector.getBinding(MutationService.class));
    }
}