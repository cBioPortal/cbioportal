package org.mskcc.portal.mutation.diagram;

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.google.inject.servlet.GuiceServletContextListener;

/**
 * Mutation diagram servlet configuration.
 */
public final class MutationDiagramServletConfig extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new MockMutationDiagramModule());
    }
}
