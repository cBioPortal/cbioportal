package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertNotNull;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for MockMutationDiagramModule.
 */
public final class MockMutationDiagramModuleTest {
    private Injector injector;

    @Before
    public void setUp() {
        injector = Guice.createInjector(new MockMutationDiagramModule());
    }    

    @Test
    public void testDomainServiceBinding() {
        assertNotNull(injector.getBinding(DomainService.class));
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