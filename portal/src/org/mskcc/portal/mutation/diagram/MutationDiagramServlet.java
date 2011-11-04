package org.mskcc.portal.mutation.diagram;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

/**
 * Mutation diagram servlet.
 */
@Singleton
public final class MutationDiagramServlet extends HttpServlet {
    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // todo:  check and sanitize hugoGeneSymbol if necessary; allow more than one
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/jsp/mutation_diagram.jsp");
        dispatcher.forward(request, response);
    }
}
