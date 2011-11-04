package org.mskcc.portal.mutation.diagram;

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

/**
 * Mutation diagram servlet configuration.
 */
public final class MutationDiagramServletConfig extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new MockMutationDiagramModule(), new ServletModule() {
                @Override
                protected void configureServlets() {
                    serve("/mutation_diagram.do").with(MutationDiagramServlet.class);
                    serve("/mutation_diagram_data.json").with(MutationDiagramDataServlet.class);
                }
            });
    }
}
