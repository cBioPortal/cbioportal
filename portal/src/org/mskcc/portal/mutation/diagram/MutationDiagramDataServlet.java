package org.mskcc.portal.mutation.diagram;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

/**
 * Mutation diagram data servlet.
 */
public final class MutationDiagramDataServlet extends HttpServlet {
    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    private final DomainService domainService;
    private final IdMappingService idMappingService;
    private final MutationService mutationService;

    @Inject
    public MutationDiagramDataServlet(final DomainService domainService, final IdMappingService idMappingService, final MutationService mutationService) {
        this.domainService = domainService;
        this.idMappingService = idMappingService;
        this.mutationService = mutationService;
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        String hugoGeneSymbol = request.getParameter("hugoGeneSymbol");
        int length = 0;  // uh oh
        String uniProtId = idMappingService.getUniProtId(hugoGeneSymbol);
        List<Domain> domains = domainService.getDomains(uniProtId);
        List<Mutation> mutations = mutationService.getMutations(hugoGeneSymbol);
        String label = hugoGeneSymbol + "/" + uniProtId;
        MutationDiagram mutationDiagram = new MutationDiagram(label, length, domains, mutations);
        response.getWriter().append(mutationDiagram.toJSONString());
    }
}
