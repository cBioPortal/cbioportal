package org.mskcc.portal.mut_diagram.impl;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mskcc.portal.mut_diagram.FeatureService;
import org.mskcc.portal.mut_diagram.IdMappingService;

import com.google.inject.Guice;
import com.google.inject.Injector;

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
}